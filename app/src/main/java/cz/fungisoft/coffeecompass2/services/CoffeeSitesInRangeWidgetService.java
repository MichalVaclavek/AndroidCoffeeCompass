package cz.fungisoft.coffeecompass2.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.JobIntentService;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.R;
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
 * This is variant used by {@link MainAppWidgetProvider} only.<br>
 * Extends JobIntentService to call its functionality using enqueueWork() method.
 * Uses either REST API requests from server or DB repository in case of OFFLiNE mode to get
 * requested data about CoffeeSites.<br>
 * <p>
 * Requires using Location service.
 */
public class CoffeeSitesInRangeWidgetService extends JobIntentService
                                             implements CoffeeSitesInRangeFromServerResultListener,
                                                        PropertyChangeListener {

    public static final int JOB_ID = 1010;

    public static final int NOTIFICATION_ID = 121234;

    private static final long MAX_STARI_DAT = 1000 * 300; // pokud jsou posledni zname udaje o poloze starsi jako 5 minuty, zjistit nove
    private static final float LAST_PRESNOST = 100.0f; // pokud je posledni presnosy polohy horsi, zkus pockat na lepsi

    private static final String TAG = "SitesInRangeWidgetSrv";

    private static final long GET_LOCATION_WAITING_TIME = 40_000L; // wait max. 40 secs to get right location


    /**
     * Location when the currentSitesInRange where observed
     */
    private static LatLng searchLocationOfCurrentSites;

    /**
     * To indicate, that searching for location is still ongoing after  locationService.getPosledniPozice()
     * and service waits for locationService's propertyChange for better location accuracy.
     */
    private static boolean locationSearchFinished = false;

    private int currentSearchRange;
    private String coffeeSort;


    /**
     * CoffeeSites repository to be used in case of OFFLINE mode
     */
    private static CoffeeSiteRepository coffeeSiteRepository;

    /**
     * To detect that search request to server is running
     */
    private boolean isSearchingSites = false;

    private int[] allWidgetIds; // currently not used

    /**
     * Indicate that service was invoked by user click on refresh icon.
     * used to show/not show Toast informing about ongoing searching for nearest site.
     */
    private Boolean serviceInvokedByUser = false;

    /**
     * Called for Android API >= 26 using context.startForegroundService(sitesInRangeServiceIntent);
     * from MainAppWidgetProvider
     */
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        prepareAndStartForeground();
        starWorkOnWidgetRequest(intent);
        return START_STICKY;
    }

    /**
     * Method to prepare this servie to as a Foreground service. Needed for Android API >= 26
     * For lower API the enqueue() method is used to invoke this service action.
     */
    private void prepareAndStartForeground() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Start Widget update", NotificationManager.IMPORTANCE_LOW);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            //Intent notificationIntent = new Intent(this, FoundCoffeeSitesListActivity.class); 
            Intent notificationIntent = new Intent(this, CoffeeSitesInRangeWidgetService.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.widget_notification_title))
                .setContentText(getText(R.string.widget_notification_message))
                .setSmallIcon(R.drawable.cup_rating_full_3)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.widget_ticker_text))
                .build();

            startForeground(NOTIFICATION_ID, notification);
        }
    }


    /**
     * Start method of this service to be called from Widget
     * ( CoffeeSitesInRangeWidgetService.enqueueWork(context, sitesInRangeServiceIntent); )
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
        this.serviceInvokedByUser = (Boolean) intent.getExtras().get("serviceInvokedByUser");

        isSearchingSites = false; // reset
        doBindLocationService();
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
            startLocationFoundTimerTask();
            locationService.addPropertyChangeListener(this);
            Log.d(TAG, "Location service binded.");
            Location lastLocation =  locationService.getPosledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
            locationSearchFinished = false;
            if (lastLocation != null) {
                if (serviceInvokedByUser) {
                    Toast.makeText(this, R.string.widget_toast_searching_coffee, Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "Last location found.");
                searchLocationOfCurrentSites = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                Log.d(TAG, "Last location accuracy: " + lastLocation.getAccuracy());
                if (lastLocation.getAccuracy() < LAST_PRESNOST) {
                    locationSearchFinished = true;
                }
                if (!isSearchingSites) {
                    startSearchCurrentSitesForWidget();
                }
            }
        }

    }


    private void doUnbindLocationService() {
        if (mShouldUnbind) {
            locationService.removePropertyChangeListener(this);
            // Release information about the service's state.
            unbindService(locationServiceConnector);
            Log.i(TAG, "Location service unbinded.");
            mShouldUnbind = false;
        }
    }


    /**
     * Calls either AsyncTask retrieving CoffeeSites from DB or AsyncTask retrieving CoffeeSites from server.
     */
    private void startSearchCurrentSitesForWidget() {
        Log.i(TAG, "Start calling Async tasks for updating CoffeeSites for Widget.");
        if (searchLocationOfCurrentSites != null) {
            Log.i(TAG, "Search location lat.: " + searchLocationOfCurrentSites.latitude + ", lon.: " + searchLocationOfCurrentSites.longitude);
            if (Utils.isOfflineModeOn(getApplicationContext())) {
                startSearchCoffeeSitesInDB(searchLocationOfCurrentSites, currentSearchRange);
            } else {
                String coffeeSortLoc = this.coffeeSort != null ? this.coffeeSort : "";
                startSearchSitesInRangeFromServer(coffeeSortLoc, searchLocationOfCurrentSites.latitude, searchLocationOfCurrentSites.longitude, this.currentSearchRange);
            }
        }
    }

    /**
     * Calls REST API Async. task.
     *
     * @param coffeeSort
     */
    private void startSearchSitesInRangeFromServer(String coffeeSort, double latitude, double longitude, int range) {
        isSearchingSites = true;
        Log.i(TAG, "Start Async task for searching on server.");
        new GetCoffeeSitesInRangeAsyncTask(this,
                latitude, longitude,
                range,
                coffeeSort).execute();
    }

    /**
     * A callback method to be called, when there are CoffeeSites in range returned by
     * GetSitesInRangeAsyncTask (called in GetSitesInRangeAsyncTask.doInBackground().)
     */
    @Override
    public void onSitesInRangeReturnedFromServer(List<CoffeeSiteMovable> coffeeSites) {
        isSearchingSites = false;
        Log.d(TAG, "Returned search from server. Number of coffee sites found: " + coffeeSites.size());
        Collections.sort(coffeeSites);
        updateWidget(coffeeSites, null);
    }

    @Override
    public void onSitesInRangeReturnedFromServerError(String error) {
        isSearchingSites = false;
    }

    /**
     * Updates Widget with CoffeeSites found by this service.
     * Also releases resources if used (Disposable and/or timer) and finishes the service (for API < 26)
     *
     * @param coffeeSites
     * @param d
     */
    private void updateWidget(List<? extends CoffeeSite> coffeeSites, Disposable d) {
        if (d != null) {
            d.dispose();
        }

        if ((locationFoundTimer != null) && locationSearchFinished) {
            locationFoundTimer.cancel();
            Log.d(TAG, "Timer cancelled.");
        }

        Log.d(TAG, "Updating widget with found coffeeSites.");
        MainAppWidgetProvider.updateCoffeeSiteWidget(this, coffeeSites, locationSearchFinished);

        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) && locationSearchFinished ) { // was started using enqueue()
            stopSelf();
        }
    }

    /**
     * Wait for change of location before performing service's job ... to get actual location.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Log.d(TAG, "Location property changed.");
        if (locationService != null) {
            searchLocationOfCurrentSites = locationService.getCurrentLatLng();
            if (searchLocationOfCurrentSites != null ) {
                locationSearchFinished = true;
                if (!isSearchingSites) {
                    startSearchCurrentSitesForWidget();
                }
            }
        }
    }

    private Timer locationFoundTimer;

    /**
     * Starts TimerTask to check, if searchingFinished is done. If not,
     * service is finished
     */
    private void startLocationFoundTimerTask() {
        TimerTask task = new TimerTask() {
            public void run() {
                Log.d(TAG, "Timer task performed on: " + new Date() + ". Thread's name: " + Thread.currentThread().getName());
                locationSearchFinished = true;
                updateWidget(null, null);
            }
        };

        locationFoundTimer = new Timer("Location_search_finished_timer");
        Log.d(TAG, "Timer task scheduled.");
        locationFoundTimer.schedule(task, GET_LOCATION_WAITING_TIME);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "Unbinding Location service.");
        locationService.removePropertyChangeListener(this);
        doUnbindLocationService();
        super.onDestroy();
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
    private void startSearchCoffeeSitesInDB(LatLng searchLocation, int range) {
       /*
        Needed to process results of DB search returned as Single in the Main thread
        DB request must run in separate thread, therefore AsyncTask is created,
        but Picasso library (used by Widget) works in Main thread, therefore update of Widget cannot
        be called from AsyncTask from onSuccess() of the DisposableSingleObserver
        */
        final CountDownLatch latch = new CountDownLatch(1);
        isSearchingSites = true;
        Log.i(TAG, "Start Async task for searching in DB.");
        new GetSingleCoffeeSitesAsyncTask(latch, searchLocation, range)
                .execute();
        try {
            latch.await(); // wait to finish Async task assignment to coffeeSitesFromDB
            isSearchingSites = false;
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
                            Collections.sort(coffeeSiteMovables);
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