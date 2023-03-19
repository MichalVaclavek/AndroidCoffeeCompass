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
import cz.fungisoft.coffeecompass2.asynctask.image.ImageDeleteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.image.ImageUploadAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteImageServiceCallResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;

/**
 * Service to handle requests for saving or deleting Image/Photo of the CoffeeSite.
 * Calls REST interface to save/deleteUser the image from coffeecompass.cz server.
 * Needs UserAccountService available to obtain logged-in user data as the
 * REST requests for saving/deleting image requires logged-in user.
 */
public class CoffeeSiteImageService extends Service implements UserAccountServiceConnectionListener {

    private static final String TAG = "CoffeeSiteImageService";

    /**
     * List of listeners for operations performed by CoffeeSiteImageService
     * i.e. results of Save and Delete of CoffeeSite's image
     */
    private static final List<CoffeeSiteImageServiceCallResultListener> imageOperationsResultListeners = new ArrayList<>();

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
    private boolean mShouldBindUserLoginService = true;

    private void doBindUserAccountService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
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

    /** Methods to be called by Activity **/

    public void uploadImage(File imageFile, CoffeeSite cs) {
        Log.d(TAG, "ImageFile exists: " + imageFile.exists());
        if (imageFile.exists()) {
            new ImageUploadAsyncTask(this, userAccountService, imageFile, cs).execute();
        }
    }

    /**
     * Deletes image of the CoffeeSite on server and on COFFEESITE_IMAGE_DIR
     * @param cs
     */
    public void deleteImage(CoffeeSite cs) {
        ImageUtil.deleteCoffeeSiteImage(getApplicationContext(), cs);
        new ImageDeleteAsyncTask(this, userAccountService, cs).execute();
    }

    /**
     * Deletes image of the CoffeeSite saved in phone, but not saved in COFFEESITE_IMAGE_DIR
     * i.e. image saved only during CoffeeSite creation in CreateCoffeeSiteActivity
     *
     * @param cs
     */
    public void deleteLocalImageFile(CoffeeSite cs) {
        ImageUtil.deleteCoffeeSiteImage(getApplicationContext(), cs);
    }

    /** Methods to be called from AsyncTask REST calls */

    public void evaluateImageSaveResult(CoffeeSite cs, Result result) {
        if (result instanceof Result.Success) {
            onImageSaveSuccess(cs, ((Result.Success) result).getData().toString());
        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                Log.e(TAG, "Error REST call. " + error.getDetail());
                onImageSaveFailure(cs, error.getDetail());
            }
        }
    }

    public void evaluateImageDeleteResult(CoffeeSite cs, Result result) {
        if (result instanceof Result.Success) {
            onImageDeleteSuccess(cs, ((Result.Success) result).getData().toString());
        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                Log.e(TAG, "Error REST call. " + error.getDetail());
                onImageDeleteFailure(cs, error.getDetail());
            }
        }
    }

    // Fire-up methods for login/register/logout events to be processed by listeners

    /* ---- Om Image Save ----  */
    private void onImageSaveSuccess(CoffeeSite cs, String result) {
        for (CoffeeSiteImageServiceCallResultListener listener : imageOperationsResultListeners) {
            listener.onImageSaveSuccess(cs, result);
        }
    }
    private void onImageSaveFailure(CoffeeSite cs, String error) {
        for (CoffeeSiteImageServiceCallResultListener listener : imageOperationsResultListeners) {
            listener.onImageSaveFailure(cs, error);
        }
    }

    /* ---- On Image Delete ----  */
    private void onImageDeleteSuccess(CoffeeSite cs, String result) {
        for (CoffeeSiteImageServiceCallResultListener listener : imageOperationsResultListeners) {
            listener.onImageDeleteSuccess(cs, result);
        }
    }
    private void onImageDeleteFailure(CoffeeSite cs, String error) {
        for (CoffeeSiteImageServiceCallResultListener listener : imageOperationsResultListeners) {
            listener.onImageDeleteFailure(cs, error);
        }
    }

    @Override
    public void onDestroy() {
        doUnbindUserLoginService();
        super.onDestroy();
    }

}
