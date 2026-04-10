package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CupTypeDao;
import io.reactivex.Single;

/**
 * Repository class for CupType objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class CupTypeRepository extends CoffeeSiteRepositoryBase {

    private final CupTypeDao cupTypeDao;
    private final LiveData<List<CupType>> mAllCupTypes;

    CupTypeRepository(CoffeeSiteDatabase db) {
        super(db);
        cupTypeDao = db.cupTypeDao();
        mAllCupTypes = cupTypeDao.getAllCupTypes();
    }

    public LiveData<List<CupType>> getAllCupTypes() {
        return mAllCupTypes;
    }

    public Single<CupType> getCupType(String cupTypeValue) {
        return cupTypeDao.getCupType(cupTypeValue);
    }

    public void insert (CupType cupType) {
        new CupTypeRepository.insertAsyncTask(cupTypeDao).execute(cupType);
    }

    private static class insertAsyncTask {

        private final CupTypeDao mAsyncTaskDao;

        insertAsyncTask(CupTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final CupType... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertCupType(params[0]));
        }
    }

    public void insertAll (List<CupType> CupTypes) {
        new InsertAllAsyncTask(cupTypeDao).execute(CupTypes);
    }

    void insertAllBlocking(List<CupType> cupTypes) {
        cupTypeDao.insertAll(cupTypes);
    }

    private static class InsertAllAsyncTask {

        private final CupTypeDao mAsyncTaskDao;

        InsertAllAsyncTask(CupTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<CupType>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
