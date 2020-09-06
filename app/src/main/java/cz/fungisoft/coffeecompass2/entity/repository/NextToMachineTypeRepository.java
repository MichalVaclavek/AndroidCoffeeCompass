package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.repository.dao.NextToMachineTypeDao;
import io.reactivex.Flowable;

public class NextToMachineTypeRepository {

    private NextToMachineTypeDao nextToMachineTypeDao;
    private LiveData<List<NextToMachineType>> mAllNextToMachineTypes;

    NextToMachineTypeRepository(Context context) {
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(context);
        nextToMachineTypeDao = db.nextToMachineTypeDao();
        mAllNextToMachineTypes = nextToMachineTypeDao.getAllNextToMachineTypes();
    }

    public LiveData<List<NextToMachineType>> getAllNextToMachineTypes() {
        return mAllNextToMachineTypes;
    }

    public Flowable<NextToMachineType> getNextToMachineType(String nextToMachineTypeValue) {
        return nextToMachineTypeDao.getNextToMachineType(nextToMachineTypeValue);
    }

    public void insert (NextToMachineType nextToMachineType) {
        new NextToMachineTypeRepository.insertAsyncTask(nextToMachineTypeDao).execute(nextToMachineType);
    }

    private static class insertAsyncTask extends AsyncTask<NextToMachineType, Void, Void> {

        private NextToMachineTypeDao mAsyncTaskDao;

        insertAsyncTask(NextToMachineTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NextToMachineType... params) {
            mAsyncTaskDao.insertNextToMachineType(params[0]);
            return null;
        }
    }

    public void insertAll (List<NextToMachineType> NextToMachineTypes) {
        new InsertAllAsyncTask(nextToMachineTypeDao).execute(NextToMachineTypes);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<NextToMachineType>, Void, Void> {

        private NextToMachineTypeDao mAsyncTaskDao;

        InsertAllAsyncTask(NextToMachineTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<NextToMachineType>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
