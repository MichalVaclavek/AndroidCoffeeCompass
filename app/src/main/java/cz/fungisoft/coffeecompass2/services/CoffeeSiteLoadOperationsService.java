package cz.fungisoft.coffeecompass2.services;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSiteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetCoffeeSitesFromCurrentUserAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetNumberOfCoffeeSitesFromCurrentUserAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteNumbersRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;

/**
 * Service to perform CoffeeSite load operations i.e. loading of one CoffeeSite,
 * loading of all CoffeeSites created by user and so on.
 * It has to listen some interface, especialy those called by AsyncTasks when REST
 * calls are performed to load CoffeeSites
 */
public class CoffeeSiteLoadOperationsService extends CoffeeSiteWithUserAccountService
                                             implements CoffeeSiteRESTResultListener,
                                                        CoffeeSitesRESTResultListener,
                                                        CoffeeSiteNumbersRESTResultListener {

    static final String TAG = "CoffeeSiteLoadService";

    /**
     * Enum type to identify CoffeeSite change status operations
     * Can be used in Activities using this service
     */
    public enum LoadOperation {
        COFFEE_SITE_LOAD,
        COFFEE_SITES_FROM_USER_LOAD,
        COFFEE_SITES_FROM_CURRENT_USER_LOAD,
        COFFEE_SITES_NUMBER_FROM_CURRENT_USER
    }


    // Listeners, usualy Activities, which called respective service method
    // and wants to be informed about resutl later, as all the operations are Async
    private List<CoffeeSiteLoadServiceOperationsListener> loadOperationsListeners = new ArrayList<>();

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
    public void onNumberOfCoffeeSitesReturned(CoffeeSiteRESTOper oper, Result<Integer> result) {
        int coffeeSitesNumber = 0;
        if (result instanceof Result.Success) {
            coffeeSitesNumber = ((Result.Success<Integer>) result).getData();
            if (oper == CoffeeSiteRESTOper.COFFEE_SITES_NUMBER_FROM_CURRENT_USER) {
                informClientAboutNumberOfCoffeeSitesFromLoggedInUser(oper, coffeeSitesNumber, "");
            }
        } else {
            RestError error = ((Result.Error) result).getRestError();
            if (error != null) {
                informClientAboutNumberOfCoffeeSitesFromLoggedInUser(oper, 0, error.getDetail());
                Log.e(TAG, "Error when obtaining number of coffee sites from user." + error.getDetail());
            }
        }
    }


    @Override
    public void onCoffeeSiteReturned(CoffeeSiteRESTOper oper, Result<CoffeeSite> result) {
        CoffeeSite coffeeSite = null;
        if (result instanceof Result.Success) {
            coffeeSite = ((Result.Success<CoffeeSite>) result).getData();
            switch (oper) {
                case COFFEE_SITE_LOAD: informClientAboutLoadedCoffeeSite(coffeeSite, "");
                   break;
            }

        } else {
            RestError error = ((Result.Error) result).getRestError();
            if (error != null) {
                informClientAboutLoadedCoffeeSite(null, error.getDetail());
                Log.e(TAG, "Error when obtaining coffee site." + error.getDetail());
            }
        }
    }

    @Override
    public void onCoffeeSitesReturned(CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result) {

        List<CoffeeSite> coffeeSites;
        if (result instanceof Result.Success) {
            coffeeSites = ((Result.Success<List<CoffeeSite>>) result).getData();
            switch (oper) {
                case COFFEE_SITES_FROM_USER_LOAD:
                case COFFEE_SITES_FROM_CURRENT_USER_LOAD: informClientAboutLoadedCoffeeSitesList(oper, coffeeSites, "");
                   break;
            }
        } else {
            RestError error = ((Result.Error) result).getRestError();
            if (error != null) {
                informClientAboutLoadedCoffeeSitesList(oper, null, error.getDetail());
                Log.e(TAG, "Error when obtaining coffee sites." + error.getDetail());
            }
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service started.");
    }


    public void findAllCoffeeSitesByUserId(long userId) {
        //new GetCoffeeSitesFromCurrentUserAsyncTask(currentUser, this).execute();
    }

    public void findNumberOfCoffeeSitesFromCurrentUser() {

        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITES_NUMBER_FROM_CURRENT_USER;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            //new GetNumberOfCoffeeSitesFromCurrentUserAsyncTask(currentUser, this).execute();
            new GetNumberOfCoffeeSitesFromCurrentUserAsyncTask(requestedRESTOperation, currentUser, this).execute();
        } else {
            Log.i(TAG, "Current user is null. Cannot execute GetNumberOfCoffeeSitesFromCurrentUserAsyncTask");
        }
    }

    public void findAllCoffeeSitesFromCurrentUser() {

        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITES_FROM_CURRENT_USER_LOAD;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            //new GetCoffeeSitesFromCurrentUserAsyncTask(currentUser, this).execute();
            new GetCoffeeSitesFromCurrentUserAsyncTask(requestedRESTOperation, currentUser, this).execute();
        } else {
            Log.i(TAG, "Current user is null. Cannot execute GetNumberOfCoffeeSitesFromCurrentUserAsyncTask");
        }
    }

    public void findCoffeeSiteById(long coffeeSiteId) {

        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITE_LOAD;
        //new GetCoffeeSiteAsyncTask(this, coffeeSiteId).execute();
        new GetCoffeeSiteAsyncTask(requestedRESTOperation,this, coffeeSiteId).execute();
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

}
