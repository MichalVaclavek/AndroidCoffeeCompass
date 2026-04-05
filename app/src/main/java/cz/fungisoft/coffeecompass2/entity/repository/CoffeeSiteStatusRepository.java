package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteStatusDao;
import io.reactivex.Single;

/**
 * Repository class for CoffeeSiteStatus objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class CoffeeSiteStatusRepository extends CoffeeSiteRepositoryBase {

    private final CoffeeSiteStatusDao coffeeSiteStatusDao;
    private final LiveData<List<CoffeeSiteStatus>> mAllCoffeeSiteStatuses;
    private final Single<List<CoffeeSiteStatus>> mAllCoffeeSiteStatusesSingle;

    CoffeeSiteStatusRepository(CoffeeSiteDatabase db) {
        super(db);
        coffeeSiteStatusDao = db.coffeeSiteStatusDao();
        mAllCoffeeSiteStatuses = coffeeSiteStatusDao.getAllCoffeeSiteStatuses();
        mAllCoffeeSiteStatusesSingle = coffeeSiteStatusDao.getAllCoffeeSiteStatusesSingle();
    }

    public LiveData<List<CoffeeSiteStatus>> getAllCoffeeSiteStatuses() {
        return mAllCoffeeSiteStatuses;
    }

    public Single<List<CoffeeSiteStatus>> getAllCoffeeSiteStatusesSingle() {
        return mAllCoffeeSiteStatusesSingle;
    }

    public Single<CoffeeSiteStatus> getCoffeeSiteStatus(String siteStatus) {
        return coffeeSiteStatusDao.getCoffeeSiteStatus(siteStatus);
    }

    public void insert (CoffeeSiteStatus coffeeSiteStatus) {
        new CoffeeSiteStatusRepository.insertAsyncTask(coffeeSiteStatusDao).execute(coffeeSiteStatus);
    }

    private static class insertAsyncTask {

        private final CoffeeSiteStatusDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSiteStatusDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final CoffeeSiteStatus... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertCoffeeSiteStatus(params[0]));
        }
    }

    public void insertAll (List<CoffeeSiteStatus> CoffeeSiteStatuss) {
        new InsertAllAsyncTask(coffeeSiteStatusDao).execute(CoffeeSiteStatuss);
    }

    private static class InsertAllAsyncTask {

        private final CoffeeSiteStatusDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteStatusDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<CoffeeSiteStatus>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
