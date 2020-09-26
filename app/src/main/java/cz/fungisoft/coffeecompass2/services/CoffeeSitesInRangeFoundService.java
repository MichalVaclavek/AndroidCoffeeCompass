package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeFoundListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeFromServerResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeSearchOperationListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Service to check, if there is a change of CoffeeSites in the search range while equipment is
 * moving.<br>
 * Implements PropertyChangeListener of the LocationService, which is needed for equipment move
 * detection.<br>
 * Can use repository in case on OFFLiNE mode.
 */
public class CoffeeSitesInRangeFoundService extends Service implements PropertyChangeListener,
                                                                       CoffeeSitesInRangeFromServerResultListener {

    private static final String TAG = "SitesInRangeUpdateSrv";

    private boolean offlinemode = false;

    /**
     * CoffeeSites repository to be used in case of OFFLINE mode
     */
    private static CoffeeSiteRepository coffeeSiteRepository;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        CoffeeSitesInRangeFoundService getService() {
            return CoffeeSitesInRangeFoundService.this;
        }
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSitesInRangeFoundService.LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Actual list of CoffeeSites in the search range from current position of the equipment.
     */
    //private List<CoffeeSiteMovable> currentSitesInRange = new ArrayList<>();


    private LiveData<List<CoffeeSite>> foundSites;

    public LiveData<List<CoffeeSite>> getFoundSites() {
        return foundSites;
    }

    /**
     * Location when the currentSitesInRange where observed
     */
    private LatLng searchLocationOfCurrentSites;

    private int currentSearchRange;
    private String coffeeSort;

    /**
     * Cislo vetsi nez 0 a menzi nez 1, vyjadrujici kdy se ma provest
     * novy dotaz na server pro aktualni CoffeeSites.
     * Pri zmene lokace se vypocita o jakou vzdalenost se telefon posunul
     * a pokud je tato zmenu vetsi jako moveToRangeNewSearchRatio * currentSearchRange
     * pak se posle novy dotaz na server.
     */
    private final double DISTANCE_TO_RANGE_NEW_SEARCH_RATION = 0.15;

    /**
     * Listeners to listen events of start/stop of coffee sites in range searching
     */
    private List<CoffeeSitesInRangeSearchOperationListener> sitesInRangeSearchOperationListeners = new ArrayList<>();


    public void addSitesInRangeSearchOperationListener(CoffeeSitesInRangeSearchOperationListener sitesInRangeUpdateListener) {
        sitesInRangeSearchOperationListeners.add(sitesInRangeUpdateListener);
        Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + sitesInRangeSearchOperationListeners.size());
    }

    public void removeSitesInRangeSearchOperationListener(CoffeeSitesInRangeSearchOperationListener sitesInRangeUpdateListener) {
        sitesInRangeSearchOperationListeners.remove(sitesInRangeUpdateListener);
        Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + sitesInRangeSearchOperationListeners.size());
    }

    /**
     * Listeners to listen event of new coffeesites search result
     */
    private List<CoffeeSitesInRangeFoundListener> sitesInRangeFoundListeners = new ArrayList<>();

    public void addSitesInRangeFoundListener(CoffeeSitesInRangeFoundListener sitesInRangeUpdateListener) {
        sitesInRangeFoundListeners.add(sitesInRangeUpdateListener);
        //Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + sitesInRangeSearchOperationListeners.size());
    }

    public void removeSitesInRangeFoundListener(CoffeeSitesInRangeFoundListener sitesInRangeUpdateListener) {
        sitesInRangeFoundListeners.remove(sitesInRangeUpdateListener);
        //Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + sitesInRangeSearchOperationListeners.size());
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


    @Override
    public void onCreate() {
        super.onCreate();
        coffeeSiteRepository = new CoffeeSiteRepository(CoffeeSiteDatabase.getDatabase(getApplicationContext()));
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
            locationService.removePropertyChangeListener(this);
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
     * Listen to location changes invoked by LocationService.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        long movedDistance = locationService.getDistanceFromCurrentLocation(searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude);

        if (movedDistance >= DISTANCE_TO_RANGE_NEW_SEARCH_RATION * currentSearchRange) {
            searchLocationOfCurrentSites = locationService.getCurrentLatLng();
            if (!offlinemode) {
                startSearchSitesInRangeFromServer(coffeeSort, searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude, this.currentSearchRange);
            } else {
                foundSites = coffeeSiteRepository.getCoffeeSitesInRange(searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude, this.currentSearchRange);
            }
        }
    }

    /**
     * Calls the REST API function to get current CoffeeSites in Range.
     * and updates the sites in range using locationService. If anything
     * updated, then inform listeners about the change.
     *
     * @return
     */
    public void requestUpdatesOfCurrentSitesInRange(LatLng searchLocationOfCurrentSites, int range, String coffeeSort, boolean offline) {
        this.searchLocationOfCurrentSites = searchLocationOfCurrentSites;
        if (locationService != null && this.searchLocationOfCurrentSites == null) {
            this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
        }
        this.currentSearchRange = range;
        this.coffeeSort = coffeeSort;
        this.offlinemode = offline;

        if (!offlinemode) {
            startSearchSitesInRangeFromServer(coffeeSort, searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude, this.currentSearchRange);
        } else {
            foundSites = coffeeSiteRepository.getCoffeeSitesInRange(searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude, this.currentSearchRange);
        }
    }

    /**
     * Calls REST async. task. or requests DB if in OFFLINE mode
     *
     * @param coffeeSort
     */
    private void startSearchSitesInRangeFromServer(String coffeeSort, double latitude, double longitude, int range) {

        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
            listener.onStartSearchingSitesInRange();
        }

        if (Utils.isOnline() ) {
            new GetCoffeeSitesInRangeAsyncTask(this,
                    latitude,
                    longitude,
                    range,
                    coffeeSort).execute();
        }
    }

    /**
     * A callback method to be called, when there are CoffeeSites in range returned by
     * async. task called by GetSitesInRangeAsyncTask.onPostExecute(result).
     * Compares returned coffeeSites with currentSitesInRange and finds new and old
     * CoffeeSites.
     */
    @Override
    public void onSitesInRangeReturnedFromServer(List<CoffeeSiteMovable> coffeeSites) {
        if (coffeeSites.size() > 0) {
            for (CoffeeSitesInRangeFoundListener listener : sitesInRangeFoundListeners) {
                listener.onSitesInRangeFound(coffeeSites);
            }
        }
        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
            listener.onSearchingSitesInRangeFinished();
        }
    }

    @Override
    public void onSitesInRangeReturnedFromServerError(String error) {
        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
            listener.onSearchingSitesInRangeError(error);
            listener.onSearchingSitesInRangeFinished();
        }
    }

}