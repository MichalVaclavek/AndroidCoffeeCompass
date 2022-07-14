package cz.fungisoft.coffeecompass2.services;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteServiceCUDOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteUploadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.CoffeeSiteCreateUpdateAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.CoffeeSiteDeleteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.UploadCoffeeSitesAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteIdRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesUploadRESTResultListener;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITES_UPLOAD;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_SAVE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_UPDATE;

/**
 * Service to call all Async tasks related to Create, Update and Delete CoffeeSite operations
 * and pass the results to calling Activity.
 * Also used to upload list of CoffeeSites created/updated when OFFLINE.
 * <p>
 * Also can perform saving of the created/updated CoffeeSite into DB when operating OFFLINE.
 */
public class CoffeeSiteCUDOperationsService extends CoffeeSiteWithUserAccountService
                                            implements CoffeeSiteRESTResultListener,
                                                       CoffeeSitesUploadRESTResultListener, // for CoffeeSites upload
                                                       CoffeeSiteIdRESTResultListener {

    static final String TAG = "CoffeeSiteCUDOpService";

    /**
     * To save coffee site into DB, when OFFLINE
     */
    private static CoffeeSiteRepository coffeeSiteRepository;

    /**
     * Enum type to identify CoffeeSite operations
     * Can be used in Activities using this service
     */
    public enum CUDOperation {
        COFFEE_SITE_SAVE,
        COFFEE_SITE_SAVE_TO_DB,
        COFFEE_SITE_UPDATE,
        COFFEE_SITE_DELETE
    }

    // Listeners, usually Activities, which called respective service method
    // and wants to be informed about resutl later, as all the operations are Async
    private final List<CoffeeSiteServiceCUDOperationsListener> coffeeSiteCUDOperationsListeners = new ArrayList<>();

    public void addCUDOperationsListener(CoffeeSiteServiceCUDOperationsListener listener) {
        if (!coffeeSiteCUDOperationsListeners.contains(listener)) {
            coffeeSiteCUDOperationsListeners.add(listener);
        }
        // Updates PreferenceHelper data saving info in there are any CoffeeSites not saved on server, i.e. created when OFFLINE
        if (!coffeeSiteCUDOperationsListeners.isEmpty() &&
                (coffeeSiteCUDOperationsListeners.get(0) instanceof LifecycleOwner))
            getNumOfCoffeeSitesNotSavedOnServer().observe((LifecycleOwner) coffeeSiteCUDOperationsListeners.get(0), new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer numOfSitesNotSavedOnServer) {
                    DataForOfflineModePreferenceHelper offlineDataPreferenceHelper = new DataForOfflineModePreferenceHelper(getApplicationContext());
                    offlineDataPreferenceHelper.putDataSavedOfflineAvailable(numOfSitesNotSavedOnServer != null && numOfSitesNotSavedOnServer > 0);
                }
            });
    }

    public void removeCUDOperationsListener(CoffeeSiteServiceCUDOperationsListener listener) {
        coffeeSiteCUDOperationsListeners.remove(listener);
    }

    // Listeners for CoffeeSites upload operations result. Usually Activities, which called respective service method
    // and wants to be informed about result later, as all the operations are Async
    private final List<CoffeeSiteUploadServiceOperationsListener> uploadOperationsListeners = new ArrayList<>();

    public void addUploadOperationsListener(CoffeeSiteUploadServiceOperationsListener listener) {
        if (!uploadOperationsListeners.contains(listener)) {
            uploadOperationsListeners.add(listener);
        }
    }
    public void removeUploadOperationsListener(CoffeeSiteUploadServiceOperationsListener listener) {
        uploadOperationsListeners.remove(listener);
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
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(getApplicationContext());
        db.getOpenHelper().getWritableDatabase(); // to invoke onOpen() of the DB
        coffeeSiteRepository = new CoffeeSiteRepository(db);

        Log.d(TAG, "Service started.");
    }


    /**
     * @param coffeeSite
     */
    public void save(CoffeeSite coffeeSite) {
        requestedRESTOperation = COFFEE_SITE_SAVE;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setCreatedByUserName(currentUser.getUserName());
            new CoffeeSiteCreateUpdateAsyncTask(requestedRESTOperation, coffeeSite, userAccountService,
                    this).execute();
        }
    }

    /**
     * Saves newly created CoffeeSite in OFFLINE mode to local DB.
     * Or updates CoffeeSite in OFFLINE mode in local DB.
     *
     * @param coffeeSite
     */
    public void saveToDB(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setCreatedByUserName(currentUser.getUserName());
            coffeeSite.setSavedOnServer(false);
            coffeeSiteRepository.insert(coffeeSite);
        }
    }
    /**
     * Saves newly created CoffeeSite in OFFLINE mode to local DB.
     * Or updates CoffeeSite in OFFLINE mode in local DB.
     *
     * @param coffeeSite
     */
    public void saveToDB(List<CoffeeSite> coffeeSites) {
        currentUser = getCurrentUser();
        if (currentUser != null && coffeeSites != null) {
            coffeeSiteRepository.insertAll(coffeeSites);
        }
    }


    /**
     * Saves newly created CoffeeSite in OFFLINE mode to local DB.
     * Or updates CoffeeSite in OFFLINE mode in local DB.
     *
     * @param coffeeSite
     */
    public void updateInDB(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null && coffeeSite != null) {
            coffeeSite.setLastEditUserName(currentUser.getUserName());
            coffeeSite.setSavedOnServer(false);
            coffeeSiteRepository.update(coffeeSite);
        }
    }

    /**
     * Updates status of CoffeeSite saved in DB to be kept as not modified in Offline mode
     *
     * @param coffeeSite
     */
    public void cancelUpdateInDB(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null && coffeeSite != null) {
            coffeeSite.setSavedOnServer(true);
            coffeeSiteRepository.update(coffeeSite);
        }
    }

    /**
     * Deletes CoffeeSite, not saved on server, from local DB.
     *
     * @param coffeeSite
     */
    public void deleteFromDB(CoffeeSite coffeeSite) {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSiteRepository.deleteCoffeeSiteFromDB(coffeeSite);
        }
    }

    /**
     * Deletes all CoffeeSite not saved on server, from local DB.
     * Used after such CoffeeSites are already saved on server.
     */
    public void deleteAllNotSavedCoffeeSitesFromDB() {
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSiteRepository.deleteCoffeeSiteSavedOnServerFromDB();
        }
    }

    private LiveData<Integer> getNumOfCoffeeSitesNotSavedOnServer() {
        return coffeeSiteRepository.getNumOfCoffeeSitesNotSavedOnServer();
    }


    public void update(CoffeeSite coffeeSite) {
        requestedRESTOperation = COFFEE_SITE_UPDATE;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            coffeeSite.setLastEditUserName(currentUser.getUserName());
            new CoffeeSiteCreateUpdateAsyncTask(requestedRESTOperation, coffeeSite, userAccountService,
                    this).execute();
        }
    }


    public void delete(CoffeeSite coffeeSite) {
        requestedRESTOperation = CoffeeSiteRESTOper.COFFEE_SITE_DELETE;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            new CoffeeSiteDeleteAsyncTask(requestedRESTOperation, coffeeSite, userAccountService,
                    this).execute();
        }
    }

    @Override
    public void onCoffeeSiteReturned(CoffeeSiteRESTOper oper, Result<CoffeeSite> result) {
        CoffeeSite returnedCoffeeSite;
        if (result instanceof Result.Success) {
            returnedCoffeeSite = ((Result.Success<CoffeeSite>) result).getData();
            informClientAboutCoffeeSiteOperationResult(oper, returnedCoffeeSite, "");

        } else if (result instanceof Result.Error) {
            Result.Error error = (Result.Error) result;
            informClientAboutCoffeeSiteOperationResult(oper, null, error.getDetail());
            Log.e(TAG, "Error when returning coffee site. " + error.getDetail());
        }
    }

    /**
     * Called for example from AsyncTask to deleteUser CoffeeSite
     *
     * @param oper
     * @param result
     */
    @Override
    public void onCoffeeSitesIdReturned(CoffeeSiteRESTOper oper, Result<Long> result) {
        long returnedCoffeeSiteId;
        if (result instanceof Result.Success) {
            returnedCoffeeSiteId = ((Result.Success<Long>) result).getData();
            informClientAboutCoffeeSiteIdOperationResult(oper, returnedCoffeeSiteId, "");

        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                informClientAboutCoffeeSiteIdOperationResult(oper, 0, error.getDetail());
                Log.e(TAG, "Error when returning coffee site id. " + error.getDetail());
            }
        }
    }

    /*** Upload CoffeeSites operation *** START ***/

    private List<CoffeeSite> coffeeSitesToUpload;


    /**
     * Upload list of CoffeeSites (not their images)
     *
     * @param coffeeSitesToUpload
     */
    public void uploadCoffeeSites(List<CoffeeSite> coffeeSitesToUpload) {
        this.coffeeSitesToUpload = coffeeSitesToUpload;
        requestedRESTOperation = COFFEE_SITES_UPLOAD;
        currentUser = getCurrentUser();
        if (currentUser != null) {
            // Modify CoffeeSites saved only in phone DB to be saved as new CoffeeSites on server
            for (CoffeeSite coffeeSite : this.coffeeSitesToUpload) {
                if (!coffeeSite.isSavedOnServer() && coffeeSite.getStatusZaznamu().getStatus().isEmpty()) {
                    coffeeSite.saveId(); // if the saving fails, we can then restore original local DB id
                    coffeeSite.setId(0); // new CoffeeSite must be with ID=0 to be saved on server
                    coffeeSite.setLastEditUserName(null);
                    coffeeSite.setHodnoceni(null); // CREATED cannot have Rating yet
                }
            }
            new UploadCoffeeSitesAsyncTask(requestedRESTOperation, userAccountService, coffeeSitesToUpload,this).execute();
        } else {
            Log.w(TAG, "Current user is null. Cannot execute UploadCoffeeSitesAsyncTask.execute()");
        }
    }

    /**
     * Called after {@link UploadCoffeeSitesAsyncTask} finished.
     *
     * @param oper identifier of REST operation which lead to call this method
     * @param result success or error result of the operation. If success, then List<CoffeeSite> is returned in result = new Result.Success<>(coffeeSites);
     */
    @Override
    public void onCoffeeSitesUploadedAndReturned(CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result) {
        if (oper == COFFEE_SITES_UPLOAD) {
            this.onCoffeeSitesUploaded(oper, result);
        }
    }


    /**
     * **** CoffeeSites upload SUCCESSFUL ****
     *
     * @param oper
     * @param result
     */
    public void onCoffeeSitesUploaded(CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result) {

        if (result instanceof Result.Success) {
            Log.i(TAG, "CoffeeSite sites uploaded.");

            List<CoffeeSite> updatedCoffeeSites = ((Result.Success<List<CoffeeSite>>) result).getData();

            // CoffeeSites saved, clear not saved CoffeeSites and save the same CoffeeSites returned from server,
            // but with correct IDs.
            // This triggers refresh of this Activity list of CoffeeSites, see onCoffeeSiteServiceConnected()
            // and onUserAccountServiceConnected()
            // deleteAllNotSavedCoffeeSitesFromDB();
            // SECOND: Save newly created/updated CoffeeSites as returned from server, to DB
            // saveToDB(updatedCoffeeSites);

            informClientAboutUploadedCoffeeSitesResult(updatedCoffeeSites, "");
        } else {
            Log.e(TAG, "CoffeeSite sites upload failed.");
            Result.Error error = (Result.Error) result;
            // Restore IDs of the input CoffeeSites - it can be then used to further processing by calling activity.
            // (for example allowed new editing of the CoffeeSite, if upload would fail)
            for (CoffeeSite coffeeSite : this.coffeeSitesToUpload) {
                if (!coffeeSite.isSavedOnServer() && coffeeSite.getStatusZaznamu().getStatus().isEmpty()) {
                    coffeeSite.restoreId();
                }
            }
            if (error != null) {
                informClientAboutUploadedCoffeeSitesResult(null, error.getDetail());
                Log.e(TAG, "Error when uploading coffee sites. " + error.getDetail());
            }
        }
    }

    /**
     * Informs clients (listener Activity) about result of uploading CoffeeSites to server
     */
    private void informClientAboutUploadedCoffeeSitesResult(List<CoffeeSite> returnedCoffeeSites, String error) {
        Log.i(TAG, "Informing clients about coffeeSites list upload.");

        for (CoffeeSiteUploadServiceOperationsListener listener : uploadOperationsListeners) {
            listener.onCoffeeSitesUploaded(returnedCoffeeSites, error);
        }
    }

    /*** Upload CoffeeSites operation *** END ***/

    /**
     * Informs calling Activity about requested action result via Intent.<br>
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
            }
        }
    }

    /**
     * Informs calling Activity about requested Delete (or other operation returning coffeeSite ID )
     * operations result.
     */
    private void informClientAboutCoffeeSiteIdOperationResult(CoffeeSiteRESTOper operation, long coffeeSiteId, String error) {

        for (CoffeeSiteServiceCUDOperationsListener listener : coffeeSiteCUDOperationsListeners) {
            switch (operation) {
                case COFFEE_SITE_DELETE: listener.onCoffeeSiteDeleted(coffeeSiteId, error);
                    break;
            }
        }
    }

}
