package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;

/**
 * A common ancestor of CoffeeSiteServices with connected UserAccountService
 * All other CoffeeSiteServices can extend this service as this one is also abstract.
 */
public abstract class CoffeeSiteWithUserAccountService extends Service
                                                       implements UserAccountServiceConnectionListener {

    static final String TAG = "CoffeeSiteServiceBase";

    /**
     *  Enum to identify different actions called by CoffeeSiteServices
     *  Needed to distinquiese correct action call within RESTListener methods
     */
    public enum CoffeeSiteRESTOper {

        COFFEE_SITE_ENTITIES_LOAD,

        COFFEE_SITE_SAVE,
        COFFEE_SITE_UPDATE,
        COFFEE_SITE_DELETE,

        COFFEE_SITE_CANCEL,
        COFFEE_SITE_ACTIVATE,
        COFFEE_SITE_DEACTIVATE,

        COFFEE_SITE_LOAD,
        COFFEE_SITES_FROM_USER_LOAD,
        COFFEE_SITES_FROM_CURRENT_USER_LOAD,
        COFFEE_SITES_NUMBER_FROM_CURRENT_USER
    }

    /**
     * Id of the operation requested from Activity. It is passed to respective AsyncTask
     * and serves as identity for callback Listener methods here to inform correct
     * listeners of Activity
     */
    protected CoffeeSiteRESTOper requestedRESTOperation;

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSiteWithUserAccountService.LocalBinder();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {

        CoffeeSiteWithUserAccountService getService() {
            return CoffeeSiteWithUserAccountService.this;
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
        doBindUserAccountService();
        Log.d(TAG, "Service started.");
    }

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService = false;
    // Don't attempt to bind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldBindUserLoginService = true;

    /**
     * Current logged-in user
     */
    protected LoggedInUser currentUser;

    private static UserAccountService userAccountService;
    private static UserAccountServiceConnector userAccountServiceConnector;

    /**
     * Current CoffeeSite which is used for server operations save, update, activate and so on
     */
    private CoffeeSite coffeeSite;

    /**
     * Operation name type requested by client Activity when registering the service
     */
    protected int requestedOperation = 0;

    protected String operationResult = "";
    protected String operationError = "";

    /**
     * Helper method to get current logged-in user from userAccountService
     * @return
     */
    protected LoggedInUser getCurrentUser() {

        if (userAccountService != null) {
            LoggedInUser currentUser = userAccountService.getLoggedInUser();
            return currentUser;
        } else return null;
    }


    @Override
    public void onUserAccountServiceConnected() {
        Log.i(TAG, "onUserLoginServiceConnected()");
        userAccountService = userAccountServiceConnector.getUserLoginService();
        if (userAccountService != null && userAccountService.isUserLoggedIn()) {
            currentUser = userAccountService.getLoggedInUser();
            Log.i(TAG, "currentUser available");
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
                    Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                            "exist, or this client isn't allowed access to it.");
                }
        }
    }

    private void doUnbindUserAccountService() {
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
        doUnbindUserAccountService();
        super.onDestroy();
    }

}
