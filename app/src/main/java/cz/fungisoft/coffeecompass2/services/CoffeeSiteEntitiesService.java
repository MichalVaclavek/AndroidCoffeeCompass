package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteEntitiesServiceOperationsListener;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetAllCoffeeSitesAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ReadCoffeeSiteEntitiesAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntityRepositories;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesLoadRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.NetworkStateReceiver;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_ENTITIES_LOAD;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper.COFFEE_SITE_LOAD_ALL;

/**
 * A Service class to hold CoffeeSite entities classes.
 * Is able to load all available instancies of CoffeeSite entities
 * and save them into EntitiesRepository.
 *
 * It has its own Service connector and is not part of other
 * CoffeeSiteServices with their common service connector.
 */
public class CoffeeSiteEntitiesService extends Service
                                       implements CoffeeSiteEntitiesLoadRESTResultListener,
                                                  CoffeeSitesRESTResultListener,
                                                  CoffeeSiteDatabase.DbDeleteEndListener {

    static final String TAG = "CoffeeSiteServiceBase";

    /**
     * Detector of internet connection change
     */
    private final NetworkStateReceiver networkChangeStateReceiver = new NetworkStateReceiver();


    // Listeners, usually Activities, which called respective service method
    // and wants to be informed about resutl later, as all the operations are Async
    private List<CoffeeSiteEntitiesServiceOperationsListener> coffeeSiteEntitiesOperationsListeners = new ArrayList<>();

    public void addCoffeeSiteEntitiesOperationsListener(CoffeeSiteEntitiesServiceOperationsListener listener) {
        if (!coffeeSiteEntitiesOperationsListeners.contains(listener)) {
            coffeeSiteEntitiesOperationsListeners.add(listener);
        }
    }

    public void removeCoffeeSiteEntitiesOperationsListener(CoffeeSiteEntitiesServiceOperationsListener listener) {
        coffeeSiteEntitiesOperationsListeners.remove(listener);
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new CoffeeSiteEntitiesService.LocalBinder();


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        CoffeeSiteEntitiesService getService() {
            return CoffeeSiteEntitiesService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private static CoffeeSiteEntityRepositories entitiesRepository;

    private static CoffeeSiteRepository coffeeSiteRepository;

    public CoffeeSiteEntityRepositories getEntitiesRepository() {
       return entitiesRepository;
    }

    private CoffeeSiteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeStateReceiver, filter);

        db = CoffeeSiteDatabase.getDatabase(getApplicationContext());
        db.addDbDeleteEndListener(this);
        coffeeSiteRepository = new CoffeeSiteRepository(db);
        db.getOpenHelper().getWritableDatabase(); // to invoke onOpen() of the DB

        Log.d(TAG, "Service started.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeStateReceiver);
        db.removeDbDeleteEndListener(this);
        Log.d(TAG, "Service destroyed.");
    }


    /**
     * Deletes current CS entities data from DB and loads and saves new ones
     */
    public void populateCSEntities() {
        db.deleteCSEntitiesAsync();
    }

    @Override
    public void onCSEntitiesDeletedEnd() {
//        if (Utils.isOfflineModeOn(getApplicationContext())) {
            readAndSaveAllEntitiesFromServer();
//        }
    }

    /**
     * Methods to start running AsyncTask
     **/

    private void readAndSaveAllEntitiesFromServer() {
        if (Utils.isOnline()) {
            entitiesRepository = CoffeeSiteEntityRepositories.getInstance(db);
            new ReadCoffeeSiteEntitiesAsyncTask(COFFEE_SITE_ENTITIES_LOAD, this, entitiesRepository).execute();
        }
    }

    @Override
    public void onCoffeeSiteEntitiesLoaded(Result<Boolean> result) {
        if (result instanceof Result.Success) {
            informClientAboutCSEntitiesLoadResult( ((Result.Success<Boolean>) result).getData());
        }
    }

    private void informClientAboutCSEntitiesLoadResult(Boolean result) {
        for (CoffeeSiteEntitiesServiceOperationsListener listener : coffeeSiteEntitiesOperationsListeners) {
            listener.onCoffeeSiteEntitiesLoaded(result);
        }
    }

    /**
     * Methods to start AsyncTasks for deleting/loading/and saving of CoffeeSites
     **/

    /**
     * Deletes current CoffeeSites data from DB and loads and saves new ones
     */
    public void populateCoffeeSites() {
        db.deleteCoffeeSitesAsync();
    }

    @Override
    public void onCoffeeSitesDeletedEnd() {
        readAndSaveAllCoffeeSitesFromServer();
    }

    private void readAndSaveAllCoffeeSitesFromServer() {
        if (Utils.isOnline()) {
            new GetAllCoffeeSitesAsyncTask(COFFEE_SITE_LOAD_ALL, this).execute();
        }
    }

    /**
     * On all CoffeeSites returned from server.
     *
     * @param oper identifier of REST operation which lead to call this method
     * @param result - success or error result of the operation. If success, then List<CoffeeSite> is returned in result = new Result.Success<>(coffeeSites);
     */
    @Override
    public void onCoffeeSitesReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result) {
        if (result instanceof Result.Success) {

            List<CoffeeSite> allCoffeeSites = ((Result.Success<List<CoffeeSite>>) result).getData();

            coffeeSiteRepository.insertAll(allCoffeeSites);

            for (CoffeeSite cs : allCoffeeSites) {
                if (!cs.getMainImageURL().isEmpty()) {
                    String imageFileName = "photo_site_" + cs.getId();
                    cs.setMainImageFileName(imageFileName);
                    //ImageUtil.saveImage(getApplicationContext(), cs.getMainImageURL(), ImageUtil.COFFEESITE_IMAGE_DIR, cs.getMainImageFileName());
                    //Picasso.get().load(cs.getMainImageURL()).into(ImageUtil.picassoImageTarget(getApplicationContext(), ImageUtil.COFFEESITE_IMAGE_DIR, imageFileName));
                }
            }

            informClientAboutAllCoffeeSitesLoadResult(true);

        } else {
            //TODO - show info, that loading of All sites failed
            informClientAboutAllCoffeeSitesLoadResult(false);
        }

    }

    private void informClientAboutAllCoffeeSitesLoadResult(Boolean result) {
        for (CoffeeSiteEntitiesServiceOperationsListener listener : coffeeSiteEntitiesOperationsListeners) {
            listener.onAllCoffeeSitesLoaded(result);
        }
    }

}
