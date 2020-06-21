package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeUpdateListener;

/**
 * Service to check, if there is a change of CoffeeSites int hte search range while equipment is
 * moving.<br>
 * Implements PropertyChangeListener of the LocationService which is needed for equipment move
 * detection.
 */
public class CoffeeSitesInRangeUpdateService extends Service implements PropertyChangeListener, CoffeeSitesInRangeResultListener {

    private static final String TAG = "SitesInRangeUpdateSrv";

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        CoffeeSitesInRangeUpdateService getService() {
            return CoffeeSitesInRangeUpdateService.this;
        }
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSitesInRangeUpdateService.LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Actual list of CoffeeSites in the search range from current position of the equipment.
     */
    private List<CoffeeSiteMovable> currentSitesInRange = new ArrayList<>();

    /**
     * Location when the currentSitesInRange where observed
     */
    private LatLng searchLocationOfCurrentSites;

    private int currentSearchRange;

    /**
     * Cislo vetsi nez 0 a menzi nez 1, vyjadrujici kdy se ma provest
     * novy dotaz na server pro aktualni CoffeeSites.
     * Pri zmene lokace se vypocita o jakou vzdalenost se telefon posunul
     * a pokud je tato zmenu vetsi jako moveToRangeNewSearchRatio * currentSearchRange
     * pak se posle novy dotaz na server.
     */
    private double distanceToRangeNewSearchRatio = 0.15;

    /**
     * CoffeeSites, which have come into current Range.
     * (i.e. the sites which were not included in the previous actual list of Sites in Range, but
     * are included in the current sites in Range. i.e. "plus" difference between current
     * and old sites in Range)
     */
    private List<CoffeeSiteMovable> newSitesInRange = new ArrayList<>();

    /**
     * CoffeeSites, which have left the current Range.
     * (i.e. the sites which were included in the previous actual list of Sites in Range, but
     * not included in the current sites in Range. i.e. "minus" difference between current
     * and old sites in Range)
     */
    private List<CoffeeSiteMovable> goneSitesOutOfRange = new ArrayList<>();;

    private List<CoffeeSitesInRangeUpdateListener>  sitesInRangeUpdateListeners = new ArrayList<>();

    private String coffeeSort;

    public CoffeeSitesInRangeUpdateService() {
    }

    public void addSitesInRangeUpdateListener(CoffeeSitesInRangeUpdateListener sitesInRangeUpdateListener) {
        sitesInRangeUpdateListeners.add(sitesInRangeUpdateListener);
        Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + sitesInRangeUpdateListeners.size());
    }

    public void removePropertyChangeListener(CoffeeSitesInRangeUpdateListener sitesInRangeUpdateListener) {
        sitesInRangeUpdateListeners.remove(sitesInRangeUpdateListener);
        Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + sitesInRangeUpdateListeners.size());
    }

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbind;

    //Location service
    protected LocationService locationService;
    private LocationServiceConnector locationServiceConnector;

    public void onLocationServiceConnected() {
        locationService = locationServiceConnector.getLocationService();
        locationService.addPropertyChangeListener(this);
    }

    /**
     * Listen to location changes invoked by LocationService.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        long movedDistance = locationService.getDistanceFromCurrentLocation(searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude);

        if (movedDistance >= distanceToRangeNewSearchRatio * currentSearchRange) {
            searchLocationOfCurrentSites = locationService.getCurrentLatLng();
            startGetSitesInRangeAsyncTask(this.coffeeSort, this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude, this.currentSearchRange);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        doBindLocationService();
    }

    private void doBindLocationService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        locationServiceConnector = new LocationServiceConnector(this);
        if (bindService(new Intent(this, LocationService.class),
                locationServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.e(TAG, "Error: The requested 'LocationService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindLocationService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            unbindService(locationServiceConnector);
            mShouldUnbind = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindLocationService();
    }

    /**
     * Calls the REST API function to get current CoffeeSites in Range.
     * and updates the sites in range using locationService. If anything
     * updated, then inform listeners about the change.
     *
     * @return
     */
    public void requestUpdatesOfCurrentSitesInRange(LatLng searchLocationOfCurrentSites, int range, String coffeeSort) {
        this.searchLocationOfCurrentSites = searchLocationOfCurrentSites;
        if (locationService != null && this.searchLocationOfCurrentSites == null) {
            this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
        }
        this.currentSearchRange = range;
        this.coffeeSort = coffeeSort;
          // Call REST async. task
        startGetSitesInRangeAsyncTask(coffeeSort, searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude, range);
    }

    /**
     * Calls REST async. task.
     *
     * @param coffeeSort
     */
    private void startGetSitesInRangeAsyncTask(String coffeeSort, double latitude, double longitude, int range) {

        if (Utils.isOnline()) {
            for (CoffeeSitesInRangeUpdateListener listener : sitesInRangeUpdateListeners) {
                listener.onStartSearchingSitesInRange();
            }
            new GetCoffeeSitesInRangeAsyncTask(this,
                    latitude,
                    longitude,
                    range,
                    coffeeSort).execute();
        } else {
            showNoInternetToast();
        }
    }

    /**
     * A callback method to be called, when there are CoffeeSites in range returned by
     * async. task. Called by GetSitesInRangeAsyncTask.onPostExecute(result)
     * Compares return coffeeSites with currentSitesInRange and finds new and old
     * CoffeeSites.
     */
    @Override
    public void onSitesInRangeReturnedFromServer(List<CoffeeSiteMovable> coffeeSites) {
        // 1. Find newSites
        newSitesInRange.clear();
        newSitesInRange.addAll(coffeeSites);
        newSitesInRange.removeAll(currentSitesInRange);

        // 1. Find oldSites, i.e. sites not included in the coffeeSites
        goneSitesOutOfRange.clear();
        goneSitesOutOfRange.addAll(currentSitesInRange);
        goneSitesOutOfRange.removeAll(coffeeSites);

        currentSitesInRange = coffeeSites;

        for (CoffeeSitesInRangeUpdateListener listener : sitesInRangeUpdateListeners) {
            if (goneSitesOutOfRange.size() > 0) {
                listener.onSitesOutOfRange(goneSitesOutOfRange);
            }
            if (newSitesInRange.size() > 0) {
                listener.onNewSitesInRange(newSitesInRange);
            }

            listener.onSearchingSitesInRangeFinished();
        }
    }

    @Override
    public void onSitesInRangeReturnedFromServerError(String error) {
        for (CoffeeSitesInRangeUpdateListener listener : sitesInRangeUpdateListeners) {
            listener.onNewSitesInRangeError(error);
            listener.onSearchingSitesInRangeFinished();
        }
    }

    /**
     * Show info Toast message, that internet connection is not available
     */
    private void showNoInternetToast() {
        Toast toast = Toast.makeText(getApplicationContext(),
                "No Internet connection.",
                Toast.LENGTH_SHORT);
        toast.show();
    }

}