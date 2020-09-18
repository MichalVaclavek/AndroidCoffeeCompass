package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.relations.CoffeeSiteWithCsStatus;
import io.reactivex.Flowable;

@Dao
public interface CoffeeSiteDao {

    static final double ONE_METER_IN_DEGREE = 8.988764E-6; // one meter on Earth as a part of one degree

    @Transaction
    @Query("SELECT * FROM coffee_site_table")
    LiveData<List<CoffeeSiteWithCsStatus>> loadCoffeeSiteWithCsStatuses();

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

    @Query("SELECT * FROM coffee_site_table WHERE id = :id LIMIT 1")
    Flowable<CoffeeSite> getCoffeeSiteById(int id);

    @Query("SELECT * FROM coffee_site_table WHERE siteName = :coffeeSiteName LIMIT 1")
    Flowable<CoffeeSite> getCoffeeSiteByName(String coffeeSiteName);

    @Query("DELETE FROM coffee_site_table")
    void deleteAll();

    @Insert
    void insertAll(List<CoffeeSite> coffeeSites);

    @Insert
    void insertCoffeeSite(CoffeeSite coffeeSite);

}
