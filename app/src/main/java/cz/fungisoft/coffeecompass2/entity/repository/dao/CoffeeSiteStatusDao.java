package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import io.reactivex.Flowable;

@Dao
public interface CoffeeSiteStatusDao {

//    @Transaction
//    @Query("SELECT * FROM coffee_site_table WHERE id LIKE :coffeeSiteId")
//    public List<CoffeeSiteWithCsStatus> getCoffeeSiteWithStatus(String coffeeSiteId);

    @Query("SELECT * FROM coffee_site_status_table")
    LiveData<List<CoffeeSiteStatus>> getAllCoffeeSiteStatuses();

    @Query("SELECT * FROM coffee_site_status_table WHERE status LIKE :stringValue LIMIT 1")
    Flowable<CoffeeSiteStatus> getCoffeeSiteStatus(String stringValue);

    @Query("DELETE FROM coffee_site_status_table")
    void deleteAll();

    @Insert
    void insertAll(List<CoffeeSiteStatus> coffeeSiteStatuses);

    @Insert
    void insertCoffeeSiteStatus(CoffeeSiteStatus coffeeSiteStatuse);
}
