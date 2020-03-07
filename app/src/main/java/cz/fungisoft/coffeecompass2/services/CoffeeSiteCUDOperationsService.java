package cz.fungisoft.coffeecompass2.services;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteServiceCUDOperationsListener;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.CoffeeSiteCUDOperationsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteIdRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteRESTResultListener;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_SAVE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_UPDATE;

/**
 * Service to call all Async tasks related to Create, Update and Delete CoffeeSite operations
 * and pass the results to calling Activity.
 */
public class CoffeeSiteCUDOperationsService extends CoffeeSiteWithUserAccountService
                                            implements CoffeeSiteRESTResultListener,
                                                       CoffeeSiteIdRESTResultListener {

    static final String TAG = "CoffeeSiteCUDOpService";

    /**
     * Enum type to identify CoffeeSite operations
     * Can be used in Activities using this service
     */
    public enum CUDOperation {
        COFFEE_SITE_SAVE,
        COFFEE_SITE_UPDATE,
        COFFEE_SITE_DELETE
    }

    // Listeners, usually Activities, which called respective service method
    // and wants to be informed about resutl later, as all the operations are Async
    private List<CoffeeSiteServiceCUDOperationsListener> coffeeSiteCUDOperationsListeners = new ArrayList<>();

    public void addCUDOperationsListener(CoffeeSiteServiceCUDOperationsListener listener) {
        if (!coffeeSiteCUDOperationsListeners.contains(listener)) {
            coffeeSiteCUDOperationsListeners.add(listener);
        }
    }
    public void removeCUDOperationsListener(CoffeeSiteServiceCUDOperationsListener listener) {
        coffeeSiteCUDOperationsListeners.remove(listener);
    }


    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSiteCUDOperationsService.LocalBinder();


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends CoffeeSiteWithUserAccountService.LocalBinder {

        CoffeeSiteCUDOperationsService getService() {
            return CoffeeSiteCUDOperationsService.this;
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
        Log.d(TAG, "Service started.");
    }

    /**
     * Current CoffeeSite which is used for server operations save, update, activate and so on
     */
    private CoffeeSite coffeeSite;

    /**
     * @param coffeeSite
     */
    public void save(CoffeeSite coffeeSite) {
        requestedRESTOperation = COFFEE_SITE_SAVE;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setCreatedByUserName(currentUser.getUserName());
            new CoffeeSiteCUDOperationsAsyncTask(requestedRESTOperation, coffeeSite, currentUser,
                    this).execute();
        }
    }


    public void update(CoffeeSite coffeeSite) {
        requestedRESTOperation = COFFEE_SITE_UPDATE;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setLastEditUserName(currentUser.getUserName());
            new CoffeeSiteCUDOperationsAsyncTask(requestedRESTOperation, coffeeSite, currentUser,
                    this).execute();
        }
    }


    public void delete(CoffeeSite coffeeSite) {
        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITE_DELETE;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new CoffeeSiteCUDOperationsAsyncTask(requestedRESTOperation, coffeeSite, currentUser,
                    this).execute();
        }
    }

    @Override
    public void onCoffeeSiteReturned(CoffeeSiteRESTOper oper, Result<CoffeeSite> result) {
        CoffeeSite returnedCoffeeSite;
        if (result instanceof Result.Success) {
            returnedCoffeeSite = ((Result.Success<CoffeeSite>) result).getData();
            informClientAboutCoffeeSiteOperationResult(oper, returnedCoffeeSite, "");

        } else {
            RestError error = ((Result.Error) result).getRestError();
            informClientAboutCoffeeSiteOperationResult(oper, null, error.getDetail());
            Log.e(TAG, "Error when returning coffee sites." + error.getDetail());
        }
    }

    @Override
    public void onCoffeeSitesIdReturned(CoffeeSiteRESTOper oper, Result<Long> result) {
        long returnedCoffeeSiteId;
        if (result instanceof Result.Success) {
            returnedCoffeeSiteId = ((Result.Success<Long>) result).getData();
            informClientAboutCoffeeSiteIdOperationResult(oper, returnedCoffeeSiteId, "");

        } else {
            RestError error = ((Result.Error) result).getRestError();
            informClientAboutCoffeeSiteIdOperationResult(oper, 0, error.getDetail());
            Log.e(TAG, "Error when returning coffee site id. " + error.getDetail());
        }

    }


    /**
     * Informs calling Activity about requested action result via Intent
     * Expects that operationResult and operationError are set by
     * methods calling this method
     */
    private void informClientAboutCoffeeSiteOperationResult(CoffeeSiteRESTOper operation, CoffeeSite coffeeSite, String error) {

        for (CoffeeSiteServiceCUDOperationsListener listener : coffeeSiteCUDOperationsListeners) {
            switch (operation) {
                case COFFEE_SITE_SAVE: listener.onCoffeeSiteSaved(coffeeSite, error);
                    break;
                case COFFEE_SITE_UPDATE: listener.onCoffeeSiteUpdated(coffeeSite, error);
                    break;

                default: break;
            }
        }
    }

    /**
     * Informs calling Activity about requested ...
     */
    private void informClientAboutCoffeeSiteIdOperationResult(CoffeeSiteRESTOper operation, long coffeeSiteId, String error) {

        for (CoffeeSiteServiceCUDOperationsListener listener : coffeeSiteCUDOperationsListeners) {
            switch (operation) {
                case COFFEE_SITE_DELETE: listener.onCoffeeSiteDeleted(coffeeSiteId, error);
                    break;

                default: break;
            }
        }
    }

}
