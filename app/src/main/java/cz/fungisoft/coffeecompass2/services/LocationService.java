package cz.fungisoft.coffeecompass2.services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;

/**
 * Location service to provide location to all other Activities.<br>
 * Basically used in conjuction with ActivityWithLocationService ancestors
 */
public class LocationService extends Service {

    private String TAG = "Location service";

    private static final long GPS_REFRESH_TIME_MS = 2_000; // milisecond of GPS refresh ?
    private static final long MAX_STARI_DAT = 1000 * 60; // pokud jsou posledni zname udaje o poloze starsi jako 1 minuta, zjistit nove (po spusteni app.)
    private static final long POLLING = 1000 * 2; // milisecond of GPS refresh ?
    private static final float MIN_PRESNOST = 25.0f;
    private static final float LAST_PRESNOST = 1000.0f;
    private static final float MIN_VZDALENOST = 3.0f; // min. zmena GPS polohy, ktera vyvola onLocationChanged() ?

    private NotificationManager mNM;

    /**
     * Support for property change, location in this case.
     */
    private PropertyChangeSupport support;

    public LocationService() {
        support = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
        Log.d(TAG,  ". Pocet posluchacu zmeny polohy: " + support.getPropertyChangeListeners().length);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
        Log.d(TAG,  ". Pocet posluchacu zmeny polohy: " + support.getPropertyChangeListeners().length);
    }

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    private LocationManager locManager;
    private Location location;
    private LocationListener locListener;

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if ((locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE)) == null) {
            this.stopSelf();
        }

        Log.d(TAG, "Service started.");

        locListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location loc) {

                if (location == null // Current location has better then Min. accuracy
                                     // and time period for observing location elapsed
                        ||
                        (location.getTime() < (System.currentTimeMillis() - GPS_REFRESH_TIME_MS) && (loc.hasAccuracy()) )
                        && (
                            (loc.getProvider().equals("gps") && (loc.getAccuracy() < MIN_PRESNOST))
                            || // only available provider is network
                            ((locManager.getProviders(true).size() == 1) && loc.getProvider().equals("network") && loc.getAccuracy() < MIN_PRESNOST * 5)
                        )
                )
                {
                    Location oldLocation = location;
                    location = loc;
                    support.firePropertyChange("location", oldLocation, location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        //startServiceOrStopIfNotPermitted();

    }

    /**
     * Zjisteni posledni zname pozice po spusteni Service.
     *
     * @param minAccuracy
     * @param cas
     * @return
     */
    public Location posledniPozice(float minAccuracy, long cas) {

        Location vysledek = null;
        float topPresnost = Float.MAX_VALUE;
        long topCas = 0;

        List<String> matchingProviders = locManager.getAllProviders();
        for (String provider : matchingProviders) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return null;
            }
            Location location = locManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if (accuracy < topPresnost) {
                    vysledek = location;
                    topPresnost = accuracy;
                    topCas = time;
                }
            }
        }

        if (topPresnost > minAccuracy
            || (System.currentTimeMillis() - topCas) > cas)
            return null;
        else
            return vysledek;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);

        startServiceOrStopIfNotPermitted();

        return START_NOT_STICKY;
    }

    private void startServiceOrStopIfNotPermitted() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            this.stopSelf();
            return;
        }

        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, POLLING, MIN_VZDALENOST, locListener);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, POLLING, MIN_VZDALENOST, locListener);
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        locManager.removeUpdates(locListener);
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        startServiceOrStopIfNotPermitted();
        location = posledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
        return mBinder;
    }

    public LatLng getCurrentLatLng() {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public Location getCurrentLocation() {
        return location;
    }
    /**
     *
     * @param lat1 - latitude of the point we want to find distnace from current location
     * @param lon1 - langitude of the point we want to find distnace from current location
     * @return
     */
    public long getDistanceFromCurrentLocation(double lat1, double lon1) {

        double lat2;
        double lon2;

        if (location != null) {
            lat2 = location.getLatitude();
            lon2 = location.getLongitude();
        } else
            return 0;

        long eRadius = 6372000; // polomer Zeme v metrech, v CR?
        long distance;
        double c, a;

        double latDist = Math.toRadians( lat2 - lat1 );
        double lonDist = Math.toRadians( lon2 - lon1 );
        a = Math.pow( Math.sin( latDist/2 ), 2 ) + Math.cos( Math.toRadians( lat1 ) ) * Math.cos( Math.toRadians( lat2 ) ) * Math.pow( Math.sin( lonDist / 2 ), 2 );
        c  = 2 * Math.atan2( Math.sqrt( a ), Math.sqrt( 1 - a ) );

        distance = Math.round(eRadius * c);

        return distance;
    }

}