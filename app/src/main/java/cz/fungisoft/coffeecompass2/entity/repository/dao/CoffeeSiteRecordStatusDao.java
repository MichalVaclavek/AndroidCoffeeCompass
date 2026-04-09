package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import io.reactivex.Single;

@Dao
public interface CoffeeSiteRecordStatusDao {

    @Query("SELECT * FROM coffee_site_record_status_table")
    LiveData<List<CoffeeSiteRecordStatus>> getAllCSRecordStatuses();

    @Query("SELECT * FROM coffee_site_record_status_table WHERE status = :stringValue LIMIT 1")
    Single<CoffeeSiteRecordStatus> getCoffeeSiteRecordStatus(String stringValue);

    @Query("DELETE FROM coffee_site_record_status_table")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CoffeeSiteRecordStatus> csRecordStatuses);

    @Insert
    void insert(CoffeeSiteRecordStatus csRecordStatuse);
}
