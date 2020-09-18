package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass2.entity.repository.dao.StarsQualityDescriptionDao;
import io.reactivex.Flowable;

public class StarsQualityDescriptionRepository extends CoffeeSiteRepositoryBase {

    private StarsQualityDescriptionDao starsQualityDescriptionDao;
    private LiveData<List<StarsQualityDescription>> mAllStarsQualityDescriptions;

    StarsQualityDescriptionRepository(CoffeeSiteDatabase db) {
        super(db);
        starsQualityDescriptionDao = db.starsQualityDescriptionDao();
        mAllStarsQualityDescriptions = starsQualityDescriptionDao.getAllStarsQualityDescriptions();
    }

    public LiveData<List<StarsQualityDescription>> getAllStarsQualityDescriptions() {
        return mAllStarsQualityDescriptions;
    }

    public Flowable<StarsQualityDescription> getStarsQualityDescription(int starsQualityDescriptionValue) {
        return starsQualityDescriptionDao.getStarsQualityDescriptionByNumber(starsQualityDescriptionValue);
    }

    public void insert (StarsQualityDescription starsQualityDescription) {
        new StarsQualityDescriptionRepository.insertAsyncTask(starsQualityDescriptionDao).execute(starsQualityDescription);
    }

    private static class insertAsyncTask extends AsyncTask<StarsQualityDescription, Void, Void> {

        private StarsQualityDescriptionDao mAsyncTaskDao;

        insertAsyncTask(StarsQualityDescriptionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final StarsQualityDescription... params) {
            mAsyncTaskDao.insertStarsQualityDescription(params[0]);
            return null;
        }
    }

    public void insertAll (List<StarsQualityDescription> StarsQualityDescriptions) {
        new InsertAllAsyncTask(starsQualityDescriptionDao).execute(StarsQualityDescriptions);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<StarsQualityDescription>, Void, Void> {

        private StarsQualityDescriptionDao mAsyncTaskDao;

        InsertAllAsyncTask(StarsQualityDescriptionDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<StarsQualityDescription>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
