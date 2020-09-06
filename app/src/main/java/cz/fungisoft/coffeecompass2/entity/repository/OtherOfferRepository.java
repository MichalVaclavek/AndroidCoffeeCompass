package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.repository.dao.OtherOfferDao;
import io.reactivex.Flowable;

public class OtherOfferRepository {

    private OtherOfferDao otherOfferDao;
    private LiveData<List<OtherOffer>> mAllOtherOffers;

    OtherOfferRepository(Context context) {
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(context);
        otherOfferDao = db.otherOfferDao();
        mAllOtherOffers = otherOfferDao.getAllOtherOffers();
    }

    public LiveData<List<OtherOffer>> getAllOtherOffers() {
        return mAllOtherOffers;
    }

    public Flowable<OtherOffer> getOtherOffer(String otherOfferValue) {
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

        private OtherOfferDao mAsyncTaskDao;

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
