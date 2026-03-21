package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.repository.dao.relations.CoffeeSiteWithComments;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface CoffeeSiteDao {

    @Query("SELECT * FROM coffee_site_table")
    LiveData<List<CoffeeSite>> getAllCoffeeSites();

    @Query("SELECT * FROM coffee_site_table WHERE mainImageURL != \"\" ORDER BY createdOn ASC")
    LiveData<List<CoffeeSite>> getAllCoffeeSitesWithImage();

    @Query("SELECT * FROM coffee_site_table WHERE mainImageURL != \"\" ORDER BY createdOn ASC")
    Single<List<CoffeeSite>> getAllCoffeeSitesWithImageSingle();

    @Query("SELECT COUNT(*) FROM coffee_site_table WHERE mainImageURL != \"\"")
    Flowable<Integer> getAllCoffeeSitesWithImageNumber();

    // savedOnServer == 0 means false
    @Query("SELECT * FROM coffee_site_table WHERE savedOnServer == 0 ORDER BY createdOn ASC")
    Single<List<CoffeeSite>> getCoffeeSitesNotSavedOnServerSingle();

    // savedOnServer == 0 means false
    @Query("SELECT * FROM coffee_site_table WHERE savedOnServer == 0 ORDER BY createdOn ASC")
    LiveData<List<CoffeeSite>> getCoffeeSitesNotSavedOnServer();

    // savedOnServer == 0 means false
    @Query("SELECT COUNT(id) FROM coffee_site_table WHERE savedOnServer == 0")
    LiveData<Integer> getNumOfCoffeeSitesNotSavedOnServer();

    /**
     *
     * @param searchRangeAsDegreePart to be calculated as searchRange in meters * {@link ONE_METER_IN_DEGREE}
     * @return
     */
    @Query("SELECT * FROM coffee_site_table WHERE zemSirka > (:latitudeFrom - :searchRangeAsDegreePart) " +
            "AND zemSirka < (:latitudeFrom + :searchRangeAsDegreePart) " +
            "AND zemDelka > (:longitudeFrom - :searchRangeAsDegreePart) " +
            "AND zemDelka < (:longitudeFrom + :searchRangeAsDegreePart) " +
            "AND statusZaznamu IS NOT NULL")
    LiveData<List<CoffeeSite>> getCoffeeSitesInRectangleLiveData(double latitudeFrom, double longitudeFrom, double searchRangeAsDegreePart);

    /**
     *
     * @param searchRangeAsDegreePart to be calculated as searchRange in meters * {@link ONE_METER_IN_DEGREE}
     * @return
     */
    @Query("SELECT * FROM coffee_site_table WHERE zemSirka > (:latitudeFrom - :searchRangeAsDegreePart) " +
            "AND zemSirka < (:latitudeFrom + :searchRangeAsDegreePart) " +
            "AND zemDelka > (:longitudeFrom - :searchRangeAsDegreePart) " +
            "AND zemDelka < (:longitudeFrom + :searchRangeAsDegreePart) " +
            "AND statusZaznamu IS NOT NULL")
    Single<List<CoffeeSite>> getCoffeeSitesInRectangleSingle(double latitudeFrom, double longitudeFrom, double searchRangeAsDegreePart);

    /**
     *
     * @param townName
     * @return
     */
    @Query("SELECT * FROM coffee_site_table WHERE mesto LIKE :townName")
    Single<List<CoffeeSite>> getCoffeeSitesInTownSingle(String townName);


    /**
     *
     * @param userName
     * @return
     */
    @Query("SELECT * FROM coffee_site_table WHERE originalUserName LIKE :userName ORDER BY createdOn DESC")
    LiveData<List<CoffeeSite>> getCoffeeSitesFromUser(String userName);

    /**
     * CoffeeSites from User saved on server i.e. not modified nor completely new CoffeeSites
     *
     * savedOnServer == 1 means true
     * @param userName
     * @return
     */
    @Query("SELECT * FROM coffee_site_table WHERE originalUserName LIKE :userName AND savedOnServer == 1 ORDER BY createdOn DESC")
    LiveData<List<CoffeeSite>> getCoffeeSitesFromUserSavedOnServer(String userName);


    @Query("SELECT * FROM coffee_site_table WHERE id = :id LIMIT 1")
    LiveData<CoffeeSite> getCoffeeSiteById(long id);

    @Query("SELECT * FROM coffee_site_table WHERE id = :id LIMIT 1")
    Maybe<CoffeeSite> getCoffeeSiteByIdMaybe(long id);

    @Query("SELECT * FROM coffee_site_table WHERE siteName = :coffeeSiteName LIMIT 1")
    Flowable<CoffeeSite> getCoffeeSiteByName(String coffeeSiteName);

    @Delete
    void delete(CoffeeSite coffeeSite);

    @Query("DELETE FROM coffee_site_table WHERE id = :coffeeSiteId")
    int deleteById(String coffeeSiteId);

    @Query("DELETE FROM coffee_site_table")
    void deleteAll();

    @Query("DELETE FROM coffee_site_table WHERE savedOnServer == 1")
    void deleteAllExceptNotSavedOnServer();

    // Used after all new, previously not saved CoffeeSites, were saved now
    // Keeps those not saved, which were only modified, i.e. those downloaded
    // for offline mode from server and modified when Offline
    @Query("DELETE FROM coffee_site_table WHERE savedOnServer == 0")
    int deleteAllNotSavedOnServer();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CoffeeSite> coffeeSites);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCoffeeSite(CoffeeSite coffeeSite);

    @Transaction
    @Update(entity = CoffeeSite.class)
    void updateCoffeeSite(CoffeeSite coffeeSite);

    @Transaction
    @Query("SELECT * FROM coffee_site_table")
    List<CoffeeSiteWithComments> getAllCoffeeSitesWithComments();

}
