package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.NextToMachineTypeDao;
import io.reactivex.Single;

/**
 * Repository class for NextToMachineType objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class NextToMachineTypeRepository extends CoffeeSiteRepositoryBase {

    private final NextToMachineTypeDao nextToMachineTypeDao;
    private final LiveData<List<NextToMachineType>> mAllNextToMachineTypes;

    NextToMachineTypeRepository(CoffeeSiteDatabase db) {
        super(db);
        nextToMachineTypeDao = db.nextToMachineTypeDao();
        mAllNextToMachineTypes = nextToMachineTypeDao.getAllNextToMachineTypes();
    }

    public LiveData<List<NextToMachineType>> getAllNextToMachineTypes() {
        return mAllNextToMachineTypes;
    }

    public Single<NextToMachineType> getNextToMachineType(String nextToMachineTypeValue) {
        return nextToMachineTypeDao.getNextToMachineType(nextToMachineTypeValue);
    }

    public void insert (NextToMachineType nextToMachineType) {
        new NextToMachineTypeRepository.insertAsyncTask(nextToMachineTypeDao).execute(nextToMachineType);
    }

    private static class insertAsyncTask {

        private final NextToMachineTypeDao mAsyncTaskDao;

        insertAsyncTask(NextToMachineTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final NextToMachineType... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertNextToMachineType(params[0]));
        }
    }

    public void insertAll (List<NextToMachineType> NextToMachineTypes) {
        new InsertAllAsyncTask(nextToMachineTypeDao).execute(NextToMachineTypes);
    }

    private static class InsertAllAsyncTask {

        private final NextToMachineTypeDao mAsyncTaskDao;

        InsertAllAsyncTask(NextToMachineTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<NextToMachineType>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
