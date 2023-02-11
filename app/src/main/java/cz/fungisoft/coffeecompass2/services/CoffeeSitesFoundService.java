package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetNumberOfCoffeeSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesFoundListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesFoundFromServerResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeSearchOperationListener;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Service to find CoffeeSites in current search range from current geo location. Uses other objects to get such data<br>
 * either from server or from DB (for OFFLINE mode).<br>
 * Also used to find all CoffeeSites in given Town.<br>
 * Implements PropertyChangeListener of the LocationService, which is needed for equipment move
 * detection.<br>
 */
public class CoffeeSitesFoundService extends Service implements PropertyChangeListener,
                                                                CoffeeSitesFoundFromServerResultListener {

    private static final String TAG = "SitesInRangeUpdateSrv";

    private static final long MAX_STARI_DAT = 1000 * 300; // pokud jsou posledni zname udaje o poloze starsi jako 5 minuty, zjistit nove
    private static final float LAST_PRESNOST = 100.0f; // pokud je posledni presnosy polohy horsi, zkus pockat na lepsi


    /**
     * Location when the currentSitesInRange where observed
     */
    private LatLng searchLocationOfCurrentSites;

    private int currentSearchRange;

    public void setCurrentSearchRange(int currentSearchRange) {
        this.currentSearchRange = currentSearchRange;
        if (!isSearching && Utils.isOfflineModeOn(getApplicationContext())) {
            setDBInput(this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude, this.currentSearchRange);
        }
    }

    private List<Integer> allSearchRanges;
    private String coffeeSort;

    /**
     * To detect a first successful detection of the location
     */
    private boolean firstLocationDetection = true;


    /**
     * CoffeeSites repository to be used in case of OFFLINE mode
     */
    private static CoffeeSiteRepository coffeeSiteRepository;

    /**
     * To detect, that search request to server is running
     */
    private boolean isSearching = false;

    /**
     * To detect, that search request for number of sites in range to server is running
     */
    private boolean isSearchingNumOfSites = false;


    /**
     * Main field provided by this service
     */
    private LiveData<List<CoffeeSiteMovable>> foundSites;

    public LiveData<List<CoffeeSiteMovable>> getFoundSites() {
        return foundSites;
    }

    private LiveData<Integer> numberOfFoundSites;

    public LiveData<Integer> getNumberOfFoundSites() {
        return numberOfFoundSites;
    }


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        CoffeeSitesFoundService getService() {
            return CoffeeSitesFoundService.this;
        }
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSitesFoundService.LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Cislo vetsi nez 0 a mensi nez 1, vyjadrujici, kdy se ma provest
     * novy dotaz na server pro aktualni CoffeeSites.
     * Pri zmene lokace se vypocita o jakou vzdalenost se telefon posunul
     * a pokud je tato zmenu vetsi jako moveToRangeNewSearchRatio * currentSearchRange
     * pak se posle novy dotaz na server nebo do DB.
     */
    private static final double DISTANCE_TO_RANGE_NEW_SEARCH_RATIO = 0.15;

    /**
     * Listeners to listen events of start/stop of coffee sites in range searching
     */
    private final List<CoffeeSitesInRangeSearchOperationListener> sitesInRangeSearchOperationListeners = new ArrayList<>();


    public void addFoundSitesSearchOperationListener(CoffeeSitesInRangeSearchOperationListener sitesInRangeUpdateListener) {
        sitesInRangeSearchOperationListeners.add(sitesInRangeUpdateListener);
        Log.d(TAG,  ". Pocet posluchacu zacatku/konce vyhledavani CoffeeSites: " + sitesInRangeSearchOperationListeners.size());
    }

    public void removeFoundSitesSearchOperationListener(CoffeeSitesInRangeSearchOperationListener sitesInRangeUpdateListener) {
        sitesInRangeSearchOperationListeners.remove(sitesInRangeUpdateListener);
        Log.d(TAG,  ". Pocet posluchacu zacatku/konce vyhledavani CoffeeSites: " + sitesInRangeSearchOperationListeners.size());
    }

    /**
     * Listeners to listen event of new coffeesites search result
     */
    private final List<CoffeeSitesFoundListener> sitesFoundListeners = new ArrayList<>();

    public void addSitesFoundListener(CoffeeSitesFoundListener sitesInRangeUpdateListener) {
        if (!sitesFoundListeners.contains(sitesInRangeUpdateListener)) {
            sitesFoundListeners.add(sitesInRangeUpdateListener);
        }
    }

    public void removeSitesFoundListener(CoffeeSitesFoundListener sitesInRangeUpdateListener) {
        sitesFoundListeners.remove(sitesInRangeUpdateListener);
    }

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private static boolean mShouldUnbind;

    // Location service
    protected static LocationService locationService;
    private static LocationServiceConnector locationServiceConnector;


    @Override
    public void onCreate() {
        super.onCreate();
        coffeeSiteRepository = new CoffeeSiteRepository(CoffeeSiteDatabase.getDatabase(getApplicationContext()));

        // Transform CoffeeSites returned from DB (sites within Rectangle) to CoffeeSiteMovable (sites within search circle) expected by FoundCoffeeSiteListActivity
        foundSites = Transformations.map(coffeeSiteRepository.getCoffeeSitesInRange(), coffeeSites -> {
            List<CoffeeSiteMovable> coffeeSiteMovables = new ArrayList<>();
            for (CoffeeSite cs : coffeeSites) { // filters only CoffeeSites in circle range and maps to CoffeeSiteMovable
                if ((searchLocationOfCurrentSites != null)
                    && (Utils.countDistanceMetersFromSearchPoint(cs.getLatitude(), cs.getLongitude(), searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude) <= currentSearchRange)) {
                        coffeeSiteMovables.add(new CoffeeSiteMovable(cs, searchLocationOfCurrentSites));
                }
            }
//            for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
//                listener.onSearchingSitesFinished(coffeeSiteMovables.size());
//            }
            return coffeeSiteMovables;
        });

        numberOfFoundSites = Transformations.map(coffeeSiteRepository.getCoffeeSitesInRange(), List::size);

        doBindLocationService();
    }

    private void doBindLocationService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
        Log.i(TAG, "Binding location service ...");
        locationServiceConnector = new LocationServiceConnector(this);
        if (bindService(new Intent(this, LocationService.class),
                locationServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.e(TAG, "Error: The requested 'LocationService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    public void onLocationServiceConnected() {
        locationService = locationServiceConnector.getLocationService();
        if (locationService != null) {
            Log.i(TAG, "Location service binded.");
            locationService.addPropertyChangeListener(this);
            if (searchLocationOfCurrentSites == null) {
                this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
            }
        }
    }

    private void doUnbindLocationService() {
        if (mShouldUnbind && locationService != null) {
            locationService.removePropertyChangeListener(this);
            // Release information about the service's state.
            unbindService(locationServiceConnector);
            mShouldUnbind = false;
        }
    }

    /**
     * Set new input search data (lat, long, range) to repository to initiate new DB search.
     *
     * @param latitudeFrom
     * @param longitudeFrom
     * @param searchRange
     */
    private void setDBInput(double latitudeFrom, double longitudeFrom, int searchRange) {
        coffeeSiteRepository.setNewLatLongRangeSearchCriteria(latitudeFrom, longitudeFrom, searchRange);
    }

    /**
     * Listen to location changes invoked by LocationService.
     * If the move distance overlaps given threshold, search of new CoffeeSites starts.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        long movedDistance;
        if (locationService != null) {
            if (this.searchLocationOfCurrentSites == null) {
                this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
            }

            if (this.searchLocationOfCurrentSites != null) {
                movedDistance = locationService.getDistanceFromCurrentLocation(this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude);

                if (movedDistance >= DISTANCE_TO_RANGE_NEW_SEARCH_RATIO * currentSearchRange
                    || firstLocationDetection) {
                    firstLocationDetection = false;
                    // get current location for searching
                    this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
                    if (this.searchLocationOfCurrentSites != null
                            && this.coffeeSort != null) {
                        startSearchingSitesInRange(this.coffeeSort, this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude, this.currentSearchRange);
                        startSearchingNumbersOfSitesInRanges(this.coffeeSort, this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude, this.allSearchRanges, this.currentSearchRange);
                        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
                            listener.onStartSearchingSites();
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates prerequisites for CoffeeSites search and starts searching for CoffeeSites in range.<br>
     * Also calls onStartSearchingSitesInRange of the listeners to perform action required at search begin.
     */
    public void requestUpdatesOfCurrentSitesInRange(LatLng searchLocationOfCurrentSites, int range, String coffeeSort) {
        getSearchLocation(searchLocationOfCurrentSites);

        this.currentSearchRange = range;
        this.coffeeSort = coffeeSort;

        if (this.searchLocationOfCurrentSites != null) {
            startSearchingSitesInRange(coffeeSort, this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude, this.currentSearchRange);
            for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
                listener.onStartSearchingSites();
            }
        }
    }

    /**
     * Validates prerequisites for number of CoffeeSites search and starts searching for CoffeeSites in range.<br>
     */
    public void requestUpdateOfCoffeeSitesNumberInRanges(LatLng searchLocationOfCurrentSites, int range, List<Integer> allRanges, String coffeeSort) {
        getSearchLocation(searchLocationOfCurrentSites);

        this.currentSearchRange = range;
        this.coffeeSort = coffeeSort;
        this.allSearchRanges = allRanges;

        if (this.searchLocationOfCurrentSites != null) {
            startSearchingNumbersOfSitesInRanges(coffeeSort, this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude, allRanges, this.currentSearchRange);
        }
    }

    private void getSearchLocation(LatLng searchLocationOfCurrentSites) {
        this.searchLocationOfCurrentSites = searchLocationOfCurrentSites;
        if (this.searchLocationOfCurrentSites == null && locationService != null) {
            this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
            if (this.searchLocationOfCurrentSites == null) {
                Location lastLocation = locationService.getPosledniPozice(LAST_PRESNOST, MAX_STARI_DAT); // 5 minutes old data are OK
                if (lastLocation != null) {
                    this.searchLocationOfCurrentSites = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                }
            }
        }
    }

    /**
     * Starts request for number of CoffeeSites in range either from server (REST API).
     *
     * @param coffeeSort
     * @param latitude
     * @param longitude
     * @param range
     */
    private synchronized void startSearchingNumbersOfSitesInRanges(String coffeeSort, double latitude, double longitude, List<Integer> allRanges, int selectedRange) {
        if (!isSearchingNumOfSites && !Utils.isOfflineModeOn(getApplicationContext())) {
            startSearchNumberOfSitesInRangeFromServer(coffeeSort, latitude, longitude, allRanges);
        }
        if (!isSearchingNumOfSites && Utils.isOfflineModeOn(getApplicationContext())) {
            setDBInput(latitude, longitude, selectedRange);
        }
    }

    /**
     * Starts request for CoffeeSites in range either from server (REST API) or sets the input to LiveData
     * to be returned from DB.
     *
     * @param coffeeSort
     * @param latitude
     * @param longitude
     * @param range
     */
    private synchronized void startSearchingSitesInRange(String coffeeSort, double latitude, double longitude, int range) {
        if (!isSearching) {
            if (!Utils.isOfflineModeOn(getApplicationContext())) {
                startSearchSitesInRangeFromServer(coffeeSort, latitude, longitude, range);
            }
            else { // invokes update of LiveData<List<CoffeeSite>> returned from DB
                setDBInput(latitude, longitude, range);
            }
        }
    }

    /**
     * Calls REST call Async. task.
     *
     * @param coffeeSort
     */
    private void startSearchSitesInRangeFromServer(String coffeeSort, double latitude, double longitude, int range) {
        isSearching = true;
        Log.i(TAG, "Start Async task for searching on server.");
        new GetCoffeeSitesInRangeAsyncTask(this,
                latitude,
                longitude,
                range,
                coffeeSort).execute();
    }

    /**
     * A callback method to be called, when there are CoffeeSites in range returned by
     * async. task called by GetSitesInRangeAsyncTask.onPostExecute(result).
     */
    @Override
    public void onSitesInRangeReturnedFromServer(List<CoffeeSiteMovable> coffeeSites) {
        isSearching = false;
        for (CoffeeSitesFoundListener listener : sitesFoundListeners) {
            listener.onSitesInRangeFound(coffeeSites);
        }
        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
            listener.onSearchingSitesFinished(coffeeSites.size());
        }
        Log.i(TAG, "Returned search of Coffee Sites from server. Number of coffee sites found: " + coffeeSites.size());
    }

    /**
     * Calls REST call Async. task
     *
     * @param coffeeSort
     */
    private void startSearchNumberOfSitesInRangeFromServer(String coffeeSort, double latitude, double longitude, List<Integer> ranges) {
        isSearchingNumOfSites = true;
        Log.i(TAG, "Start Async task for searching number of sites on server.");
        new GetNumberOfCoffeeSitesInRangeAsyncTask(this,
                latitude,
                longitude,
                ranges,
                coffeeSort).execute();
    }

    /**
     * A callback method to be called, when there are CoffeeSites in range returned by
     * async. task called by GetSitesInRangeAsyncTask.onPostExecute(result).
     */
    @Override
    public void onNumberOfSitesInRangesReturnedFromServer(Map<String, Integer> numOfCoffeeSitesInDistances) {
        isSearchingNumOfSites = false;
        for (CoffeeSitesFoundListener listener : sitesFoundListeners) {
            listener.onNumbersOfSitesInRangesFound(numOfCoffeeSitesInDistances);
        }
        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
            listener.onSearchingSitesFinished(0);
        }
        Log.i(TAG, "Returned search from server.");
    }

    @Override
    public void onSitesInRangeReturnedFromServerError(String error) {
        isSearching = false;
        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
            listener.onSearchingSitesError(error);
            listener.onSearchingSitesFinished(0);
        }
    }

    @Override
    public void onDestroy() {
        doUnbindLocationService();
        super.onDestroy();
    }
}