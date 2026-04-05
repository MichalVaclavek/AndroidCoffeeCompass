package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.SiteLocationTypeDao;
import io.reactivex.Single;

/**
 * Repository class for SiteLocationType objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class SiteLocationTypeRepository extends CoffeeSiteRepositoryBase {

    private final SiteLocationTypeDao siteLocationTypeDao;

    private final LiveData<List<SiteLocationType>> mAllSiteLocationTypes;
    private final Single<List<SiteLocationType>> mAllSiteLocationTypesSingle;

    SiteLocationTypeRepository(CoffeeSiteDatabase db) {
        super(db);
        siteLocationTypeDao = db.siteLocationTypeDao();
        mAllSiteLocationTypes = siteLocationTypeDao.getAllSiteLocationTypes();
        mAllSiteLocationTypesSingle = siteLocationTypeDao.getAllSiteLocationTypesSingle();
    }

    public LiveData<List<SiteLocationType>> getAllSiteLocationTypes() {
        return mAllSiteLocationTypes;
    }

    public Single<List<SiteLocationType>> getAllSiteLocationTypesSingle() {
        return mAllSiteLocationTypesSingle;
    }

    public Single<SiteLocationType> getSiteLocationType(String siteLocationTypeValue) {
        return siteLocationTypeDao.getSiteLocationType(siteLocationTypeValue);
    }

    public void insert (SiteLocationType siteLocationType) {
        new SiteLocationTypeRepository.insertAsyncTask(siteLocationTypeDao).execute(siteLocationType);
    }

    private static class insertAsyncTask {

        private final SiteLocationTypeDao mAsyncTaskDao;

        insertAsyncTask(SiteLocationTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final SiteLocationType... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertSiteLocationType(params[0]));
        }
    }

    public void insertAll (List<SiteLocationType> SiteLocationTypes) {
        new InsertAllAsyncTask(siteLocationTypeDao).execute(SiteLocationTypes);
    }

    private static class InsertAllAsyncTask {

        private final SiteLocationTypeDao mAsyncTaskDao;

        InsertAllAsyncTask(SiteLocationTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<SiteLocationType>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
