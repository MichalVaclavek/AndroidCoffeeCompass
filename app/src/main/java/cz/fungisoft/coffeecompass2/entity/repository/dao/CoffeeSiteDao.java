package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.dao.relations.CoffeeSiteWithComments;
import io.reactivex.Flowable;

@Dao
public interface CoffeeSiteDao {

    @Query("SELECT * FROM coffee_site_table")
    LiveData<List<CoffeeSite>> getAllCoffeeSites();

    /**
     *
     * @param searchRangeAsDegreePart to be calculated as searchRange in meters * {@link ONE_METER_IN_DEGREE}
     * @return
     */
    @Query("SELECT * FROM coffee_site_table WHERE zemSirka > (:latitudeFrom - :searchRangeAsDegreePart)" +
            "AND zemSirka < (:latitudeFrom + :searchRangeAsDegreePart)" +
            "AND zemDelka > (:longitudeFrom - :searchRangeAsDegreePart)" +
            "AND zemDelka < (:longitudeFrom + :searchRangeAsDegreePart)")
    LiveData<List<CoffeeSite>> getCoffeeSitesInRectangle(double latitudeFrom, double longitudeFrom, double searchRangeAsDegreePart);

    /**
     *
     * @param userName
     * @return
     */
    @Query("SELECT * FROM coffee_site_table WHERE originalUserName LIKE :userName")
    LiveData<List<CoffeeSite>> getCoffeeSitesFromUser(String userName);


    @Query("SELECT * FROM coffee_site_table WHERE id = :id LIMIT 1")
    LiveData<CoffeeSite> getCoffeeSiteById(long id);

    @Query("SELECT * FROM coffee_site_table WHERE siteName = :coffeeSiteName LIMIT 1")
    Flowable<CoffeeSite> getCoffeeSiteByName(String coffeeSiteName);

    @Query("DELETE FROM coffee_site_table")
    void deleteAll();

    @Insert
    void insertAll(List<CoffeeSite> coffeeSites);

    @Insert
    void insertCoffeeSite(CoffeeSite coffeeSite);

    @Transaction
    @Query("SELECT * FROM coffee_site_table")
    List<CoffeeSiteWithComments> getCoffeeSitesWithComments();

    @Transaction
    @Query("SELECT * FROM coffee_site_table WHERE id = :coffeeSiteId LIMIT 1")
    List<CoffeeSiteWithComments> getCoffeeSiteWithComments(long coffeeSiteId);

//    @Transaction
//    @Insert
//    long setCoffeeSiteWithComments(CoffeeSiteWithComments coffeeSiteWithComments);


}
