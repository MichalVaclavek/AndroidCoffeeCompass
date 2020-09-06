package cz.fungisoft.coffeecompass2.entity.repository;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteDao;
import io.reactivex.Flowable;

public class CoffeeSiteRepository {

    private CoffeeSiteDao coffeeSiteDao;
    private LiveData<List<CoffeeSite>> mAllCoffeeSites;

    CoffeeSiteRepository(Context context) {
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(context);
        coffeeSiteDao = db.coffeeSiteDao();
        mAllCoffeeSites = coffeeSiteDao.getAllCoffeeSites();
    }

    public LiveData<List<CoffeeSite>> getAllCoffeeSites() {
        return mAllCoffeeSites;
    }

    public Flowable<CoffeeSite> getCoffeeSiteById(int siteId) {
        return coffeeSiteDao.getCoffeeSiteById(siteId);
    }

    public void insert (CoffeeSite coffeeSite) {
        new insertAsyncTask(coffeeSiteDao).execute(coffeeSite);
    }

    private static class insertAsyncTask extends AsyncTask<CoffeeSite, Void, Void> {

        private CoffeeSiteDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CoffeeSite... params) {
            mAsyncTaskDao.insertCoffeeSite(params[0]);
            return null;
        }
    }

    public void insertAll (List<CoffeeSite> coffeeSites) {
        new CoffeeSiteRepository.InsertAllAsyncTask(coffeeSiteDao).execute(coffeeSites);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<CoffeeSite>, Void, Void> {

        private CoffeeSiteDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<CoffeeSite>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
