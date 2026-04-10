package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteTypeDao;
import io.reactivex.Single;

/**
 * Repository class for CoffeeSiteType objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class CoffeeSiteTypeRepository extends CoffeeSiteRepositoryBase {

    private final CoffeeSiteTypeDao coffeeSiteTypeDao;

    private final LiveData<List<CoffeeSiteType>> mAllCoffeeSiteTypes;
    private final Single<List<CoffeeSiteType>> mAllCoffeeSiteTypesSingle;

    CoffeeSiteTypeRepository(CoffeeSiteDatabase db) {
        super(db);
        coffeeSiteTypeDao = db.coffeeSiteTypeDao();
        mAllCoffeeSiteTypes = coffeeSiteTypeDao.getAllCoffeeSiteTypes();
        mAllCoffeeSiteTypesSingle = coffeeSiteTypeDao.getAllCoffeeSiteTypesSingle();
    }

    public LiveData<List<CoffeeSiteType>> getAllCoffeeSiteTypes() {
        return mAllCoffeeSiteTypes;
    }

    public Single<List<CoffeeSiteType>> getAllCoffeeSiteTypesSingle() {
        return mAllCoffeeSiteTypesSingle;
    }

    public Single<CoffeeSiteType> getCoffeeSiteType(String siteTypeValue) {
        return coffeeSiteTypeDao.getCoffeeSiteType(siteTypeValue);
    }

    public void insert (CoffeeSiteType coffeeSiteType) {
        new CoffeeSiteTypeRepository.insertAsyncTask(coffeeSiteTypeDao).execute(coffeeSiteType);
    }

    private static class insertAsyncTask {

        private final CoffeeSiteTypeDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSiteTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final CoffeeSiteType... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertCoffeeSiteType(params[0]));
        }
    }

    public void insertAll (List<CoffeeSiteType> coffeeSiteTypes) {
        new InsertAllAsyncTask(coffeeSiteTypeDao).execute(coffeeSiteTypes);
    }

    void insertAllBlocking(List<CoffeeSiteType> coffeeSiteTypes) {
        coffeeSiteTypeDao.insertAll(coffeeSiteTypes);
    }

    private static class InsertAllAsyncTask {

        private final CoffeeSiteTypeDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<CoffeeSiteType>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
