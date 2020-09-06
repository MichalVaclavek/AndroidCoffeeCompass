package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteEntitiesServiceOperationsListener;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetAllCoffeeSitesAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.ReadCoffeeSiteEntitiesAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntityRepositories;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesLoadRESTResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;

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
                                                  CoffeeSitesRESTResultListener {

    static final String TAG = "CoffeeSiteServiceBase";

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

    public CoffeeSiteEntityRepositories getEntitiesRepository() {
       return entitiesRepository;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        entitiesRepository = CoffeeSiteEntityRepositories.getInstance(getApplicationContext());

        Log.d(TAG, "Service started.");
    }


    /**
     * Methods to start running AsyncTask
     **/

    public void readAndSaveAllEntitiesFromServer() {
        new ReadCoffeeSiteEntitiesAsyncTask(COFFEE_SITE_ENTITIES_LOAD, this, entitiesRepository).execute();
    }

    @Override
    public void onCoffeeSiteEntitiesLoaded(Result<Boolean> result) {
        if (result instanceof Result.Success) {
            informClientAboutResult( ((Result.Success<Boolean>) result).getData());
        }
    }

    private void informClientAboutResult(Boolean result) {
        for (CoffeeSiteEntitiesServiceOperationsListener listener : coffeeSiteEntitiesOperationsListeners) {
            listener.onCoffeeSiteEntitiesLoaded(result);
        }
    }

    /**
     * Methods to start running AsyncTask
     **/

    public void readAndSaveAllCoffeeSitesFromServer() {
        new GetAllCoffeeSitesAsyncTask(COFFEE_SITE_LOAD_ALL, this).execute();
    }

    @Override
    public void onCoffeeSitesReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result) {
        if (result instanceof Result.Success) {

//            CoffeeSiteDBHelper coffeeSiteDBHelper = new CoffeeSiteDBHelper(this, dbManager);
//            dbManager.open(coffeeSiteDBHelper);
//
//            for (CoffeeSite coffeeSite : ((Result.Success<List<CoffeeSite>>) result).getData()) {
//                dbManager.insert(coffeeSite);
//            }
//
//            dbManager.close();


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
