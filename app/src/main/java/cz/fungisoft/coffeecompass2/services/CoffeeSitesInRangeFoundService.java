package cz.fungisoft.coffeecompass2.services;

import android.app.Notification;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeFoundListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeFromServerResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeSearchOperationListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.widgets.MainAppWidgetProvider;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Service to check, if there is a change of CoffeeSites in the search range while equipment is
 * moving.<br>
 * Implements PropertyChangeListener of the LocationService, which is needed for equipment move
 * detection.<br>
 * Can use repository in case of OFFLiNE mode.
 */
public class CoffeeSitesInRangeFoundService extends JobIntentService implements PropertyChangeListener,
                                                                       CoffeeSitesInRangeFromServerResultListener {

    public static final int JOB_ID = 1010;

    private static final String TAG = "SitesInRangeUpdateSrv";


    /**
     * Location when the currentSitesInRange where observed
     */
    private LatLng searchLocationOfCurrentSites;

    private int currentSearchRange;
    private String coffeeSort;


    /**
     * CoffeeSites repository to be used in case of OFFLINE mode
     */
    private static CoffeeSiteRepository coffeeSiteRepository;

    /**
     * To detect, that search request to server is running
     */
    private boolean isSearching = false;

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

    private static int[] allWidgetIds;

    /**
     * Used when called from MainAppWidgetProvider by context.startService(intent);
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service invoked from MainAppWidgetProvider: onStartCommand()");
        starWorkOnWidgetRequest(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     *
     * @param context
     * @param work
     */
    public static void enqueueWork(Context context, Intent work) {
        Log.i(TAG, "Service invoked from MainAppWidgetProvider: enqueueWork()");
        enqueueWork(context, CoffeeSitesInRangeFoundService.class, JOB_ID, work);
    }

    /**
     * To detect, if the enque job has been already called
     */
    private Intent theOnlyJobIhave = null;

    /**
     * Called from MainAppWidgetProvider using CoffeeSitesInRangeFoundService.enqueueWork(context, intent);
     *
     * @param intent
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "Service invoked from MainAppWidgetProvider: onHandleWork()");
        if (theOnlyJobIhave == null) {
            theOnlyJobIhave = intent;
            //final int extraValue = theOnlyJobIhave.getIntExtra(intent.KEY, -500);
            //Log.d(TAG, "onHandleWork: " + extraValue);
            try {
                starWorkOnWidgetRequest(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "onHandleWork I'm already busy, refuse to work >:(");
        }
        Log.d(TAG, "onHandleWork end");
        //starWorkOnWidgetRequest(intent);
    }

    /**
     * All work, which should be done, if service is invoked from MainAppWidgetProvider
     */
    private void starWorkOnWidgetRequest(Intent intent) {
        allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        this.currentSearchRange = (int) intent.getExtras().get("searchRange");
        this.coffeeSort = (String) intent.getExtras().get("coffeeSort");
        if (locationService == null && !mShouldUnbind) {
            doBindLocationService();
        } else {
            updateCurrentSitesForWidget();
        }
    }


    /**
     * Cislo vetsi nez 0 a menzi nez 1, vyjadrujici, kdy se ma provest
     * novy dotaz na server pro aktualni CoffeeSites.
     * Pri zmene lokace se vypocita o jakou vzdalenost se telefon posunul
     * a pokud je tato zmenu vetsi jako moveToRangeNewSearchRatio * currentSearchRange
     * pak se posle novy dotaz na server nebo do DB.
     */
    private final double DISTANCE_TO_RANGE_NEW_SEARCH_RATIO = 0.15;

    /**
     * Listeners to listen events of start/stop of coffee sites in range searching
     */
    private final List<CoffeeSitesInRangeSearchOperationListener> sitesInRangeSearchOperationListeners = new ArrayList<>();


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
    private final List<CoffeeSitesInRangeFoundListener> sitesInRangeFoundListeners = new ArrayList<>();

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

    // Location service
    protected static LocationService locationService;
    private static LocationServiceConnector locationServiceConnector;


    public void onLocationServiceConnected() {
        locationService = locationServiceConnector.getLocationService();
        if (locationService != null) {
            locationService.addPropertyChangeListener(this);
            if (searchLocationOfCurrentSites == null) {
                this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
            }
            if (allWidgetIds != null) {
                updateCurrentSitesForWidget();
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (allWidgetIds != null) { // started from Widget using context.startForegroundService(intent);
            startForeground(1, new Notification());
        }
        coffeeSiteRepository = new CoffeeSiteRepository(CoffeeSiteDatabase.getDatabase(getApplicationContext()));

        // Transform CoffeeSites returned from DB (sites within Rectangel) to CoffeeSiteMovable (sites within search circle) expected by FoundCoffeeSiteListActivity
        foundSites = Transformations.map(coffeeSiteRepository.getCoffeeSitesInRange(), coffeeSites -> {
            List<CoffeeSiteMovable> coffeeSiteMovables = new ArrayList<>();
            for (CoffeeSite cs : coffeeSites) { // filters only CoffeeSites in circle range and maps to CoffeeSiteMovable
                if (searchLocationOfCurrentSites != null) {
                    if (Utils.countDistanceMetersFromSearchPoint(cs.getLatitude(), cs.getLongitude(), searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude) <= currentSearchRange) {
                        coffeeSiteMovables.add(new CoffeeSiteMovable(cs, searchLocationOfCurrentSites));
                    }
                }
            }
            return coffeeSiteMovables;
        });

        if (locationService == null && !mShouldUnbind) {
            doBindLocationService();
        }
    }

    private void doBindLocationService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
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
        if (mShouldUnbind && locationService != null) {
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
     * Set new input search data to repository to initiate new DB search.
     *
     * @param latitudeFrom
     * @param longitudeFrom
     * @param searchRange
     */
    private void setInput(double latitudeFrom, double longitudeFrom, int searchRange) {
        coffeeSiteRepository.setNewSearchCriteria(latitudeFrom, longitudeFrom, searchRange);
    }


    /**
     * Main field provided by this service
     */
    private LiveData<List<CoffeeSiteMovable>> foundSites;

    public LiveData<List<CoffeeSiteMovable>> getFoundSites() {
        return foundSites;
    }

    /**
     * Listen to location changes invoked by LocationService.
     * If the move distance overlaps given threshold, search of new CoffeeSites starts.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        long movedDistance = 0;
        if (this.searchLocationOfCurrentSites != null) {
            movedDistance = locationService.getDistanceFromCurrentLocation(this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude);
        }

        if (movedDistance >= DISTANCE_TO_RANGE_NEW_SEARCH_RATIO * currentSearchRange) {
            for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
                listener.onStartSearchingSitesInRange();
            }
            this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();

            if (this.searchLocationOfCurrentSites != null && this.coffeeSort != null) {
                startSearchingSites(this.coffeeSort, this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude, this.currentSearchRange);
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
    public void requestUpdatesOfCurrentSitesInRange(LatLng searchLocationOfCurrentSites, int range, String coffeeSort) {
        this.searchLocationOfCurrentSites = searchLocationOfCurrentSites;
        if (locationService != null) {
            this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
        }
        this.currentSearchRange = range;
        this.coffeeSort = coffeeSort;

        if (this.searchLocationOfCurrentSites != null) {
            startSearchingSites(coffeeSort, this.searchLocationOfCurrentSites.latitude, this.searchLocationOfCurrentSites.longitude, this.currentSearchRange);
            for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
                listener.onStartSearchingSitesInRange();
            }
        }
    }

    private void startSearchingSites(String coffeeSort, double latitude, double longitude, int range) {
        if (!isSearching) {
            if (!Utils.isOfflineModeOn(getApplicationContext())) {
                startSearchSitesInRangeFromServer(coffeeSort, latitude, longitude, range);
            }
            else { // updates LiveData returned from DB
                setInput(latitude, latitude, range);
            }
        }
    }

    /**
     * Calls REST async. task. or requests DB if in OFFLINE mode
     *
     * @param coffeeSort
     */
    private void startSearchSitesInRangeFromServer(String coffeeSort, double latitude, double longitude, int range) {
        isSearching = true;
        new GetCoffeeSitesInRangeAsyncTask(this,
                latitude,
                longitude,
                range,
                coffeeSort).execute();
    }


    private void updateCurrentSitesForWidget() {
        // Get search info, longitude, latitude, range
        // Range taken from Widget settings, or from Widget Intent ?
        if (locationService != null && this.currentSearchRange > 0) {
            LatLng searchLocation = locationService.getCurrentLatLng();
            if (searchLocation != null) {
                if (Utils.isOfflineModeOn(getApplicationContext())) {
                    //TODO - get Single
                    Disposable d = coffeeSiteRepository.getCoffeeSitesInRangeSingle(searchLocation.latitude, searchLocation.longitude, this.currentSearchRange)
                            .delay(10, TimeUnit.SECONDS, Schedulers.io())
                            .subscribeWith(new DisposableSingleObserver<List<CoffeeSite>>() {
                                @Override
                                public void onStart() {
                                    Log.i(TAG, "Start Single request for Widget");
                                }

                                @Override
                                public void onSuccess(@NonNull List<CoffeeSite> coffeeSites) {
                                    //foundSitesForWidget = coffeeSites;
                                    //TODO updateWidget
                                    updateWidget(coffeeSites);
                                    for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
                                        listener.onSearchingSitesInRangeFinished();
                                    }
                                }

                                @Override
                                public void onError(Throwable error) {
                                    Log.e(TAG, "Single request for Widget failed.");
                                    for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
                                        listener.onSearchingSitesInRangeFinished();
                                    }
                                }
                            });

                    d.dispose();
                } else {
                    //TODO Start search from server - Asynchronously After result is here, updateWidget
                    String coffeeSortLoc = this.coffeeSort != null ? this.coffeeSort : "";
                    startSearchSitesInRangeFromServer(coffeeSortLoc, searchLocation.latitude, searchLocation.longitude, this.currentSearchRange);
                }
            }
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
        //TODO - detection, taht service was invoked from Widget. If yes, updateWidget via WidgetManager
        isSearching = false;
        for (CoffeeSitesInRangeFoundListener listener : sitesInRangeFoundListeners) {
            listener.onSitesInRangeFound(coffeeSites);
        }
        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
            listener.onSearchingSitesInRangeFinished();
        }

        updateWidget(coffeeSites);
    }

    @Override
    public void onSitesInRangeReturnedFromServerError(String error) {
        isSearching = false;
        for (CoffeeSitesInRangeSearchOperationListener listener : sitesInRangeSearchOperationListeners) {
            listener.onSearchingSitesInRangeError(error);
            listener.onSearchingSitesInRangeFinished();
        }
    }

    private void updateWidget(List<? extends CoffeeSite> coffeeSites) {
        MainAppWidgetProvider.updateCoffeeSiteWidget(this, coffeeSites);
    }

}