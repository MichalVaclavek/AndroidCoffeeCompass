package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CupTypeDao;
import io.reactivex.Flowable;

public class CupTypeRepository extends CoffeeSiteRepositoryBase {

    private CupTypeDao cupTypeDao;
    private LiveData<List<CupType>> mAllCupTypes;

    CupTypeRepository(CoffeeSiteDatabase db) {
        super(db);
        cupTypeDao = db.cupTypeDao();
        mAllCupTypes = cupTypeDao.getAllCupTypes();
    }

    public LiveData<List<CupType>> getAllCupTypes() {
        return mAllCupTypes;
    }

    public Flowable<CupType> getCupType(String cupTypeValue) {
        return cupTypeDao.getCupType(cupTypeValue);
    }

    public void insert (CupType cupType) {
        new CupTypeRepository.insertAsyncTask(cupTypeDao).execute(cupType);
    }

    private static class insertAsyncTask extends AsyncTask<CupType, Void, Void> {

        private CupTypeDao mAsyncTaskDao;

        insertAsyncTask(CupTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CupType... params) {
            mAsyncTaskDao.insertCupType(params[0]);
            return null;
        }
    }

    public void insertAll (List<CupType> CupTypes) {
        new InsertAllAsyncTask(cupTypeDao).execute(CupTypes);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<CupType>, Void, Void> {

        private CupTypeDao mAsyncTaskDao;

        InsertAllAsyncTask(CupTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<CupType>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
