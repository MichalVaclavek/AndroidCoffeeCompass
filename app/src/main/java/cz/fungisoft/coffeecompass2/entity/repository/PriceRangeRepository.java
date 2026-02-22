package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.repository.dao.PriceRangeDao;
import io.reactivex.Single;

/**
 * Repository class for PriceRange objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class PriceRangeRepository extends CoffeeSiteRepositoryBase {

    private final PriceRangeDao priceRangeDao;

    private final LiveData<List<PriceRange>> mAllPriceRanges;
    private final Single<List<PriceRange>> mAllPriceRangesSingle;

    PriceRangeRepository(CoffeeSiteDatabase db) {
        super(db);
        priceRangeDao = db.priceRangeDao();
        mAllPriceRanges = priceRangeDao.getAllPriceRanges();
        mAllPriceRangesSingle = priceRangeDao.getAllPriceRangesSingle();
    }

    public LiveData<List<PriceRange>> getAllPriceRanges() {
        return mAllPriceRanges;
    }

    public Single<PriceRange> getPriceRange(String priceRangeValue) {
        return priceRangeDao.getPriceRange(priceRangeValue);
    }

    public void insert (PriceRange priceRange) {
        new PriceRangeRepository.insertAsyncTask(priceRangeDao).execute(priceRange);
    }

    public Single<List<PriceRange>> getAllPriceRangesSingle() {
        return mAllPriceRangesSingle;
    }

    private static class insertAsyncTask {

        private final PriceRangeDao mAsyncTaskDao;

        insertAsyncTask(PriceRangeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final PriceRange... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertPriceRange(params[0]));
        }
    }

    public void insertAll (List<PriceRange> PriceRanges) {
        new InsertAllAsyncTask(priceRangeDao).execute(PriceRanges);
    }

    private static class InsertAllAsyncTask {

        private final PriceRangeDao mAsyncTaskDao;

        InsertAllAsyncTask(PriceRangeDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<PriceRange>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
