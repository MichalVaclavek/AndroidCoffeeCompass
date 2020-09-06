package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteTypeDao;
import io.reactivex.Flowable;

public class CoffeeSiteTypeRepository {

    private CoffeeSiteTypeDao coffeeSiteTypeDao;
    private LiveData<List<CoffeeSiteType>> mAllCoffeeSiteTypes;

    CoffeeSiteTypeRepository(Context context) {
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(context);
        coffeeSiteTypeDao = db.coffeeSiteTypeDao();
        mAllCoffeeSiteTypes = coffeeSiteTypeDao.getAllCoffeeSiteTypes();
    }

    public LiveData<List<CoffeeSiteType>> getAllCoffeeSiteTypes() {
        return mAllCoffeeSiteTypes;
    }

    public Flowable<CoffeeSiteType> getCoffeeSiteType(String siteTypeValue) {
        return coffeeSiteTypeDao.getCoffeeSiteType(siteTypeValue);
    }

    public void insert (CoffeeSiteType coffeeSiteType) {
        new CoffeeSiteTypeRepository.insertAsyncTask(coffeeSiteTypeDao).execute(coffeeSiteType);
    }

    private static class insertAsyncTask extends AsyncTask<CoffeeSiteType, Void, Void> {

        private CoffeeSiteTypeDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSiteTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CoffeeSiteType... params) {
            mAsyncTaskDao.insertCoffeeSiteType(params[0]);
            return null;
        }
    }

    public void insertAll (List<CoffeeSiteType> coffeeSiteTypes) {
        new InsertAllAsyncTask(coffeeSiteTypeDao).execute(coffeeSiteTypes);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<CoffeeSiteType>, Void, Void> {

        private CoffeeSiteTypeDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<CoffeeSiteType>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
