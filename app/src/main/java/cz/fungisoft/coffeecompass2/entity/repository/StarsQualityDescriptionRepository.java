package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass2.entity.repository.dao.StarsQualityDescriptionDao;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Repository class for StarsQualityDescription objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class StarsQualityDescriptionRepository extends CoffeeSiteRepositoryBase {

    private final StarsQualityDescriptionDao starsQualityDescriptionDao;
    private final LiveData<List<StarsQualityDescription>> mAllStarsQualityDescriptions;
    private final Single<List<StarsQualityDescription>> mAllStarsQualityDescriptionsSingle;


    StarsQualityDescriptionRepository(CoffeeSiteDatabase db) {
        super(db);
        starsQualityDescriptionDao = db.starsQualityDescriptionDao();
        mAllStarsQualityDescriptions = starsQualityDescriptionDao.getAllStarsQualityDescriptions();
        mAllStarsQualityDescriptionsSingle = starsQualityDescriptionDao.getAllStarsQualityDescriptionsSingle();
    }

    public LiveData<List<StarsQualityDescription>> getAllStarsQualityDescriptions() {
        return mAllStarsQualityDescriptions;
    }

    public Single<List<StarsQualityDescription>> getAllCoffeeSiteTypesSingle() {
        return mAllStarsQualityDescriptionsSingle;
    }


    public Flowable<StarsQualityDescription> getStarsQualityDescription(int starsQualityDescriptionValue) {
        return starsQualityDescriptionDao.getStarsQualityDescriptionByNumber(starsQualityDescriptionValue);
    }

    public void insert (StarsQualityDescription starsQualityDescription) {
        new StarsQualityDescriptionRepository.insertAsyncTask(starsQualityDescriptionDao).execute(starsQualityDescription);
    }

    private static class insertAsyncTask {

        private final StarsQualityDescriptionDao mAsyncTaskDao;

        insertAsyncTask(StarsQualityDescriptionDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final StarsQualityDescription... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertStarsQualityDescription(params[0]));
        }
    }

    public void insertAll (List<StarsQualityDescription> StarsQualityDescriptions) {
        new InsertAllAsyncTask(starsQualityDescriptionDao).execute(StarsQualityDescriptions);
    }

    private static class InsertAllAsyncTask {

        private final StarsQualityDescriptionDao mAsyncTaskDao;

        InsertAllAsyncTask(StarsQualityDescriptionDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<StarsQualityDescription>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
