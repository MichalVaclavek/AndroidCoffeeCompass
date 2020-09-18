package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.SiteLocationTypeDao;
import io.reactivex.Flowable;

public class SiteLocationTypeRepository extends CoffeeSiteRepositoryBase {

    private SiteLocationTypeDao siteLocationTypeDao;
    private LiveData<List<SiteLocationType>> mAllSiteLocationTypes;

    SiteLocationTypeRepository(CoffeeSiteDatabase db) {
        super(db);
        siteLocationTypeDao = db.siteLocationTypeDao();
        mAllSiteLocationTypes = siteLocationTypeDao.getAllSiteLocationTypes();
    }

    public LiveData<List<SiteLocationType>> getAllSiteLocationTypes() {
        return mAllSiteLocationTypes;
    }

    public Flowable<SiteLocationType> getSiteLocationType(String siteLocationTypeValue) {
        return siteLocationTypeDao.getSiteLocationType(siteLocationTypeValue);
    }

    public void insert (SiteLocationType siteLocationType) {
        new SiteLocationTypeRepository.insertAsyncTask(siteLocationTypeDao).execute(siteLocationType);
    }

    private static class insertAsyncTask extends AsyncTask<SiteLocationType, Void, Void> {

        private SiteLocationTypeDao mAsyncTaskDao;

        insertAsyncTask(SiteLocationTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SiteLocationType... params) {
            mAsyncTaskDao.insertSiteLocationType(params[0]);
            return null;
        }
    }

    public void insertAll (List<SiteLocationType> SiteLocationTypes) {
        new InsertAllAsyncTask(siteLocationTypeDao).execute(SiteLocationTypes);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<SiteLocationType>, Void, Void> {

        private SiteLocationTypeDao mAsyncTaskDao;

        InsertAllAsyncTask(SiteLocationTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<SiteLocationType>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
