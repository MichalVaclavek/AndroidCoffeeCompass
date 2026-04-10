package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteRecordStatusDao;
import io.reactivex.Single;

/**
 * Repository class for CoffeeSiteRecord objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class CoffeeSiteRecordStatusRepository extends CoffeeSiteRepositoryBase {

    private final CoffeeSiteRecordStatusDao coffeeSiteRecordStatusDao;
    private final LiveData<List<CoffeeSiteRecordStatus>> mAllCoffeeSiteRecordStatuss;

    CoffeeSiteRecordStatusRepository(CoffeeSiteDatabase db) {
        super(db);
        coffeeSiteRecordStatusDao = db.coffeeSiteRecordStatusDao();
        mAllCoffeeSiteRecordStatuss = coffeeSiteRecordStatusDao.getAllCSRecordStatuses();
    }

    public LiveData<List<CoffeeSiteRecordStatus>> getAllCoffeeSiteRecordStatuses() {
        return mAllCoffeeSiteRecordStatuss;
    }

    public Single<CoffeeSiteRecordStatus> getCoffeeSiteRecordStatus(String recordStatusValue) {
        return coffeeSiteRecordStatusDao.getCoffeeSiteRecordStatus(recordStatusValue);
    }

    public void insert (CoffeeSiteRecordStatus CoffeeSiteRecordStatus) {
        new CoffeeSiteRecordStatusRepository.insertAsyncTask(coffeeSiteRecordStatusDao).execute(CoffeeSiteRecordStatus);
    }

    private static class insertAsyncTask {

        private final CoffeeSiteRecordStatusDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSiteRecordStatusDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final CoffeeSiteRecordStatus... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insert(params[0]));
        }
    }

    public void insertAll (List<CoffeeSiteRecordStatus> CoffeeSiteRecordStatuss) {
        new InsertAllAsyncTask(coffeeSiteRecordStatusDao).execute(CoffeeSiteRecordStatuss);
    }

    void insertAllBlocking(List<CoffeeSiteRecordStatus> coffeeSiteRecordStatuses) {
        coffeeSiteRecordStatusDao.insertAll(coffeeSiteRecordStatuses);
    }

    private static class InsertAllAsyncTask {

        private final CoffeeSiteRecordStatusDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteRecordStatusDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<CoffeeSiteRecordStatus>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
