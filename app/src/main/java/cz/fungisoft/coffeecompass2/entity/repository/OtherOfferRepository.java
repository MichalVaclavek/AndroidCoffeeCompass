package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

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

    OtherOfferRepository(CoffeeSiteDatabase db) {
        super(db);
        otherOfferDao = db.otherOfferDao();
        mAllOtherOffers = otherOfferDao.getAllOtherOffers();
    }

    public LiveData<List<OtherOffer>> getAllOtherOffers() {
        return mAllOtherOffers;
    }

    public Single<OtherOffer> getOtherOffer(String otherOfferValue) {
        return otherOfferDao.getOtherOffer(otherOfferValue);
    }

    public void insert (OtherOffer otherOffer) {
        new OtherOfferRepository.insertAsyncTask(otherOfferDao).execute(otherOffer);
    }

    private static class insertAsyncTask extends AsyncTask<OtherOffer, Void, Void> {

        private OtherOfferDao mAsyncTaskDao;

        insertAsyncTask(OtherOfferDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final OtherOffer... params) {
            mAsyncTaskDao.insertOtherOffer(params[0]);
            return null;
        }
    }

    public void insertAll (List<OtherOffer> OtherOffers) {
        new InsertAllAsyncTask(otherOfferDao).execute(OtherOffers);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<OtherOffer>, Void, Void> {

        private final OtherOfferDao mAsyncTaskDao;

        InsertAllAsyncTask(OtherOfferDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<OtherOffer>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
