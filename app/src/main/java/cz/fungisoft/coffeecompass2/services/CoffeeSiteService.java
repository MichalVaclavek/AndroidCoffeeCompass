package cz.fungisoft.coffeecompass2.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ChangeStatusOfCoffeeSiteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.CoffeeSiteCUDOperationsAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSiteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesFromCurrentUserAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetNumberOfCoffeeSitesFromCurrentUserAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ReadCoffeeSiteEntitiesAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;


/**
 * Class to provide operations needed for creating, saving, updating, and
 * deleting CoffeeSite instances.
 * It also allowes to change CoffeeSite status (Activate, Deactivate, Cancel)
 * and loading of CoffeeSiteEntities needed to build CoffeeSite instance to be saved/updated
 * via REST API.
 * Mostly it contains all operations needed for comunication with coffeecompass.cz
 * server API.
 * All requested REST api calls are done using AsynTask, except obtaining CoffeeSiteEntities,
 * which is called directly from this service.
 */
public class CoffeeSiteService extends IntentService implements UserAccountServiceConnectionListener {

    static final String REQ_COFFEESITE_TAG = "CoffeeSiteService";

    /**
     * Druhy akci, ktere bude tato trida provadet
     */

    public final static String COFFEE_SITE_ENTITY = "coffee_site_entity";
    // Jednotlive operace s danymi druhy objektu, ktere umi trida provadet
    public final static int COFFEE_SITE_ENTITIES_LOAD = 10;

    /**
     * Operations related to saving/updating/deleting of CoffeeSite
     */
    public final static String COFFEE_SITE_OPERATION = "coffee_site_operation";
    public final static int COFFEE_SITE_SAVE = 20;
    //public final static int COFFEE_SITE_SAVE_AND_ACTIVATE = 21;
    public final static int COFFEE_SITE_UPDATE = 30;
    //public final static int COFFEE_SITE_UPDATE_AND_ACTIVATE = 31; // ?? is it really needed ??
    public final static int COFFEE_SITE_DELETE = 40;

    /**
     * Operations related to changing status of CoffeeSite record
     */
    public final static String COFFEE_SITE_STATUS = "coffee_site_status";
    public final static int COFFEE_SITE_CANCEL = 50;
    public final static int COFFEE_SITE_ACTIVATE = 51;
    public final static int COFFEE_SITE_DEACTIVATE = 52;

    /**
     * Operations related to loading of CoffeeSites or obtaining info about User and his/her dites
     */
    public final static String COFFEE_SITE_LOADING = "coffee_site_loading";
    public final static int COFFEE_SITE_LOAD = 60;
    public final static int COFFEE_SITES_FROM_USER_LOAD = 61;
    public final static int COFFEE_SITES_FROM_CURRENT_USER_LOAD = 62;
    public final static int COFFEE_SITES_NUMBER_FROM_CURRENT_USER = 63;

    /**
     * Current CoffeeSite which is used for server operations save, update, activate and so on
     */
    private CoffeeSite coffeeSite;

    /**
     * CoffeeSite id which is requested to be loaded from server
     */
    private int coffeeSiteId = 0;

    /**
     * Current logged-in user
     */
    private LoggedInUser currentUser;

    private static UserAccountService userAccountService;
    private static UserAccountServiceConnector userAccountServiceConnector;

    private static CoffeeSiteEntitiesRepository entitiesRepository;

    public CoffeeSiteEntitiesRepository getEntitiesRepository() {
        return entitiesRepository;
    }

    /**
     * Operation name type requested by client Activity when registering the service
     */
    private int requestedOperation = 0;

    private String operationResult = "";
    private String operationError = "";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CoffeeSiteService(String name) {
        super(name);
        entitiesRepository = CoffeeSiteEntitiesRepository.getInstance();
        Log.i("CoffeeSiteService", "Constructor start doBindUserAccountService()");
    }


    public CoffeeSiteService() {
        this("CoffeeSiteService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        doBindUserAccountService();

        Log.i(REQ_COFFEESITE_TAG, "OnHandle intent start");
        requestedOperation = intent.getIntExtra("operation_type", 0);
        coffeeSite = (CoffeeSite) intent.getParcelableExtra("coffeeSite");

        coffeeSiteId =  intent.getIntExtra("coffeeSiteId", 0);

        if (userAccountService == null) {
            /* onHandleIntent has lost the race with onServiceConnected
             * so wait this service Thread for 10 ms to allow finish onServiceConnected()
             * //TODO is there a better solution? ... Not found, yet
             */
            try { Thread.sleep(123);  } catch (InterruptedException e) {
                Log.e(REQ_COFFEESITE_TAG, "OnHandleIntent(): sleep() failure.");
            }
        }

        switch (requestedOperation) {
            case COFFEE_SITE_ENTITIES_LOAD: {
                Log.i("OnHandle intent", "readAndSaveAllEntitiesFromServer()");
                readAndSaveAllEntitiesFromServer();
            } break;
            case COFFEE_SITE_SAVE: {
                Log.i("OnHandle intent", "save");
                save(coffeeSite);
            } break;
            case COFFEE_SITE_UPDATE: {
                update(coffeeSite);
            } break;
            case COFFEE_SITE_DELETE: {
                delete(coffeeSite);
            } break;
            case COFFEE_SITE_CANCEL: {
                cancel(coffeeSite);
            } break;
            case COFFEE_SITE_ACTIVATE: {
                activate(coffeeSite);
            } break;
            case COFFEE_SITE_DEACTIVATE: {
                deactivate(coffeeSite);
            } break;
            case COFFEE_SITE_LOAD: {
                if (coffeeSiteId != 0) {
                    findCoffeeSiteById(coffeeSiteId);
                }
            } break;
            case COFFEE_SITES_FROM_CURRENT_USER_LOAD: {
                findAllCoffeeSitesFromCurrentUser();
            } break;
            case COFFEE_SITES_NUMBER_FROM_CURRENT_USER: {
                findNumberOfCoffeeSitesFromCurrentUser();
            } break;
            case COFFEE_SITES_FROM_USER_LOAD: {
                findAllCoffeeSitesByUserId(currentUser.getUserId());
            } break;
            default: break;
        }
    }

    /**
     * Callback methods to be called by AsyncTask when finished
     **/

    /**
     * Sending result of loading all CoffeeSiteEntities load requestedOperation.
     * Called from Retrofit callback function
     */
    public void sendLoadEntitiesOperationResultToClient(String result, String error) {
        operationResult = result;
        operationError = error;

        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE_ENTITY);
        intent.putExtra("operationType", requestedOperation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Collects AsyncOperation result for Create, Update and Delete operations
     * and calls method to inform client Activity about results
     * Can also call another asyncTask if needed because of requested requestedOperation
     */
    public void sendCoffeeSiteCUDOperationResultToClient(CoffeeSite coffeeSite, CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION restOperation, String result, String error) {
        Log.i(REQ_COFFEESITE_TAG, "Sent requestedOperation Async task result to client: start");
        operationResult = result;
        operationError = error;
        this.coffeeSite = coffeeSite;

        // In all other cases, inform client about task finish and it's result
        informClientAboutCoffeeSiteOperationResult();

        Log.i(REQ_COFFEESITE_TAG, "Sent requestedOperation Async task result to client: end");
    }

    // Collects AsyncOperation result and calls method to inform client Activity about results
    public void sendCoffeeSiteStatusChangeResultToClient(CoffeeSite coffeeSite, ChangeStatusOfCoffeeSiteAsyncTask.SITE_STATUS_ASYNC_REST_OPERATION status, String result, String error) {
        Log.i(REQ_COFFEESITE_TAG, "Sent status change Async task result to client: start");
        operationResult = result;
        operationError = error;
        this.coffeeSite = coffeeSite;
        informClientAboutCoffeeSiteStatusChangeResult();
        Log.i(REQ_COFFEESITE_TAG, "Sent status change Async task result to client: end");
    }

    /**
     * One CoffeeSite load from server requestedOperation result
     * @param result
     * @param error
     */
    public void sendCoffeeSiteLoadResultToClient(CoffeeSite locCoffeeSite, String result, String error) {
        operationResult = result;
        operationError = error;
        informClientAboutLoadedCoffeeSite(locCoffeeSite);
    }

    /**
     * One CoffeeSite load from server requestedOperation result
     * @param result
     * @param error
     */
    public void sendCoffeeSitesFromUserLoadResultToClient(List<CoffeeSite> coffeeSites, String result, String error) {
        operationResult = result;
        operationError = error;
        informClientAboutLoadedCoffeeSitesList(coffeeSites);
    }

    public void sendCoffeeSitesFromUserNumberResultToClient(int coffeeSitesNumber, String result, String error) {
        operationResult = result;
        operationError = error;
        informClientAboutNumberOfCoffeeSitesFromLoggedInUser(coffeeSitesNumber);
    }


    /**
     * Informs calling Activity about requested action result via Intent
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutCoffeeSiteOperationResult() {
        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE_OPERATION);
        intent.putExtra("operationType", requestedOperation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
        intent.putExtra("coffeeSite", (Parcelable) coffeeSite);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Expects that operationResult and operationError are set by
     * methods calling this method.
     * Currently the method does the same as informClientAboutCoffeeSiteOperationResult90,
     * but it's better to keep i separately as a different behaviour could
     * be better implemented in case of CoffeeSite status changes.
     */
    private void informClientAboutCoffeeSiteStatusChangeResult() {
        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE_STATUS);
        intent.putExtra("operationType", requestedOperation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
        intent.putExtra("coffeeSite", (Parcelable) coffeeSite);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutLoadedCoffeeSite(CoffeeSite locCoffeeSite) {
        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE_LOADING);
        intent.putExtra("operationType", requestedOperation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
        if (locCoffeeSite != null) {
            intent.putExtra("coffeeSite", (Parcelable) locCoffeeSite);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutLoadedCoffeeSitesList(List<CoffeeSite> coffeeSites) {
        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE_LOADING);
        intent.putExtra("operationType", requestedOperation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
        if (coffeeSites != null) {
            intent.putParcelableArrayListExtra("coffeeSitesList", (ArrayList<? extends Parcelable>) coffeeSites);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutNumberOfCoffeeSitesFromLoggedInUser(int coffeeSitesNumber) {
        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE_LOADING);
        intent.putExtra("operationType", requestedOperation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
        intent.putExtra("coffeeSitesNumber", coffeeSitesNumber);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Methods to start running AsyncTask
     **/

    private void readAndSaveAllEntitiesFromServer() {
        new ReadCoffeeSiteEntitiesAsyncTask(this, entitiesRepository).execute();
    }

     /**
     * @param coffeeSite
     */
    private void save(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setCreatedByUserName(currentUser.getUserName());
            new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.CREATE, coffeeSite, currentUser,
                    this).execute();
        }
    }

    private void saveAndActivate(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setCreatedByUserName(currentUser.getUserName());
            new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.CREATE, coffeeSite, currentUser,
                    this).execute();
        }
    }

    private void update(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setLastEditUserName(currentUser.getUserName());
            new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.UPDATE, coffeeSite, currentUser,
                    this).execute();
        }
    }

    /**
     * Curerntly same as update as this combination is probably not applicable
     * @param coffeeSite
     */
    private void updateAndActivate(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setLastEditUserName(currentUser.getUserName());
            new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.UPDATE, coffeeSite, currentUser,
                    this).execute();
        }
    }

    private void delete(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.DELETE, coffeeSite, currentUser,
                    this).execute();
        }
    }

    private void activate(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new ChangeStatusOfCoffeeSiteAsyncTask(ChangeStatusOfCoffeeSiteAsyncTask.SITE_STATUS_ASYNC_REST_OPERATION.ACTIVATE, coffeeSite, currentUser,
                    this).execute();
        }
    }

    private void deactivate(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new ChangeStatusOfCoffeeSiteAsyncTask(ChangeStatusOfCoffeeSiteAsyncTask.SITE_STATUS_ASYNC_REST_OPERATION.DEACTIVATE, coffeeSite, currentUser,
                    this).execute();
        }
    }

    private void cancel(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new ChangeStatusOfCoffeeSiteAsyncTask(ChangeStatusOfCoffeeSiteAsyncTask.SITE_STATUS_ASYNC_REST_OPERATION.CANCEL, coffeeSite, currentUser,
                    this).execute();
        }
    }

    private void findAllCoffeeSitesByUserId(long userId) {
        //new GetCoffeeSitesFromCurrentUserAsyncTask(currentUser, this).execute();
    }

    private void findNumberOfCoffeeSitesFromCurrentUser() {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new GetNumberOfCoffeeSitesFromCurrentUserAsyncTask(currentUser, this).execute();
        } else {
            Log.i(REQ_COFFEESITE_TAG, "Current user is null. Cannot execute GetNumberOfCoffeeSitesFromCurrentUserAsyncTask");
        }
    }

    private void findAllCoffeeSitesFromCurrentUser() {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new GetCoffeeSitesFromCurrentUserAsyncTask(currentUser, this).execute();
        } else {
            Log.i(REQ_COFFEESITE_TAG, "Current user is null. Cannot execute GetNumberOfCoffeeSitesFromCurrentUserAsyncTask");
        }
    }

    private void findCoffeeSiteById(long coffeeSiteId) {
        new GetCoffeeSiteAsyncTask(this, coffeeSiteId).execute();
    }

    /**
     * Helper method to get current logged-in user from userAccountService
     * @return
     */
    private LoggedInUser getCurrentUser() {
        if (userAccountService != null) {
            LoggedInUser currentUser = userAccountService.getLoggedInUser();
            return currentUser;
        } else return null;
    }

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService = false;
    // Don't attempt to bind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldBindUserLoginService = true;

    @Override
    public void onUserAccountServiceConnected() {
        Log.i(REQ_COFFEESITE_TAG, "onUserLoginServiceConnected()");
        userAccountService = userAccountServiceConnector.getUserLoginService();
        if (userAccountService != null && userAccountService.isUserLoggedIn()) {
            currentUser = userAccountService.getLoggedInUser();
            Log.i(REQ_COFFEESITE_TAG, "currentUser available");
        }
    }

    private void doBindUserAccountService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        if (userAccountService == null) {
            userAccountServiceConnector = new UserAccountServiceConnector(this);
            Intent intent = new Intent(this, UserAccountService.class);
            //Intent intent = new Intent(getApplicationContext(), UserAccountService.class);
            if (mShouldBindUserLoginService)
                if (bindService(intent, userAccountServiceConnector, Context.BIND_AUTO_CREATE)) {
                    mShouldUnbindUserLoginService = true;
                    mShouldBindUserLoginService = false;
                } else {
                    Log.e(REQ_COFFEESITE_TAG, "Error: The requested 'UserAccountService' service doesn't " +
                            "exist, or this client isn't allowed access to it.");
                }
        }
    }

    private void doUnbindUserLoginService() {
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            userAccountServiceConnector.removeUserAccountServiceConnectionListener(this);
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserLoginService = false;
            mShouldBindUserLoginService = true;
        }
    }

    @Override
    public void onDestroy() {
        doUnbindUserLoginService();
        super.onDestroy();
    }

}