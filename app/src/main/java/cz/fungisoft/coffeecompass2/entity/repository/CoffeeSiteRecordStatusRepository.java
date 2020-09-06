package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteRecordStatusDao;
import io.reactivex.Flowable;

public class CoffeeSiteRecordStatusRepository {

    private CoffeeSiteRecordStatusDao coffeeSiteRecordStatusDao;
    private LiveData<List<CoffeeSiteRecordStatus>> mAllCoffeeSiteRecordStatuss;

    CoffeeSiteRecordStatusRepository(Context context) {
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(context);
        coffeeSiteRecordStatusDao = db.coffeeSiteRecordStatusDao();
        mAllCoffeeSiteRecordStatuss = coffeeSiteRecordStatusDao.getAllCSRecordStatuses();
    }

    public LiveData<List<CoffeeSiteRecordStatus>> getAllCoffeeSiteRecordStatuses() {
        return mAllCoffeeSiteRecordStatuss;
    }

    public Flowable<CoffeeSiteRecordStatus> getCoffeeSiteRecordStatus(String recordStatusValue) {
        return coffeeSiteRecordStatusDao.getCoffeeSiteRecordStatus(recordStatusValue);
    }

    public void insert (CoffeeSiteRecordStatus CoffeeSiteRecordStatus) {
        new CoffeeSiteRecordStatusRepository.insertAsyncTask(coffeeSiteRecordStatusDao).execute(CoffeeSiteRecordStatus);
    }

    private static class insertAsyncTask extends AsyncTask<CoffeeSiteRecordStatus, Void, Void> {

        private CoffeeSiteRecordStatusDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSiteRecordStatusDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CoffeeSiteRecordStatus... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void insertAll (List<CoffeeSiteRecordStatus> CoffeeSiteRecordStatuss) {
        new InsertAllAsyncTask(coffeeSiteRecordStatusDao).execute(CoffeeSiteRecordStatuss);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<CoffeeSiteRecordStatus>, Void, Void> {

        private CoffeeSiteRecordStatusDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteRecordStatusDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<CoffeeSiteRecordStatus>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
