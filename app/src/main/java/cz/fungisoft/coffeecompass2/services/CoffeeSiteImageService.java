package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ImageDeleteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ImageUploadAsyncTask;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteImageServiceCallResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;

/**
 * Service to handle requests for saving or deleting Image/Photo of the CoffeeSite.
 * Calls REST interface to save/delete the image from coffeecompass.cz server.
 * Needs UserAccountService available to obtain logged-in user data as the
 * REST requests for saving/deleting image requires logged-in user.
 */
public class CoffeeSiteImageService extends Service implements UserAccountServiceConnectionListener {

    private static final String TAG = "CoffeeSiteImageService";

    /**
     * List of listeneres for user login events.
     */
    private static List<CoffeeSiteImageServiceCallResultListener> imageOperationsResultListeners = new ArrayList<>();

    public void addImageOperationsResultListener(CoffeeSiteImageServiceCallResultListener imageOperationsResultListener) {
        if (!imageOperationsResultListeners.contains(imageOperationsResultListener)) {
            imageOperationsResultListeners.add(imageOperationsResultListener);
        }
        Log.i(TAG, "Počet posluchačů Image operations result: " + imageOperationsResultListeners.size());
    }

    public void removeImageOperationsResultListener(CoffeeSiteImageServiceCallResultListener imageOperationsResultListener) {
        imageOperationsResultListeners.remove(imageOperationsResultListener);
        Log.i(TAG, "Počet posluchačů Image operations result: " + imageOperationsResultListeners.size());
    }

    /**
     * Current logged-in user
     */
    private LoggedInUser currentUser;

    private static UserAccountService userAccountService;
    private static UserAccountServiceConnector userAccountServiceConnector;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        CoffeeSiteImageService getService() {
            return CoffeeSiteImageService.this;
        }
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSiteImageService.LocalBinder();

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

    @Override
    public void onUserAccountServiceConnected() {
        Log.i(TAG, "onUserLoginServiceConnected()");
        userAccountService = userAccountServiceConnector.getUserLoginService();
        if (userAccountService != null && userAccountService.isUserLoggedIn()) {
            currentUser = userAccountService.getLoggedInUser();
            Log.i(TAG, "currentUser available");
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


    /** Methods to be called by Activity **/
    public void uploadImage(File imageFile, int coffeeSiteId) {
        currentUser = getCurrentUser();
        new ImageUploadAsyncTask(this, currentUser, imageFile, coffeeSiteId).execute();
    }

    public void deleteImage(int coffeeSiteId) {
        currentUser = getCurrentUser();
        new ImageDeleteAsyncTask(this, currentUser, coffeeSiteId).execute();
    }

    /** Methods to be called from AsyncTask REST calls */

    public void evaluateImageSaveResult(Result result) {
        if (result instanceof Result.Success) {
            onImageSaveSuccess(((Result.Success) result).getData().toString());
        } else {
            RestError error = ((Result.Error) result).getRestError();
            if (error != null) {
                onImageSaveFailure(error.getDetail());
            }
        }
    }

    public void evaluateImageDeleteResult(Result result) {
        if (result instanceof Result.Success) {
            onImageDeleteSuccess(((Result.Success) result).getData().toString());
        } else {
            RestError error = ((Result.Error) result).getRestError();
            if (error != null) {
                onImageDeleteFailure(error.getDetail());
            }
        }
    }

    // Fire-up methods for login/register/logout events to be processed by listeners

    /* ---- Image Save ----  */
    private void onImageSaveSuccess(String result) {
        for (CoffeeSiteImageServiceCallResultListener listener : imageOperationsResultListeners) {
            listener.onImageSaveSuccess(result);
        }
    }
    private void onImageSaveFailure(String error) {
        for (CoffeeSiteImageServiceCallResultListener listener : imageOperationsResultListeners) {
            listener.onImageSaveFailure(error);
        }
    }

    /* ---- Image Save ----  */
    private void onImageDeleteSuccess(String result) {
        for (CoffeeSiteImageServiceCallResultListener listener : imageOperationsResultListeners) {
            listener.onImageDeleteSuccess(result);
        }
    }
    private void onImageDeleteFailure(String error) {
        for (CoffeeSiteImageServiceCallResultListener listener : imageOperationsResultListeners) {
            listener.onImageDeleteFailure(error);
        }
    }


    @Override
    public void onDestroy() {
        doUnbindUserLoginService();
        super.onDestroy();
    }

}
