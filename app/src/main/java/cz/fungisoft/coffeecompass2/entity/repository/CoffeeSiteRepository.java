package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteDao;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Repository to work with CoffeeSite's DAO.<br>
 * Provides LiveData<List<CoffeeSite>> of all CoffeeSites, found CoffeeSites in range
 * and CoffeeSites created by a User.
 */
public class CoffeeSiteRepository extends CoffeeSiteRepositoryBase {

    final double ONE_METER_IN_DEGREE = 1/(40_070_000d/360); // one meter on Earth as a part of one degree 6372000

    /**
     * A multiply factor, found by experiment, which has to be used,
     * when searching from current location within circle range, when
     * the searching itself in DB can be only performed like searching
     * within square.
     */
    final double MULTIPLY_FACTOR_FROM_CIRCLE_TO_RECTANGLE = 1.4;

    private CoffeeSiteDao coffeeSiteDao;

    private final LiveData<List<CoffeeSite>> mAllCoffeeSites;
    private final LiveData<List<CoffeeSite>> coffeeSitesWithImage;
    // Probably not needed
    private final Flowable<Integer> numberOfSitesWithImage;


    public CoffeeSiteRepository(CoffeeSiteDatabase db) {
        super(db);
        coffeeSiteDao = db.coffeeSiteDao();
        mAllCoffeeSites = coffeeSiteDao.getAllCoffeeSites();
        coffeeSitesWithImage = coffeeSiteDao.getAllCoffeeSitesWithImage();
        numberOfSitesWithImage = coffeeSiteDao.getAllCoffeeSitesWithImageNumber();
    }

    public LiveData<List<CoffeeSite>> getAllCoffeeSites() {
        return mAllCoffeeSites;
    }

    public LiveData<List<CoffeeSite>> getAllCoffeeSitesWithImage() {
        return coffeeSitesWithImage;
    }

    public  Flowable<Integer> getNumberOfAllCoffeeSitesWithImage() {
        return numberOfSitesWithImage;
    }

    /**
     * Inner class to hold input data of CoffeeSites LiveData searching parameters.
     */
    static class SearchParamsDataInput {

        public double getLatitudeFrom() {
            return latitudeFrom;
        }

        public double getLongitudeFrom() {
            return longitudeFrom;
        }

        public double getSearchRangeAsDegreePart() {
            return searchRangeAsDegreePart;
        }

        private final double latitudeFrom;
        private final double longitudeFrom;
        private final double searchRangeAsDegreePart;

        public SearchParamsDataInput(double latitudeFrom, double longitudeFrom, double searchRangeAsDegreePart) {
            this.latitudeFrom = latitudeFrom;
            this.longitudeFrom = longitudeFrom;
            this.searchRangeAsDegreePart = searchRangeAsDegreePart;
        }
    }

    /**
     * LiveData input holder for coffee sites in range input parameters
     */
    private final MutableLiveData<SearchParamsDataInput> searchParamsInput = new MutableLiveData<>();

    private void setInput(double latitudeFrom, double longitudeFrom, double searchRangeAsDegreePart) {
        searchParamsInput.setValue(new SearchParamsDataInput(latitudeFrom, longitudeFrom, searchRangeAsDegreePart));
    }

    private final LiveData<List<CoffeeSite>> coffeeSitesInRange =
            Transformations.switchMap(searchParamsInput, (input) -> coffeeSiteDao.getCoffeeSitesInRectangleLiveData(input.getLatitudeFrom(), input.getLongitudeFrom(), input.getSearchRangeAsDegreePart()));


    public void setNewSearchCriteria(double latitudeFrom, double longitudeFrom, int searchRangeInMeters) {
        double searchRangeAsDegreePart = searchRangeInMeters * MULTIPLY_FACTOR_FROM_CIRCLE_TO_RECTANGLE * ONE_METER_IN_DEGREE;
        setInput(latitudeFrom, longitudeFrom, searchRangeAsDegreePart);
    }

    public LiveData<List<CoffeeSite>> getCoffeeSitesInRange() {
        return coffeeSitesInRange;
    }

    /**
     * LiveData input holder for CoffeeSite ID
     */
    private final MutableLiveData<Long> coffeeSiteIdInput = new MutableLiveData<>();

    private void setCoffeeSiteIdInput(long coffeeSiteId) {
        coffeeSiteIdInput.setValue(coffeeSiteId);
    }

    LiveData<CoffeeSite> coffeeSiteLive = Transformations.switchMap(coffeeSiteIdInput, csId -> coffeeSiteDao.getCoffeeSiteById(csId));

    public LiveData<CoffeeSite> getCoffeeSiteById(long siteId) {
        setCoffeeSiteIdInput(siteId);
        return coffeeSiteLive;
    }

    /**
     * LiveData input holder for CoffeeSite creator user name
     */
    private final MutableLiveData<String> coffeeSiteAuthorUserNameInput = new MutableLiveData<>();

    private void setCoffeeSiteAuthorUserNameInput(String userName) {
        coffeeSiteAuthorUserNameInput.setValue(userName);
    }

    LiveData<List<CoffeeSite>> coffeeSitesFromUser = Transformations.switchMap(coffeeSiteAuthorUserNameInput, userName -> coffeeSiteDao.getCoffeeSitesFromUser(userName));

    public LiveData<List<CoffeeSite>> getCoffeeSitesByAuthorUserName(String userName) {
        setCoffeeSiteAuthorUserNameInput(userName);
        return coffeeSitesFromUser;
    }

    /* Data for MainAppWidget */

    public Single<List<CoffeeSite>> getCoffeeSitesInRangeSingle(double latitudeFrom, double longitudeFrom, double searchRange) {
        double searchRangeAsDegreePart = searchRange * MULTIPLY_FACTOR_FROM_CIRCLE_TO_RECTANGLE * ONE_METER_IN_DEGREE;
        return coffeeSiteDao.getCoffeeSitesInRectangleSingle(latitudeFrom, longitudeFrom, searchRangeAsDegreePart);
    }

    /* ***** Insert CoffeeSite procedures and AsyncTask ***** */

    public void insert (CoffeeSite coffeeSite) {
        new insertAsyncTask(coffeeSiteDao).execute(coffeeSite);
    }

    private static class insertAsyncTask extends AsyncTask<CoffeeSite, Void, Void> {

        private final CoffeeSiteDao mAsyncTaskDao;

        insertAsyncTask(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CoffeeSite... params) {
            mAsyncTaskDao.insertCoffeeSite(params[0]);
            return null;
        }
    }

    public void insertAll (List<CoffeeSite> coffeeSites) {
        new CoffeeSiteRepository.InsertAllAsyncTask(coffeeSiteDao).execute(coffeeSites);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<CoffeeSite>, Void, Void> {

        private final CoffeeSiteDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<CoffeeSite>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
