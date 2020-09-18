package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteStatusDao;
import io.reactivex.Flowable;

public class CoffeeSiteStatusRepository extends CoffeeSiteRepositoryBase {

    private CoffeeSiteStatusDao coffeeSiteStatusDao;
    private LiveData<List<CoffeeSiteStatus>> mAllCoffeeSiteStatuses;

    CoffeeSiteStatusRepository(CoffeeSiteDatabase db) {
        super(db);
        coffeeSiteStatusDao = db.coffeeSiteStatusDao();
        mAllCoffeeSiteStatuses = coffeeSiteStatusDao.getAllCoffeeSiteStatuses();
    }

    public LiveData<List<CoffeeSiteStatus>> getAllCoffeeSiteStatuses() {
        return mAllCoffeeSiteStatuses;
    }

    public Flowable<CoffeeSiteStatus> getCoffeeSiteStatus(String siteStatus) {
        return coffeeSiteStatusDao.getCoffeeSiteStatus(siteStatus);
    }

    public void insert (CoffeeSiteStatus coffeeSiteStatus) {
        new CoffeeSiteStatusRepository.insertAsyncTask(coffeeSiteStatusDao).execute(coffeeSiteStatus);
    }

    private static class insertAsyncTask extends AsyncTask<CoffeeSiteStatus, Void, Void> {

        private CoffeeSiteStatusDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSiteStatusDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CoffeeSiteStatus... params) {
            mAsyncTaskDao.insertCoffeeSiteStatus(params[0]);
            return null;
        }
    }

    public void insertAll (List<CoffeeSiteStatus> CoffeeSiteStatuss) {
        new InsertAllAsyncTask(coffeeSiteStatusDao).execute(CoffeeSiteStatuss);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<CoffeeSiteStatus>, Void, Void> {

        private CoffeeSiteStatusDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteStatusDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<CoffeeSiteStatus>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
