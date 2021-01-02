package cz.fungisoft.coffeecompass2.services;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.JobIntentService;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeFromServerResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.widgets.MainAppWidgetProvider;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Service to obtain CoffeeSites in current search range.
 * <p></>
 * This is variant used by MainAppWidgetProvider only.<br>
 * Extends JobIntentService to call its functionality using enqueueWork() method.
 * Uses either REST API requests from server or DB repository in case of OFFLiNE mode to get
 * requested data about CoffeeSites.<br>
 * <p>
 * Requires using Location service, but as it cannot wait for a long GPS fix, it uses
 * only network location.
 */
public class CoffeeSitesInRangeWidgetService extends JobIntentService
                                             implements CoffeeSitesInRangeFromServerResultListener {

    public static final int JOB_ID = 1010;

    private static final String TAG = "SitesInRangeWidgetSrv";


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

    private int[] allWidgetIds;


    /**
     * Start method of this service to be called from Widget.
     *
     * @param context
     * @param work
     */
    public static void enqueueWork(Context context, Intent work) {
        Log.i(TAG, "Service invoked from MainAppWidgetProvider: enqueueWork()");
        enqueueWork(context, CoffeeSitesInRangeWidgetService.class, JOB_ID, work);
    }

    /**
     * To detect, if the enqueue job has been already called
     */
    private Intent theOnlyJobIhave = null;

    /**
     * Invoked from MainAppWidgetProvider using CoffeeSitesInRangeFoundService.enqueueWork(context, intent);
     * Called by Android system automatically.
     *
     * @param intent
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "Service invoked from MainAppWidgetProvider: onHandleWork()");
        if (theOnlyJobIhave == null) {
            theOnlyJobIhave = intent;
            try {
                starWorkOnWidgetRequest(intent);
            } catch (Exception ex) {
                Log.e(TAG, "onHandleWork() exception during starWorkOnWidgetRequest(). Ex.: " + ex.getMessage());
            }
        } else {
            Log.d(TAG, "onHandleWork I'm already busy, refuse to work >:(");
        }
        Log.d(TAG, "onHandleWork end");
    }

    /**
     * All work, which should be done, if service is invoked from MainAppWidgetProvider
     */
    private void starWorkOnWidgetRequest(Intent intent) {
        coffeeSiteRepository = new CoffeeSiteRepository(CoffeeSiteDatabase.getDatabase(getApplicationContext()));
        this.currentSearchRange = (int) intent.getExtras().get("searchRange");
        this.coffeeSort = (String) intent.getExtras().get("coffeeSort");
        this.allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        if (locationService == null && !mShouldUnbind) {
            doBindLocationService();
        } else {
            updateCurrentSitesForWidget();
        }
    }


    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private static boolean mShouldUnbind;

    // Location service
    protected static LocationService locationService;
    private static LocationServiceConnector locationServiceConnector;


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
            Log.d(TAG, "Location service binded.");
            if (searchLocationOfCurrentSites == null) {
                this.searchLocationOfCurrentSites = locationService.getCurrentLatLng();
            }
            updateCurrentSitesForWidget();
        }
    }


    private void doUnbindLocationService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            unbindService(locationServiceConnector);
            Log.i(TAG, "Location service unbinded.");
            mShouldUnbind = false;
        }
    }

    /**
     * Calls either AsyncTasks for retrieving CoffeeSites from DB or from server.
     */
    private void updateCurrentSitesForWidget() {
        Log.i(TAG, "Updating CoffeeSites for Widget.");
        if (locationService != null && this.currentSearchRange > 0) {
            Log.i(TAG, "Updating CoffeeSites for Widget.");
            LatLng searchLocation = locationService.getCurrentLatLng();
            if (searchLocation != null && !isSearching) {
                Log.i(TAG, "Search location lat.: " + searchLocation.latitude + ", lon.: " + searchLocation.longitude);
                if (Utils.isOfflineModeOn(getApplicationContext())) {
                    startSearchCoffeeSitesInDB(searchLocation);
                } else {
                    String coffeeSortLoc = this.coffeeSort != null ? this.coffeeSort : "";
                    startSearchSitesInRangeFromServer(coffeeSortLoc, searchLocation.latitude, searchLocation.longitude, this.currentSearchRange);
                }
            }
            doUnbindLocationService(); // location service not needed now, can be unbinded now to release resources
        }
    }

    /**
     * Calls REST API Async. task.
     *
     * @param coffeeSort
     */
    private void startSearchSitesInRangeFromServer(String coffeeSort, double latitude, double longitude, int range) {
        isSearching = true;
        Log.i(TAG, "Start Async task for searching on server.");
        new GetCoffeeSitesInRangeAsyncTask(this,
                latitude, longitude,
                range,
                coffeeSort)
                .execute();
    }

    /**
     * A callback method to be called, when there are CoffeeSites in range returned by
     * async. task called by GetSitesInRangeAsyncTask.onPostExecute(result).
     * Compares returned coffeeSites with currentSitesInRange and finds new and old
     * CoffeeSites.
     */
    @Override
    public void onSitesInRangeReturnedFromServer(List<CoffeeSiteMovable> coffeeSites) {
        isSearching = false;
        Log.d(TAG, "Returned search from server. Number of coffee sites found: " + coffeeSites.size());
        updateWidget(coffeeSites, null);
    }

    @Override
    public void onSitesInRangeReturnedFromServerError(String error) {
        isSearching = false;
    }

    /**
     * Updates Widget with CoffeeSites found by this service.
     *
     * @param coffeeSites
     * @param d
     */
    private void updateWidget(List<? extends CoffeeSite> coffeeSites, Disposable d) {
        if (d != null) {
            d.dispose();
        }
        Log.d(TAG, "Updating widget with found coffeeSites.");
        MainAppWidgetProvider.updateCoffeeSiteWidget(this, coffeeSites);
    }

    /* ================ DB request for Offline mode ========================== */

    /**
     * Result of the DB request
     */
    private static List<? extends CoffeeSite> coffeeSitesFromDB;
    /**
     * Disposable of the Single DB request
     */
    private static Disposable d;

    /**
     * Starts GetSingleCoffeeSitesAsyncTask to find CoffeeSites in range from DB.
     *
     * @param searchLocation current search location
     */
    private void startSearchCoffeeSitesInDB(LatLng searchLocation) {
       /*
        Needed to process results of DB search returned as Single in the Main thread
        DB request must run in separate thread, therefore AsyncTask is created,
        but Picasso library (used by Widget) works in Main thread, therefore update of Widget cannot
        be called from AsyncTask from onSuccess() of the DisposableSingleObserver
        */
        final CountDownLatch latch = new CountDownLatch(1);
        isSearching = true;
        Log.i(TAG, "Start Async task for searching in DB.");
        new GetSingleCoffeeSitesAsyncTask(latch, searchLocation, this.currentSearchRange)
                .execute();
        try {
            latch.await(); // wait to finish Async task assignment to coffeeSitesFromDB
            isSearching = false;
            if (coffeeSitesFromDB != null) {
                updateWidget(coffeeSitesFromDB, d);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error waiting for CountDownLatch of DB search.");
        }
    }

    /**
     * Async Task to start and get Single request result from DB.
     */
    private static class GetSingleCoffeeSitesAsyncTask extends AsyncTask<Void, Void, Disposable> {

        private final int range;

        private final LatLng searchLocation;

        private final CountDownLatch latch;

        public GetSingleCoffeeSitesAsyncTask(CountDownLatch latch, LatLng searchLocation, int currentSearchRange) {
            this.latch = latch;
            this.searchLocation = searchLocation;
            this.range = currentSearchRange;
        }

        @Override
        protected Disposable doInBackground(Void... params) {
            d = coffeeSiteRepository.getCoffeeSitesInRangeSingle(searchLocation.latitude, searchLocation.longitude, range)
                    .delay(10, TimeUnit.MILLISECONDS, Schedulers.io())
                    .subscribeWith(new DisposableSingleObserver<List<CoffeeSite>>() {

                        @Override
                        public void onStart() {
                            Log.i(TAG, "Start DB Single request for Widget");
                        }

                        @Override
                        public void onSuccess(@NonNull List<CoffeeSite> coffeeSites) {
                            Log.i(TAG, "DB Single onSuccess()");
                            List<CoffeeSiteMovable> coffeeSiteMovables = new ArrayList<>();
                            for (CoffeeSite cs : coffeeSites) { // filters only CoffeeSites in circle range and maps to CoffeeSiteMovable
                                if (Utils.countDistanceMetersFromSearchPoint(cs.getLatitude(), cs.getLongitude(), searchLocation.latitude, searchLocation.longitude) <= range) {
                                    coffeeSiteMovables.add(new CoffeeSiteMovable(cs, searchLocation));
                                }
                            }
                            coffeeSitesFromDB = coffeeSiteMovables;
                            latch.countDown();
                        }

                        @Override
                        public void onError(Throwable error) {
                            Log.e(TAG, "DB Single request for Widget failed. Error: " + error.getMessage());
                            latch.countDown();
                        }
                    });

            return d;
        }
    }

}