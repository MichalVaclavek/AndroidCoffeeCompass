package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

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

    private static class insertAsyncTask extends AsyncTask<CoffeeSort, Void, Void> {

        private final CoffeeSortDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSortDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CoffeeSort... params) {
            mAsyncTaskDao.insertCoffeeSort(params[0]);
            return null;
        }
    }

    public void insertAll (List<CoffeeSort> CoffeeSorts) {
        new InsertAllAsyncTask(coffeeSortDao).execute(CoffeeSorts);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<CoffeeSort>, Void, Void> {

        private final CoffeeSortDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSortDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<CoffeeSort>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
