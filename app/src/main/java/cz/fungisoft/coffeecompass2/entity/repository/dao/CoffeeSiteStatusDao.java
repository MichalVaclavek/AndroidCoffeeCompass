package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import io.reactivex.Single;

@Dao
public interface CoffeeSiteStatusDao {

    @Query("SELECT * FROM coffee_site_status_table")
    LiveData<List<CoffeeSiteStatus>> getAllCoffeeSiteStatuses();

    @Query("SELECT * FROM coffee_site_status_table")
    Single<List<CoffeeSiteStatus>> getAllCoffeeSiteStatusesSingle();

    @Query("SELECT * FROM coffee_site_status_table WHERE status = :stringValue LIMIT 1")
    Single<CoffeeSiteStatus> getCoffeeSiteStatus(String stringValue);

    @Query("DELETE FROM coffee_site_status_table")
    void deleteAll();

    @Insert
    void insertAll(List<CoffeeSiteStatus> coffeeSiteStatuses);

    @Insert
    void insertCoffeeSiteStatus(CoffeeSiteStatus coffeeSiteStatuse);
}
