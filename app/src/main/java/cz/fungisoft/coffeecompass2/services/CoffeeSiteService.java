package cz.fungisoft.coffeecompass2.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.CoffeeSiteEntitiesRESTInterface;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ChangeStatusOfCoffeeSiteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.CoffeeSiteCUDOperationsAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSiteAsynTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesFromUserAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceConnectionListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

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
public class CoffeeSiteService extends IntentService implements UserLoginServiceConnectionListener {

    static final String REQ_ENTITIES_TAG = "GetCoffeeSiteEntities";
    static final String REQ_COFFEESITE_TAG = "CoffeeSiteService";

    /**
     * Druhy akci, ktere bude tato trida provadet
     */
    public final static String COFFEE_SITE = "coffee_site";

    public final static String COFFEE_SITE_ENTITY = "coffee_site_entity";

    // Jednotlive operace s danymi druhy objektu, ktere umi trida provadet
    public final static int COFFEE_SITE_ENTITIES_LOAD = 10;

    public final static int COFFEE_SITE_SAVE = 20;
    public final static int COFFEE_SITE_SAVE_AND_ACTIVATE = 21;
    public final static int COFFEE_SITE_UPDATE = 30;
    public final static int COFFEE_SITE_UPDATE_AND_ACTIVATE = 31; // ?? is it really needed ??
    public final static int COFFEE_SITE_DELETE = 40;

    public final static int COFFEE_SITE_CANCEL = 50;
    public final static int COFFEE_SITE_ACTIVATE = 51;
    public final static int COFFEE_SITE_DEACTIVATE = 52;

    public final static int COFFEE_SITE_LOAD = 60;
    public final static int COFFEE_SITES_FROM_USER_LOAD = 61;
    public final static int COFFEE_SITES_FROM_CURRENT_USER_LOAD = 62;

    /**
     * Current CoffeeSite which is used for server operations save, update, activate and so on
     */
    private CoffeeSite coffeeSite;

    /**
     * CoffeeSite id whic is requested to be loaded from server
     */
    private int coffeeSiteId = 0;

    /**
     * Current logged-in user
     */
    private LoggedInUser currentUser;

    private UserAccountService userAccountService;
    private UserAccountServiceConnector userLoginServiceConnector;

    /**
     *  Array of all CoffeeSiteEntity Classes to be loaded from server to repository
     *  needed to correct function of creating/updating CoffeeSite instancies
     */
    public static final Class<? extends CoffeeSiteEntity>[] COFFEE_SITE_ENTITY_CLASSES
            = new Class[]{CoffeeSiteRecordStatus.class, CoffeeSiteStatus.class, CoffeeSiteType.class,
            CoffeeSort.class, CupType.class, NextToMachineType.class, OtherOffer.class, PriceRange.class,
            SiteLocationType.class, StarsQualityDescription.class};

    private CoffeeSiteEntitiesRepository entitiesRepository;

    public CoffeeSiteEntitiesRepository getEntitiesRepository() {
        return entitiesRepository;
    }

    /**
     * Operation name type requested by client Activity when registering the service
     */
    //private String operation = "";
    private int operation = 0;

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
        Log.i("CoffeeSiteService", "Constructor start doBindUserLoginService()");

    }


    public CoffeeSiteService() {
        this("CoffeeSiteService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        doBindUserLoginService();

        Log.i(REQ_COFFEESITE_TAG, "OnHandle intent start");
        operation = intent.getIntExtra("operation_type", 0);
        coffeeSite = (CoffeeSite) intent.getParcelableExtra("coffeeSite");

        coffeeSiteId =  intent.getIntExtra("coffeeSiteId", 0);

        if (userAccountService == null) {
            /* onHandleIntent has lost the race with onServiceConnected
             * so wait this service Thread for 10 ms to allow finish onServiceConnected()
             * //TODO is there a better solution? ... Not found, yet
             */
            try { Thread.sleep(42);  } catch (InterruptedException e) {
                Log.e(REQ_COFFEESITE_TAG, "OnHandleIntent(): sleep() failure.");
            }
            //startService(intent);
        }

        switch (operation) {
            case COFFEE_SITE_ENTITIES_LOAD:{
                readAndSaveAllEntitiesFromServer();
            } break;
            case COFFEE_SITE_SAVE:{
                Log.i("OnHandle intent", "save");
                save(coffeeSite);
            } break;
            case COFFEE_SITE_SAVE_AND_ACTIVATE:{
                Log.i("OnHandle intent", "saveAndActivate");
                saveAndActivate(coffeeSite);
            } break;
            case COFFEE_SITE_UPDATE:{
                update(coffeeSite);
            } break;
            case COFFEE_SITE_UPDATE_AND_ACTIVATE:{ //TODO - Probably not needed
                updateAndActivate(coffeeSite);
            } break;
            case COFFEE_SITE_DELETE:{
                delete(coffeeSite);
            } break;
            case COFFEE_SITE_CANCEL:{
                cancel(coffeeSite);
            } break;
            case COFFEE_SITE_ACTIVATE:{
                activate(coffeeSite);
            } break;
            case COFFEE_SITE_DEACTIVATE:{
                deactivate(coffeeSite);
            } break;
            case COFFEE_SITE_LOAD:{
                if (coffeeSiteId != 0) {
                    findCoffeeSiteById(coffeeSiteId);
                }
            } break;
            case COFFEE_SITES_FROM_CURRENT_USER_LOAD:{
                findAllCoffeeSitesFromCurrentUser();
            } break;
            case COFFEE_SITES_FROM_USER_LOAD:{
                findAllCoffeeSitesByUserId(currentUser.getUserId());
            } break;
            default: break;
        }
    }

    /**
     * Callback methods to be called by AsyncTask when finished
     **/

    /**
     * Sending result of loading all CoffeeSiteEntities load operation.
     * Called from Retrofit callback function
     */
    private void sendLoadEntitiesOperationResultToClient(){
        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE_ENTITY);
        intent.putExtra("operationResult", operationResult);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /** Collects AsyncOperation result for Create, Update and Delete operations
     * and calls method to inform client Activity about results
     * Can also call another asyncTask if needed because of requested operation
     */
    public void sendCoffeeSiteCUDOperationResultToClient(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION restOperation, String result, String error) {
        Log.i(REQ_COFFEESITE_TAG, "Sent operation Async task result to client: start");
        operationResult = result;
        operationError = error;

        if (operation == COFFEE_SITE_SAVE_AND_ACTIVATE && restOperation == CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.CREATE) {
            // perform activation
            activate(coffeeSite);
        }
        if (operation == COFFEE_SITE_UPDATE_AND_ACTIVATE && restOperation == CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.UPDATE) {
            // perform activation
            activate(coffeeSite);
        }

        informClientAboutCoffeeSiteOperationResult();
        Log.i(REQ_COFFEESITE_TAG, "Sent operation Async task result to client: end");
    }

    // Collects AsyncOperation result and calls method to inform client Activity about results
    public void sendCoffeeSiteStatusChangeResultToClient(ChangeStatusOfCoffeeSiteAsyncTask.SITE_STATUS_ASYNC_REST_OPERATION status, String result, String error) {
        Log.i(REQ_COFFEESITE_TAG, "Sent status change Async task result to client: start");
        operationResult = result;
        operationError = error;
        informClientAboutCoffeeSiteStatusChangeResult();
        Log.i(REQ_COFFEESITE_TAG, "Sent status change Async task result to client: end");
    }

    /**
     * One CoffeeSite load from server operation result
     * @param result
     * @param error
     */
    public void sendCoffeeSiteLoadResultToClient(CoffeeSite locCoffeeSite, String result, String error) {
        operationResult = result;
        operationError = error;
        informClientAboutLoadedCoffeeSite(locCoffeeSite);
    }

    /**
     * One CoffeeSite load from server operation result
     * @param result
     * @param error
     */
    public void sendCoffeeSitesFromUserLoadResultToClient(List<CoffeeSite> coffeeSites, String result, String error) {
        operationResult = result;
        operationError = error;
        informClientAboutLoadedCoffeeSitesList(coffeeSites);
    }


    /**
     * Informs calling Activity about requested action result via Intent
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutCoffeeSiteOperationResult() {
        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE);
        intent.putExtra("operationType", operation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
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
        intent.setAction(COFFEE_SITE);
        intent.putExtra("operationType", operation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutLoadedCoffeeSite(CoffeeSite locCoffeeSite) {
        Intent intent = new Intent();
        intent.setAction(COFFEE_SITE);
        intent.putExtra("operationType", operation);
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
        intent.setAction(COFFEE_SITE);
        intent.putExtra("operationType", operation);
        intent.putExtra("operationResult", operationResult);
        intent.putExtra("operationError", operationError);
        if (coffeeSites != null) {
            intent.putParcelableArrayListExtra("coffeeSitesList", (ArrayList<? extends Parcelable>) coffeeSites);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    /**
     * Methods to start running AsyncTask
     **/

     /**
     * @param coffeeSite
     */
    private void save(CoffeeSite coffeeSite){
        new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.CREATE, coffeeSite, currentUser,
                this).execute();
    }

    private void saveAndActivate(CoffeeSite coffeeSite){
        new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.CREATE, coffeeSite, currentUser,
                this).execute();
    }

    private void update(CoffeeSite coffeeSite){
        new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.UPDATE, coffeeSite, currentUser,
                this).execute();
    }

    /**
     * Curerntly same as update as this combination is probably not applicable
     * @param coffeeSite
     */
    private void updateAndActivate(CoffeeSite coffeeSite){
        new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.UPDATE, coffeeSite, currentUser,
                this).execute();
    }

    private void delete(CoffeeSite coffeeSite){
        new CoffeeSiteCUDOperationsAsyncTask(CoffeeSiteCUDOperationsAsyncTask.SITE_ASYNC_REST_OPERATION.DELETE, coffeeSite, currentUser,
                this).execute();
    }

    private void activate(CoffeeSite coffeeSite){
        new ChangeStatusOfCoffeeSiteAsyncTask(ChangeStatusOfCoffeeSiteAsyncTask.SITE_STATUS_ASYNC_REST_OPERATION.ACTIVATE, coffeeSite, currentUser,
                this).execute();
    }

    private void deactivate(CoffeeSite coffeeSite){
        new ChangeStatusOfCoffeeSiteAsyncTask(ChangeStatusOfCoffeeSiteAsyncTask.SITE_STATUS_ASYNC_REST_OPERATION.DEACTIVATE, coffeeSite, currentUser,
                this).execute();
    }

    private void cancel(CoffeeSite coffeeSite){
        new ChangeStatusOfCoffeeSiteAsyncTask(ChangeStatusOfCoffeeSiteAsyncTask.SITE_STATUS_ASYNC_REST_OPERATION.CANCEL, coffeeSite, currentUser,
                this).execute();
    }

    private void findAllCoffeeSitesByUserId(long userId){
        //new GetCoffeeSitesFromUserAsyncTask(currentUser, this).execute();
    }

    private void findAllCoffeeSitesFromCurrentUser(){
        new GetCoffeeSitesFromUserAsyncTask(currentUser, this).execute();
    }

    private void findCoffeeSiteById(long coffeeSiteId) {
        new GetCoffeeSiteAsynTask(this, coffeeSiteId).execute();
    }


    /**
     * Starts Retrofit operation to load all instancies of
     * all CoffeeSiteEntity class and save them to CoffeeSiteEntitiesRepository
     */
    private void readAndSaveAllEntitiesFromServer() {

        Log.d(REQ_ENTITIES_TAG, "GetAllCoffeeSiteEntityValuesAsyncTask REST request initiated");

        Gson gson = new GsonBuilder().setLenient().create();

        //Add the interceptor to the client builder.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CoffeeSiteEntitiesRESTInterface.GET_ENTITY_BASE)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CoffeeSiteEntitiesRESTInterface api = retrofit.create(CoffeeSiteEntitiesRESTInterface.class);

        resetEntitiesCallCounter();
        for (Class<? extends CoffeeSiteEntity> entityClass : COFFEE_SITE_ENTITY_CLASSES) {
            readAndSaveEntitiesFromServer(entityClass, api);
        }
    }

    private int entitiesCallCounter = 0;

    private synchronized void incrementEntitiesCallCounter() {
        entitiesCallCounter = entitiesCallCounter + 1;
    }
    private synchronized void resetEntitiesCallCounter() {
        entitiesCallCounter = 0;
    }
    private synchronized int getEntitiesCallCounter() {
        return entitiesCallCounter;
    }

    /**
     *
     *  CoffeeSiteRecordStatus.class, CoffeeSiteStatus.class, CoffeeSiteType.class,
     *  CoffeeSort.class, CupType.class, NextToMachineType.class, OtherOffer.class, PriceRange.class,
     *  SiteLocationType.class, StarsQualityDescription.class};
     *
     * @param entityClass
     * @param api
     * @param <T>
     */
    private <T extends List<? extends CoffeeSiteEntity>> void readAndSaveEntitiesFromServer(Class<? extends CoffeeSiteEntity> entityClass,
                                                                                            CoffeeSiteEntitiesRESTInterface api) {
        operationResult = "";
        operationError = "";

        Call<T> call = null;
        //1. Get all CoffeeSiteStatus
        if (entityClass == CoffeeSiteStatus.class) {
            call = (Call<T>) api.getAllCoffeeSiteSiteStatuses();
        }
        //2. Get all CoffeeSiteStatus
        if (entityClass == CoffeeSiteRecordStatus.class) {
            call = (Call<T>) api.getAllCoffeeSiteRecordStatuses();
        }
        //3. Get all CoffeeSiteStatus
        if (entityClass == CoffeeSiteType.class) {
            call = (Call<T>) api.getAllCoffeeSiteTypes();
        }
        //4. Get all CoffeeSiteStatus
        if (entityClass == CoffeeSort.class) {
            call = (Call<T>) api.getAllCoffeeSorts();
        }
        //5. Get all CoffeeSiteStatus
        if (entityClass == CupType.class) {
            call = (Call<T>) api.getAllCupTypes();
        }
        //6. Get all CoffeeSiteStatus
        if (entityClass == NextToMachineType.class) {
            call = (Call<T>) api.getAllNextToMachineTypes();
        }
        //7. Get all CoffeeSiteStatus
        if (entityClass == OtherOffer.class) {
            call = (Call<T>) api.getAllOtherOffers();
        }
        //8. Get all CoffeeSiteStatus
        if (entityClass == SiteLocationType.class) {
            call = (Call<T>) api.getAllSiteLocationTypes();
        }
        //9. Get all CoffeeSiteStatus
        if (entityClass == StarsQualityDescription.class) {
            call = (Call<T>) api.getAllStarsQualityDescriptions();
        }
        //10. Get all CoffeeSiteStatus
        if (entityClass == PriceRange.class) {
            call = (Call<T>) api.getAllPriceRanges();
        }

        if (call != null) {
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    incrementEntitiesCallCounter();
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            //Log.i("onSuccess", response.body());
                            entitiesRepository.setEntities(response.body());

                            if (getEntitiesCallCounter() == COFFEE_SITE_ENTITY_CLASSES.length) {
                                operationResult = "OK";
                                sendLoadEntitiesOperationResultToClient();
                                entitiesRepository.setDataReadedFromServer(true);
                            }
                        } else {
                            Log.i("onEmptyResponse", "Returned empty response retrieving info about CoffeeSite entities REST request.");
                            Result.Error error = new Result.Error(new IOException("Error retrieving info about CoffeeSite entities REST request."));
                            operationError = "ERROR";
                            if (getEntitiesCallCounter() == COFFEE_SITE_ENTITY_CLASSES.length) {
                                sendLoadEntitiesOperationResultToClient();
                            }
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                        } catch (IOException e) {
                            Log.e(REQ_ENTITIES_TAG, e.getMessage());
                            operationError = getString(R.string.coffeesiteservice_error_message_not_available);
                        }
                        if (getEntitiesCallCounter() == COFFEE_SITE_ENTITY_CLASSES.length) {
                            sendLoadEntitiesOperationResultToClient();
                        }
                    }
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    incrementEntitiesCallCounter();
                    Log.e(REQ_ENTITIES_TAG, "Error retrieving info about CoffeeSite entities REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error retrieving info about CoffeeSite entities REST request.", t));
                    operationError = error.getDetail();
                    if (getEntitiesCallCounter() == COFFEE_SITE_ENTITY_CLASSES.length) {
                        sendLoadEntitiesOperationResultToClient();
                    }
                }
            });
        }
    }


    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;
    // Don't attempt to bind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldBindUserLoginService = true;

    @Override
    public void onUserLoginServiceConnected() {
        Log.i("CoffeeSiteService", "onUserLoginServiceConnected()");
        userAccountService = userLoginServiceConnector.getUserLoginService();
        if (userAccountService != null && userAccountService.isUserLoggedIn()) {
            currentUser = userAccountService.getLoggedInUser();
            Log.i("CoffeeSiteService", "currentUser available");
        }
    }

    private void doBindUserLoginService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userLoginServiceConnector = new UserAccountServiceConnector(this);
        Intent intent = new Intent(this, UserAccountService.class);
        //Intent intent = new Intent(getApplicationContext(), UserAccountService.class);
        if (mShouldBindUserLoginService)
        if (bindService(intent, userLoginServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
            mShouldBindUserLoginService = false;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }

    }

    private void doUnbindUserLoginService() {
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            unbindService(userLoginServiceConnector);
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