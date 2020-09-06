package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import io.reactivex.Flowable;

@Dao
public interface CoffeeSiteRecordStatusDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM CoffeeSiteRecordStatus")
    LiveData<List<CoffeeSiteRecordStatus>> getAllCSRecordStatuses();

    @Query("SELECT * FROM CoffeeSiteRecordStatus WHERE status LIKE :stringValue LIMIT 1")
    Flowable<CoffeeSiteRecordStatus> getCoffeeSiteRecordStatus(String stringValue);

    @Insert
    void insertAll(List<CoffeeSiteRecordStatus> csRecordStatuses);

    @Insert
    void insert(CoffeeSiteRecordStatus csRecordStatuse);
}
