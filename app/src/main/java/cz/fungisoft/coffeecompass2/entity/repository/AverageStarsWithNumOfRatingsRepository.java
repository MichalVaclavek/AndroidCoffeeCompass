package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfRatings;
import cz.fungisoft.coffeecompass2.entity.repository.dao.AverageStarsWithNumOfRatingsDao;

public class AverageStarsWithNumOfRatingsRepository {

    private AverageStarsWithNumOfRatingsDao averageStarsWithNumOfHodnoceniDao;
    private LiveData<List<AverageStarsWithNumOfRatings>> mAllCoffeeSiteTypes;

    AverageStarsWithNumOfRatingsRepository(Context context) {
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(context);
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

        private AverageStarsWithNumOfRatingsDao mAsyncTaskDao;

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
