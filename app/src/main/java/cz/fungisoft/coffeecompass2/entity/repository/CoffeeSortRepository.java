package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSortDao;
import io.reactivex.Single;

/**
 * Repository class for CoffeeSort objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class CoffeeSortRepository extends CoffeeSiteRepositoryBase {

    private final CoffeeSortDao coffeeSortDao;
    private final LiveData<List<CoffeeSort>> mAllCoffeeSorts;

    private final Single<List<CoffeeSort>> mAllCoffeeSortsSingle;

    public Single<List<CoffeeSort>> getAllCoffeeSortsSingle() {
        return mAllCoffeeSortsSingle;
    }

    CoffeeSortRepository(CoffeeSiteDatabase db) {
        super(db);
        coffeeSortDao = db.coffeeSortDao();
        mAllCoffeeSorts = coffeeSortDao.getAllCoffeeSorts();
        mAllCoffeeSortsSingle = coffeeSortDao.getAllCoffeeSortsSingle();
    }

    public LiveData<List<CoffeeSort>> getAllCoffeeSorts() {
        return mAllCoffeeSorts;
    }

    public Single<CoffeeSort> getCoffeeSort(String coffeeSortValue) {
        return coffeeSortDao.getCoffeeSort(coffeeSortValue);
    }

    public void insert (CoffeeSort coffeeSort) {
        new CoffeeSortRepository.insertAsyncTask(coffeeSortDao).execute(coffeeSort);
    }

    private static class insertAsyncTask {

        private final CoffeeSortDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSortDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final CoffeeSort... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertCoffeeSort(params[0]));
        }
    }

    public void insertAll (List<CoffeeSort> CoffeeSorts) {
        new InsertAllAsyncTask(coffeeSortDao).execute(CoffeeSorts);
    }

    private static class InsertAllAsyncTask {

        private final CoffeeSortDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSortDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<CoffeeSort>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
