package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.repository.dao.OtherOfferDao;
import io.reactivex.Single;

/**
 * Repository class for OtherOffer objects.
 * Provides LiveData and/or other Reactive classes.
 */
public class OtherOfferRepository extends CoffeeSiteRepositoryBase {

    private final OtherOfferDao otherOfferDao;
    private final LiveData<List<OtherOffer>> mAllOtherOffers;

    public LiveData<List<OtherOffer>> getAllOtherOffers() {
        return mAllOtherOffers;
    }

    private final Single<List<OtherOffer>> mAllOtherOffersSingle;

    public Single<List<OtherOffer>> geAlltOtherOffersSingle() {
        return mAllOtherOffersSingle;
    }


    public Single<OtherOffer> getOtherOffer(String otherOfferValue) {
        return otherOfferDao.getOtherOffer(otherOfferValue);
    }

    OtherOfferRepository(CoffeeSiteDatabase db) {
        super(db);
        otherOfferDao = db.otherOfferDao();
        mAllOtherOffers = otherOfferDao.getAllOtherOffers();
        mAllOtherOffersSingle = otherOfferDao.getOtherOffersSingle();
    }


    public void insert (OtherOffer otherOffer) {
        new OtherOfferRepository.insertAsyncTask(otherOfferDao).execute(otherOffer);
    }

    private static class insertAsyncTask {

        private final OtherOfferDao mAsyncTaskDao;

        insertAsyncTask(OtherOfferDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final OtherOffer... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertOtherOffer(params[0]));
        }
    }

    public void insertAll (List<OtherOffer> OtherOffers) {
        new InsertAllAsyncTask(otherOfferDao).execute(OtherOffers);
    }

    private static class InsertAllAsyncTask {

        private final OtherOfferDao mAsyncTaskDao;

        InsertAllAsyncTask(OtherOfferDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<OtherOffer>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
