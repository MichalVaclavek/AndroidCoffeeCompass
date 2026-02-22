package cz.fungisoft.coffeecompass2.entity.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteDao;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
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
     * the searching itself in DB can only be performed like searching
     * within square.
     */
    final double MULTIPLY_FACTOR_FROM_CIRCLE_TO_RECTANGLE = 1.4;

    private CoffeeSiteDao coffeeSiteDao;

    private final LiveData<List<CoffeeSite>> mAllCoffeeSites;
    private final LiveData<List<CoffeeSite>> coffeeSitesWithImage;
    private final Single<List<CoffeeSite>> coffeeSitesWithImageSingle;

    // list of coffeeSites created/updated during OFFLINE mode
    private final Single<List<CoffeeSite>> coffeeSitesNotSavedOnServerSingle;

    // list of coffeeSites created/updated during OFFLINE mode
    private final LiveData<List<CoffeeSite>> coffeeSitesNotSavedOnServer;


    // Probably not needed
    private final Flowable<Integer> numberOfSitesWithImage;


    /**
     * Needed for service downloading data for OFFLINE mode
     * @return
     */
    public Single<List<CoffeeSite>> getAllCoffeeSitesWithImageSingle() {
        return coffeeSitesWithImageSingle;
    }


    /**
     * Needed to show and save list of CoffeeSites, which are not saved on server yet
     * @return
     */
    public Single<List<CoffeeSite>> getCoffeeSitesNotSavedOnServerSingle() {
        return coffeeSitesNotSavedOnServerSingle;
    }

    public LiveData<List<CoffeeSite>> getCoffeeSitesNotSavedOnServer() {
        return coffeeSitesNotSavedOnServer;
    }

    public CoffeeSiteRepository(CoffeeSiteDatabase db) {
        super(db);
        coffeeSiteDao = db.coffeeSiteDao();
        mAllCoffeeSites = coffeeSiteDao.getAllCoffeeSites();
        coffeeSitesWithImage = coffeeSiteDao.getAllCoffeeSitesWithImage();
        coffeeSitesWithImageSingle = coffeeSiteDao.getAllCoffeeSitesWithImageSingle();
        numberOfSitesWithImage = coffeeSiteDao.getAllCoffeeSitesWithImageNumber();

        coffeeSitesNotSavedOnServerSingle = coffeeSiteDao.getCoffeeSitesNotSavedOnServerSingle();
        coffeeSitesNotSavedOnServer = coffeeSiteDao.getCoffeeSitesNotSavedOnServer();
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

        public List<Integer> getAllSearchRangeMeters() {
            return allRanges;
        }

        private final double latitudeFrom;
        private final double longitudeFrom;
        private final List<Integer> allRanges;

        public SearchParamsDataInput(double latitudeFrom, double longitudeFrom, List<Integer> allRanges) {
            this.latitudeFrom = latitudeFrom;
            this.longitudeFrom = longitudeFrom;
            this.allRanges = allRanges;
        }
    }

    /**
     * LiveData input holder for coffee sites in range input parameters
     */
    private final MutableLiveData<SearchParamsDataInput> searchLatLongRangeInput = new MutableLiveData<>();

    private void setLatLongRangeInput(double latitudeFrom, double longitudeFrom, List<Integer> allRanges) {
        searchLatLongRangeInput.setValue(new SearchParamsDataInput(latitudeFrom, longitudeFrom, allRanges));
    }

    public void setNewLatLongRangeSearchCriteriaForAllRanges(double latitudeFrom, double longitudeFrom, List<Integer> allRanges) {
        setLatLongRangeInput(latitudeFrom, longitudeFrom, allRanges);
    }

    private final Map<String, LiveData<List<CoffeeSite>>> coffeeSitesInRangeWithRange = new HashMap<>();

    private final LiveData<Map<String, LiveData<List<CoffeeSite>>>> coffeeSitesInRangeWithRangeLive = Transformations.map(searchLatLongRangeInput, input -> {
        for (Integer range : input.getAllSearchRangeMeters()) {
            double searchRangeAsDegreePart = range * MULTIPLY_FACTOR_FROM_CIRCLE_TO_RECTANGLE * ONE_METER_IN_DEGREE;
            coffeeSitesInRangeWithRange.put(range.toString(), coffeeSiteDao.getCoffeeSitesInRectangleLiveData(input.getLatitudeFrom(), input.getLongitudeFrom(), searchRangeAsDegreePart));
        }
        return coffeeSitesInRangeWithRange;
    });

    public LiveData<Map<String, LiveData<List<CoffeeSite>>>> getCoffeeSitesInRangeWithRange() {
        return coffeeSitesInRangeWithRangeLive;
    }

    public Single<List<CoffeeSite>> getCoffeeSitesInTownSingle(String townName) {
        return coffeeSiteDao.getCoffeeSitesInTownSingle(townName);
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

    public Maybe<CoffeeSite> getCoffeeSiteByIdMaybe(long siteId) {
        return coffeeSiteDao.getCoffeeSiteByIdMaybe(siteId);
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

    // list of coffeeSites saved in DB (downloaded) and not modified, i.e. saved on server from user
    LiveData<List<CoffeeSite>> coffeeSitesFromUserSavedOnServer = Transformations.switchMap(coffeeSiteAuthorUserNameInput, userName -> coffeeSiteDao.getCoffeeSitesFromUserSavedOnServer(userName));

    public LiveData<List<CoffeeSite>> getCoffeeSitesFromUserSavedOnServer(String userName) {
        setCoffeeSiteAuthorUserNameInput(userName);
        return coffeeSitesFromUserSavedOnServer;
    }

    /* Data for MainAppWidget */

    public Single<List<CoffeeSite>> getCoffeeSitesInRangeSingle(double latitudeFrom, double longitudeFrom, double searchRange) {
        double searchRangeAsDegreePart = searchRange * MULTIPLY_FACTOR_FROM_CIRCLE_TO_RECTANGLE * ONE_METER_IN_DEGREE;
        return coffeeSiteDao.getCoffeeSitesInRectangleSingle(latitudeFrom, longitudeFrom, searchRangeAsDegreePart);
    }

    /* Get number of CoffeeSites not saved on server, i.e. only saved in local DB */
    public LiveData<Integer> getNumOfCoffeeSitesNotSavedOnServer() {
        return coffeeSiteDao.getNumOfCoffeeSitesNotSavedOnServer();
    }

    /* Delete CoffeeSites not saved on server. Used when they are already saved on server */

    public void deleteCoffeeSiteSavedOnServerFromDB() {
        new DeleteCoffeeSitesNotSavedOnServerAsyncT(coffeeSiteDao).execute();
    }

    /**
     * Deletes the CoffeeSites created in Offline mode, in the background.
     *  Used when they are already saved on server
     */
    private static class DeleteCoffeeSitesNotSavedOnServerAsyncT {

        private final CoffeeSiteDao mAsyncTaskDao;

        DeleteCoffeeSitesNotSavedOnServerAsyncT(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute() {
            AsyncRunner.runInBackground(() -> {
                //int deletedNum = mAsyncTaskDao.deleteAllCreatedAndNotSavedOnServer(CoffeeSiteRecordStatus.CREATED);
                int deletedNum = mAsyncTaskDao.deleteAllNotSavedOnServer();
                Log.i("DeleteCoffeeSitesAsyncT", "CoffeeSites not saved on server deleted. " + deletedNum);
            });
        }
    }



    //*** Delete CoffeeSite from DB **/
    //*** Must run in a separate thread */
    public void deleteCoffeeSiteFromDB(CoffeeSite coffeeSite) {
        new DeleteAsyncTask(coffeeSiteDao).execute(coffeeSite);
    }

    private static class DeleteAsyncTask {

        private final CoffeeSiteDao mAsyncTaskDao;

        DeleteAsyncTask(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final CoffeeSite... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.delete(params[0]));
        }
    }

    /* ***** Update CoffeeSite procedures and AsyncTask ***** */

    public void update(CoffeeSite coffeeSite) {
        new UpdateAsyncTask(coffeeSiteDao).execute(coffeeSite);
    }

    private static class UpdateAsyncTask {

        private final CoffeeSiteDao mAsyncTaskDao;

        UpdateAsyncTask(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final CoffeeSite... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.updateCoffeeSite(params[0]));
        }
    }

    /* ***** Insert CoffeeSite procedures and AsyncTask ***** */

    public void insert (CoffeeSite coffeeSite) {
        new InsertAsyncTask(coffeeSiteDao).execute(coffeeSite);
    }

    private static class InsertAsyncTask {

        private final CoffeeSiteDao mAsyncTaskDao;

        InsertAsyncTask(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final CoffeeSite... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertCoffeeSite(params[0]));
        }
    }

    public void insertAll (List<CoffeeSite> coffeeSites) {
        new CoffeeSiteRepository.InsertAllAsyncTask(coffeeSiteDao).execute(coffeeSites);
    }

    private static class InsertAllAsyncTask {

        private final CoffeeSiteDao mAsyncTaskDao;

        InsertAllAsyncTask(CoffeeSiteDao dao) {
            mAsyncTaskDao = dao;
        }

        @SafeVarargs
        public final void execute(List<CoffeeSite>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAll(lists[0]));
        }
    }

}
