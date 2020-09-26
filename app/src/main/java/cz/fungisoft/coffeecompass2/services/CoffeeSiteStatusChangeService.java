package cz.fungisoft.coffeecompass2.services;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteServiceStatusOperationsListener;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ChangeStatusOfCoffeeSiteAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteRESTResultListener;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_ACTIVATE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_CANCEL;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_DEACTIVATE;

/**
 * Service to cover all actions regarding CoffeeSite status changes i.e. activation,
 * deactivation, cancel.<br>
 * Service uses usually AsyncTasks to perform REST calls to coffeecompass.cz server.
 * Respective status change methods are called by Activities.<br>
 * Service must implement {@link CoffeeSiteRESTResultListener} which informs about result
 * of the respectve Async tasks. The results are then passed to Activities, which
 * implement {@link CoffeeSiteServiceStatusOperationsListener}
 */
public class CoffeeSiteStatusChangeService extends CoffeeSiteWithUserAccountService
                                           implements CoffeeSiteRESTResultListener {

    static final String TAG = "CoffeeSiteStatusService";

    /**
     * Enum type to identify CoffeeSite change status operations
     * Can be used in Activities using this service
     */
    public enum StatusChangeOperation {
        COFFEE_SITE_ACTIVATE,
        COFFEE_SITE_DEACTIVATE,
        COFFEE_SITE_CANCEL
    }

    // Listeners, usually Activities, which called respective service method
    // and wants to be informed about resutl later, as all the operations are Async
    private List<CoffeeSiteServiceStatusOperationsListener> coffeeSiteStatusOperationsListeners = new ArrayList<>();

    public void addCoffeeSiteStatusOperationsListener(CoffeeSiteServiceStatusOperationsListener listener) {
        if (!coffeeSiteStatusOperationsListeners.contains(listener)) {
            coffeeSiteStatusOperationsListeners.add(listener);
        }
    }
    public void removeCoffeeSiteStatusOperationsListener(CoffeeSiteServiceStatusOperationsListener listener) {
        coffeeSiteStatusOperationsListeners.remove(listener);
    }


    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSiteStatusChangeService.LocalBinder();


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends CoffeeSiteWithUserAccountService.LocalBinder {

        CoffeeSiteStatusChangeService getService() {
            return CoffeeSiteStatusChangeService.this;
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
    //private CoffeeSite coffeeSite;


    public void activate(CoffeeSite coffeeSite) {
        requestedRESTOperation = COFFEE_SITE_ACTIVATE;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new ChangeStatusOfCoffeeSiteAsyncTask(requestedRESTOperation, coffeeSite, currentUser,
                    this).execute();
        }
    }

    public void deactivate(CoffeeSite coffeeSite) {
        requestedRESTOperation = COFFEE_SITE_DEACTIVATE;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new ChangeStatusOfCoffeeSiteAsyncTask(requestedRESTOperation, coffeeSite, currentUser,
                    this).execute();
        }
    }

    public void cancel(CoffeeSite coffeeSite) {
        requestedRESTOperation = COFFEE_SITE_CANCEL;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new ChangeStatusOfCoffeeSiteAsyncTask(requestedRESTOperation, coffeeSite, currentUser,
                    this).execute();
        }
    }

    @Override
    public void onCoffeeSiteReturned(CoffeeSiteRESTOper oper, Result<CoffeeSite> result) {
        CoffeeSite returnedCoffeeSite;
        if (result instanceof Result.Success) {
            returnedCoffeeSite = ((Result.Success<CoffeeSite>) result).getData();
            informClientAboutCoffeeSiteStatusChangeResult(oper, returnedCoffeeSite, "");

        } else {
            RestError error = ((Result.Error) result).getRestError();
            informClientAboutCoffeeSiteStatusChangeResult(oper, null, error.getDetail());
            Log.e(TAG, "Error when obtaining coffee sites." + error.getDetail());
        }
    }

    private void informClientAboutCoffeeSiteStatusChangeResult(CoffeeSiteRESTOper operation, CoffeeSite coffeeSite, String error) {
        for (CoffeeSiteServiceStatusOperationsListener listener : coffeeSiteStatusOperationsListeners) {
            switch (operation) {
                case COFFEE_SITE_ACTIVATE: listener.onCoffeeSiteActivated(coffeeSite, error);
                    break;
                case COFFEE_SITE_DEACTIVATE: listener.onCoffeeSiteDeactivated(coffeeSite, error);
                    break;
                case COFFEE_SITE_CANCEL: listener.onCoffeeSiteCanceled(coffeeSite, error);
                    break;
                default: break;
            }
        }
    }

}
