package cz.fungisoft.coffeecompass2.services;

import android.Manifest;
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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Location service to provide location to all other Activities.<br>
 * Provides location updates as a bound local service.
 */
public class LocationService extends Service {

    private final String TAG = "LocationService";

    private static final long GPS_REFRESH_TIME_MS = 2_000;
    private static final long MAX_STARI_DAT = 1000 * 60 * 15; // pokud jsou posledni zname udaje o poloze starsi jako 10 minut, zjistit nove (po spusteni app.)
    private static final long POLLING = 1000 * 2;
    private static final float LAST_PRESNOST = 1000.0f;
    private static final float MIN_VZDALENOST = 0.0f;

    private final PropertyChangeSupport support;

    public LocationService() {
        support = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public void removeAllLocationChangeListeners() {
        for (PropertyChangeListener pcl : support.getPropertyChangeListeners()) {
            if (pcl instanceof CoffeeSiteMovable) {
                ((CoffeeSiteMovable) pcl).removeAllDistanceChangeListeners();
            }
            support.removePropertyChangeListener(pcl);
        }
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    private LocationManager locManager;
    private Location location;
    private LocationListener locListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location loc) {
                boolean replaceLocation = false;

                if (location == null) {
                    replaceLocation = true;
                } else if (!loc.hasAccuracy()) {
                    replaceLocation = true;
                } else if (!location.hasAccuracy() || loc.getAccuracy() <= location.getAccuracy()) {
                    replaceLocation = true;
                } else if (location.getTime() < (System.currentTimeMillis() - (GPS_REFRESH_TIME_MS * 2))) {
                    replaceLocation = true;
                }

                if (replaceLocation) {
                    Location oldLocation = location;
                    location = loc;
                    Log.i(TAG, "Location updated. provider=" + loc.getProvider()
                            + ", acc=" + (loc.hasAccuracy() ? loc.getAccuracy() : -1)
                            + ", time=" + loc.getTime());
                    support.firePropertyChange("location", oldLocation, location);
                }
            }

            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(@NonNull String provider) {}
            @Override public void onProviderDisabled(@NonNull String provider) {}
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        requestLocationUpdates();
        return START_NOT_STICKY;
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "requestLocationUpdates(): missing location permission");
            return;
        }

        try {
            if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, POLLING, MIN_VZDALENOST, locListener);
                Log.i(TAG, "GPS_PROVIDER updates requested");
            } else {
                Log.w(TAG, "GPS_PROVIDER disabled");
            }
            if (locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, POLLING, MIN_VZDALENOST, locListener);
                Log.i(TAG, "NETWORK_PROVIDER updates requested");
            } else {
                Log.w(TAG, "NETWORK_PROVIDER disabled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting location updates", e);
        }
    }

    @Override
    public void onDestroy() {
        if (locManager != null && locListener != null) {
            locManager.removeUpdates(locListener);
        }
        super.onDestroy();
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        requestLocationUpdates();
        location = getPosledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
        Log.i(TAG, "onBind() lastLocation available: " + (location != null));
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind()");
        if (locManager != null && locListener != null) {
            locManager.removeUpdates(locListener);
        }
        return super.onUnbind(intent);
    }


    public Location getPosledniPozice(float minAccuracy, long cas) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Location bestLocation = null;
        List<String> providers = locManager.getProviders(true);
        for (String provider : providers) {
            Location l = locManager.getLastKnownLocation(provider);
            if (l == null) continue;
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }

        if (bestLocation == null) {
            Log.d(TAG, "Žádná poslední známá poloha nebyla nalezena.");
            return null;
        }

        long vekPolohySekundy = (System.currentTimeMillis() - bestLocation.getTime()) / 1000;
        Log.d(TAG, "Nalezena poloha je stará: " + vekPolohySekundy + " s. Limit je: " + (cas / 1000) + " s.");

        if (System.currentTimeMillis() - bestLocation.getTime() < cas) {
            return bestLocation;
        }
        return null;
    }

    public LatLng getCurrentLatLng() {
        return (location != null) ? new LatLng(location.getLatitude(), location.getLongitude()) : null;
    }

    public Location getCurrentLocation() {
        return location;
    }

    public long getDistanceFromCurrentLocation(double lat1, double lon1) {
        if (location == null) return 0;
        
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, location.getLatitude(), location.getLongitude(), results);
        return Math.round(results[0]);
    }
}
