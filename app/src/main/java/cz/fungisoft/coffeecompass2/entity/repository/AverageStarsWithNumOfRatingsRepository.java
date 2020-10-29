package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfRatings;
import cz.fungisoft.coffeecompass2.entity.repository.dao.AverageStarsWithNumOfRatingsDao;

/**
 * Repository class for AverageStarsWithNumOfRating objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class AverageStarsWithNumOfRatingsRepository extends CoffeeSiteRepositoryBase {

    private final AverageStarsWithNumOfRatingsDao averageStarsWithNumOfHodnoceniDao;
    private final LiveData<List<AverageStarsWithNumOfRatings>> mAllCoffeeSiteTypes;

    AverageStarsWithNumOfRatingsRepository(CoffeeSiteDatabase db) {
        super(db);
        averageStarsWithNumOfHodnoceniDao = db.averageStarsWithNumOfHodnoceniDao();
        mAllCoffeeSiteTypes = averageStarsWithNumOfHodnoceniDao.getAllAverageStarsWithNumOfHodnoceni();
    }

    public LiveData<List<AverageStarsWithNumOfRatings>> getAllAverageStarsWithNumOfHodnoceni() {
        return mAllCoffeeSiteTypes;
    }

    public void insert (AverageStarsWithNumOfRatings averageStarsWithNumOfHodnoceni) {
        new AverageStarsWithNumOfRatingsRepository.insertAsyncTask(averageStarsWithNumOfHodnoceniDao).execute(averageStarsWithNumOfHodnoceni);
    }

    private static class insertAsyncTask extends AsyncTask<AverageStarsWithNumOfRatings, Void, Void> {

        private AverageStarsWithNumOfRatingsDao mAsyncTaskDao;

        insertAsyncTask(AverageStarsWithNumOfRatingsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final AverageStarsWithNumOfRatings... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void insertAll (List<AverageStarsWithNumOfRatings> averageStarsWithNumOfHodnoceniList) {
        new InsertAllAsyncTask(averageStarsWithNumOfHodnoceniDao).execute(averageStarsWithNumOfHodnoceniList);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<AverageStarsWithNumOfRatings>, Void, Void> {

        private final AverageStarsWithNumOfRatingsDao mAsyncTaskDao;

        InsertAllAsyncTask(AverageStarsWithNumOfRatingsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<AverageStarsWithNumOfRatings>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
