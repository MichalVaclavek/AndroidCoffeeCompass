package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.repository.dao.PriceRangeDao;
import io.reactivex.Flowable;

public class PriceRangeRepository extends CoffeeSiteRepositoryBase {

    private PriceRangeDao priceRangeDao;
    private LiveData<List<PriceRange>> mAllPriceRanges;

    PriceRangeRepository(CoffeeSiteDatabase db) {
        super(db);
        priceRangeDao = db.priceRangeDao();
        mAllPriceRanges = priceRangeDao.getAllPriceRanges();
    }

    public LiveData<List<PriceRange>> getAllPriceRanges() {
        return mAllPriceRanges;
    }

    public Flowable<PriceRange> getPriceRange(String priceRangeValue) {
        return priceRangeDao.getPriceRange(priceRangeValue);
    }

    public void insert (PriceRange priceRange) {
        new PriceRangeRepository.insertAsyncTask(priceRangeDao).execute(priceRange);
    }

    private static class insertAsyncTask extends AsyncTask<PriceRange, Void, Void> {

        private PriceRangeDao mAsyncTaskDao;

        insertAsyncTask(PriceRangeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PriceRange... params) {
            mAsyncTaskDao.insertPriceRange(params[0]);
            return null;
        }
    }

    public void insertAll (List<PriceRange> PriceRanges) {
        new InsertAllAsyncTask(priceRangeDao).execute(PriceRanges);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<PriceRange>, Void, Void> {

        private PriceRangeDao mAsyncTaskDao;

        InsertAllAsyncTask(PriceRangeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<PriceRange>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
