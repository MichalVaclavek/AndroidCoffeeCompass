package cz.fungisoft.coffeecompass2.services;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITES_IN_TOWN;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCfSitesFromLoggedUserPaginatedAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSiteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesFromCurrentUserAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesInTownAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetLatestCoffeeSitesAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetNumberOfCoffeeSitesFromCurrentUserAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteNumbersRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Service to perform CoffeeSite load operations i.e.: downloading of one CoffeeSite,
 * downloading of all CoffeeSites created by user and so on.<br>
 * <p>
 * It has to listen some interface, especially those called by AsyncTasks when REST
 * calls are performed to load CoffeeSites.
 */
public class CoffeeSiteLoadOperationsService extends CoffeeSiteWithUserAccountService
                                             implements CoffeeSiteRESTResultListener,
                                                        CoffeeSitesRESTResultListener,
                                                        CoffeeSiteNumbersRESTResultListener {

    static final String TAG = "CoffeeSiteLoadService";


    /**
     * To detect, that search request to server is running
     */
    private boolean isSearching = false;

    /**
     * To keep town name when searching for CoffeeSites in town
     */
    private String townName;


    /**
     * CoffeeSites repository to be used in case of OFFLINE mode
     */
    private static CoffeeSiteRepository coffeeSiteRepository;

    // Listeners, usually Activities, which called respective service method
    // and wants to be informed about result later, as all the operations are Async
    private final List<CoffeeSiteLoadServiceOperationsListener> loadOperationsListeners = new ArrayList<>();

    public void addLoadOperationsListener(CoffeeSiteLoadServiceOperationsListener listener) {
        if (!loadOperationsListeners.contains(listener)) {
            loadOperationsListeners.add(listener);
        }
    }

    public void removeLoadOperationsListener(CoffeeSiteLoadServiceOperationsListener listener) {
        loadOperationsListeners.remove(listener);
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSiteLoadOperationsService.LocalBinder();


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends CoffeeSiteWithUserAccountService.LocalBinder {

        CoffeeSiteLoadOperationsService getService() {
            return CoffeeSiteLoadOperationsService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        coffeeSiteRepository = new CoffeeSiteRepository(CoffeeSiteDatabase.getDatabase(getApplicationContext()));
        Log.d(TAG, "Service started.");
    }


    @Override
    public void onNumberOfCoffeeSitesReturned(CoffeeSiteRESTOper oper, Result<Integer> result) {
        if (result instanceof Result.Success) {
            int coffeeSitesNumber = ((Result.Success<Integer>) result).getData();
            if (oper == CoffeeSiteRESTOper.COFFEE_SITES_NUMBER_FROM_CURRENT_USER) {
                informClientAboutNumberOfCoffeeSitesFromLoggedInUser(oper, coffeeSitesNumber, "");
            }
        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                informClientAboutNumberOfCoffeeSitesFromLoggedInUser(oper, 0, error.getDetail());
                Log.e(TAG, "Error when obtaining number of coffee sites from user. " + error.getDetail());
            }
        }
    }


    @Override
    public void onCoffeeSiteReturned(CoffeeSiteRESTOper oper, Result<CoffeeSite> result) {
        CoffeeSite coffeeSite;
        if (result instanceof Result.Success) {
            coffeeSite = ((Result.Success<CoffeeSite>) result).getData();
            switch (oper) {
                case COFFEE_SITE_LOAD: informClientAboutLoadedCoffeeSite(coffeeSite, "");
                   break;
            }

        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                informClientAboutLoadedCoffeeSite(null, error.getDetail());
                Log.e(TAG, "Error when obtaining coffee site. " + error.getDetail());
            }
        }
    }

    @Override
    public void onCoffeeSitesReturned(CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result) {

        if (result instanceof Result.Success) {
            if (((Result.Success<List<CoffeeSite>>) result).getData() instanceof List<?>) {
                List<CoffeeSite> coffeeSites = ((Result.Success<List<CoffeeSite>>) result).getData();

                switch (oper) {
                    case COFFEE_SITES_FROM_USER_LOAD:
                    case COFFEE_SITE_LOAD_ALL:
                    case COFFEE_SITES_FROM_CURRENT_USER_LOAD:
                    case COFFEE_SITES_LOAD_LATEST:
                    case COFFEE_SITES_IN_TOWN:
                        informClientAboutLoadedCoffeeSitesList(oper, coffeeSites, "");
                        break;
                }
            }
            if (((Result.Success<List<CoffeeSite>>) result).getData() instanceof CoffeeSitePageEnvelope) {
                CoffeeSitePageEnvelope coffeeSitePage =  ((Result.Success<CoffeeSitePageEnvelope>) result).getData();

                switch (oper) {
                    case COFFEE_SITES_FROM_CURRENT_USER_NEXT_PAGE_LOAD:
                    case COFFEE_SITES_FROM_CURRENT_USER_FIRST_PAGE_LOAD:
                        informClientAboutLoadedCoffeeSitesPage(oper, coffeeSitePage, "");
                        break;
                }
            }

        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                Log.e(TAG, "Error when obtaining coffee sites." + error.getDetail());
                if (oper == COFFEE_SITES_IN_TOWN) {
                    informClientAboutLoadedCoffeeSitesList(oper, null, getString(R.string.no_site_in_town, this.townName));
                } else {
                    informClientAboutLoadedCoffeeSitesList(oper, null, error.getDetail());
                }
            }
        }
    }

    public void findAllCoffeeSitesByUserId(long userId) {
        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITES_FROM_USER_LOAD;
        if (currentUser != null) {
            new GetCoffeeSitesFromCurrentUserAsyncTask(requestedRESTOperation, currentUser, this).execute();
        } else {
            Log.w(TAG, "Current user is null. Cannot execute GetCoffeeSitesFromCurrentUserAsyncTask.execute()");
        }
    }

    public void findNumberOfCoffeeSitesFromCurrentUser() {
        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITES_NUMBER_FROM_CURRENT_USER;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new GetNumberOfCoffeeSitesFromCurrentUserAsyncTask(requestedRESTOperation, currentUser, this).execute();
        } else {
            Log.w(TAG, "Current user is null. Cannot execute GetNumberOfCoffeeSitesFromCurrentUserAsyncTask.execute()");
        }
    }

    public void getCoffeeSitesActivatedLastDays(int daysBack) {
        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITES_LOAD_LATEST;
        new GetLatestCoffeeSitesAsyncTask(requestedRESTOperation, this, daysBack).execute();
    }

    public void getCoffeeSitesInTown(String townName) {
        this.townName = townName;
        startSearchingSitesInTown(townName);
    }

    public void findAllCoffeeSitesFromCurrentUser() {
        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITES_FROM_CURRENT_USER_LOAD;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new GetCoffeeSitesFromCurrentUserAsyncTask(requestedRESTOperation, currentUser, this).execute();
        } else {
            Log.w(TAG, "Current user is null. Cannot execute GetNumberOfCoffeeSitesFromCurrentUserAsyncTask.execute()");
        }
    }

    public void findCoffeeSitesPageFromCurrentUser(int pageNumber, int pageSize) {
        if (pageNumber == 1) {
            requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITES_FROM_CURRENT_USER_FIRST_PAGE_LOAD;
        } else if (pageNumber > 1) {
            requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITES_FROM_CURRENT_USER_NEXT_PAGE_LOAD;
        } else {
            return;
        }
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new GetCfSitesFromLoggedUserPaginatedAsyncTask(requestedRESTOperation, pageNumber, pageSize, currentUser, this).execute();
        } else {
            Log.w(TAG, "Current user is null. Cannot execute GetNumberOfCoffeeSitesFromCurrentUserAsyncTask.execute()");
        }
    }

    public void findCoffeeSiteById(long coffeeSiteId) {
        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITE_LOAD;
        new GetCoffeeSiteAsyncTask(requestedRESTOperation,this, coffeeSiteId, "").execute();
    }

    public void findCoffeeSiteByURL(String coffeeSiteURL) {
        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITE_LOAD;
        new GetCoffeeSiteAsyncTask(requestedRESTOperation,this, 0, coffeeSiteURL).execute();
    }


    /**
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutLoadedCoffeeSite(CoffeeSite locCoffeeSite, String error) {
        for (CoffeeSiteLoadServiceOperationsListener listener : loadOperationsListeners) {
            listener.onCoffeeSiteLoaded(locCoffeeSite, error);
        }
    }

    /**
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutLoadedCoffeeSitesList(CoffeeSiteRESTOper operation, List<CoffeeSite> coffeeSites, String error) {
        for (CoffeeSiteLoadServiceOperationsListener listener : loadOperationsListeners) {
            switch (operation) {
                case COFFEE_SITES_FROM_USER_LOAD: listener.onCoffeeSiteListFromUserLoaded(coffeeSites, error);
                    break;
                case COFFEE_SITES_FROM_CURRENT_USER_LOAD: listener.onCoffeeSiteListFromLoggedInUserLoaded(coffeeSites, error);
                    break;
                case COFFEE_SITE_LOAD_ALL_FROM_RANGE: listener.onCoffeeSitesFromRangeLoaded(coffeeSites, error);
                    break;
                case COFFEE_SITE_LOAD_ALL: listener.onAllCoffeeSitesLoaded(coffeeSites, error);
                    break;
                case COFFEE_SITES_LOAD_LATEST: listener.onLatestCoffeeSitesLoaded(coffeeSites, error);
                    break;
                case COFFEE_SITES_IN_TOWN: listener.onCoffeeSitesInTownLoaded(coffeeSites, error);
                    break;
                default: break;
            }
        }
    }

    private void informClientAboutLoadedCoffeeSitesPage(CoffeeSiteRESTOper operation, CoffeeSitePageEnvelope coffeeSites, String error) {
        for (CoffeeSiteLoadServiceOperationsListener listener : loadOperationsListeners) {
            switch (operation) {
                case COFFEE_SITES_FROM_CURRENT_USER_FIRST_PAGE_LOAD: listener.onCoffeeSiteFirstPageFromLoggedInUserLoaded(coffeeSites, error);
                    break;
                case COFFEE_SITES_FROM_CURRENT_USER_NEXT_PAGE_LOAD: listener.onCoffeeSiteNextPageFromLoggedInUserLoaded(coffeeSites, error);
                    break;
                default: break;
            }
        }
    }

    /**
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutNumberOfCoffeeSitesFromLoggedInUser(CoffeeSiteRESTOper operation, int coffeeSitesNumber, String error) {
        for (CoffeeSiteLoadServiceOperationsListener listener : loadOperationsListeners) {
            switch (operation) {
                case COFFEE_SITES_NUMBER_FROM_CURRENT_USER: listener.onNumberOfCoffeeSiteFromLoggedInUserLoaded(coffeeSitesNumber, error);
                    break;
                default: break;
            }
        }
    }

    /**
     * Starts request for CoffeeSites in town from server (REST API) or starts Async
     *
     * @param town
     */
    public void startSearchingSitesInTown(String town) {
        if (!Utils.isOfflineModeOn(getApplicationContext())) {
            startSearchSitesInTownFromServer(town);
        }
        else { // updates LiveData returned from DB
            startSearchCoffeeSitesInTownFromDB(town);
        }
    }

    /**
     * Calls REST async. task.
     *
     * @param town
     */
    private void startSearchSitesInTownFromServer(String town) {
        isSearching = true;
        Log.i(TAG, "Start Async task for searching on server.");
        new GetCoffeeSitesInTownAsyncTask(this, COFFEE_SITES_IN_TOWN, town).execute();
    }

    /**
     * Result of the DB request
     */
    private static List<CoffeeSite> coffeeSitesInTownFromDB;

    /**
     * Disposable of the Single DB request
     */
    private static Disposable d;

    private static String readDBError = "";

    /**
     * Starts GetSingleCoffeeSitesInTownAsyncTask to find CoffeeSites in town from DB.
     *
     * @param townName town to be searched for CoffeeSites
     */
    private void startSearchCoffeeSitesInTownFromDB(String townName) {
       /*
        Needed to process results of DB search returned as Single in the Main thread
        DB request must run in separate thread, therefore AsyncTask is created,
        but Picasso library (used by Widget) works in Main thread, therefore update of Widget cannot
        be called from AsyncTask from onSuccess() of the DisposableSingleObserver
        */
        final CountDownLatch latch = new CountDownLatch(1);
        isSearching = true;
        Log.i(TAG, "Start Async task for searching in DB.");
        new GetSingleCoffeeSitesInTownAsyncTask(latch, townName)
                .execute();
        try {
            latch.await(); // wait to finish Async task assignment to coffeeSitesFromDB
            isSearching = false;
            if (coffeeSitesInTownFromDB != null) {
                // Process result
                informClientAboutLoadedCoffeeSitesList(COFFEE_SITES_IN_TOWN, coffeeSitesInTownFromDB, readDBError);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error waiting for CountDownLatch of DB search.");
        }
    }

    /**
     * Async Task to start and get Single request result from DB.
     */
    private static class GetSingleCoffeeSitesInTownAsyncTask extends AsyncTask<Void, Void, Disposable> {

        private final String townName;

        private final CountDownLatch latch;

        public GetSingleCoffeeSitesInTownAsyncTask(CountDownLatch latch, String townName) {
            this.townName = townName;
            this.latch = latch;
        }

        @Override
        protected Disposable doInBackground(Void... params) {
            d = coffeeSiteRepository.getCoffeeSitesInTownSingle(townName)
                    .delay(10, TimeUnit.MILLISECONDS, Schedulers.io())
                    .subscribeWith(new DisposableSingleObserver<List<CoffeeSite>>() {

                        @Override
                        public void onStart() {
                            Log.i(TAG, "Start DB Single request for sites in town: " + townName);
                        }

                        @Override
                        public void onSuccess(@NonNull List<CoffeeSite> coffeeSites) {
                            Log.i(TAG, "DB Single onSuccess()");
                            coffeeSitesInTownFromDB = coffeeSites;
                            latch.countDown();
                        }

                        @Override
                        public void onError(Throwable error) {
                            Log.e(TAG, "DB Single request failed. Error: " + error.getMessage());
                            coffeeSitesInTownFromDB = null;
                            readDBError = "DB Single request failed. Error: " + error.getMessage();
                            latch.countDown();
                        }
                    });

            return d;
        }
    }


    @Override
    public void onDestroy() {
        if (d != null) {
            d.dispose();
        }
        super.onDestroy();
    }

}
