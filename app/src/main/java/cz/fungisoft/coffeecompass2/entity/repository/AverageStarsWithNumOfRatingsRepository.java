package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
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

    private static class insertAsyncTask {

        private final AverageStarsWithNumOfRatingsDao mAsyncTaskDao;

        insertAsyncTask(AverageStarsWithNumOfRatingsDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final AverageStarsWithNumOfRatings... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insert(params[0]));
        }
    }

    public void insertAll (List<AverageStarsWithNumOfRatings> averageStarsWithNumOfHodnoceniList) {
        new InsertAllAsyncTask(averageStarsWithNumOfHodnoceniDao).execute(averageStarsWithNumOfHodnoceniList);
    }

    private static class InsertAllAsyncTask {

        private final AverageStarsWithNumOfRatingsDao mAsyncTaskDao;

        InsertAllAsyncTask(AverageStarsWithNumOfRatingsDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<AverageStarsWithNumOfRatings>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
