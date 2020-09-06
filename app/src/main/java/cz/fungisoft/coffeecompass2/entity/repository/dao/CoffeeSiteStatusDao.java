package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.repository.relations.CoffeeSiteWithCsStatus;
import io.reactivex.Flowable;

@Dao
public interface CoffeeSiteStatusDao {

    @Transaction
    @Query("SELECT * FROM CoffeeSite WHERE id LIKE :coffeeSiteId")
    public List<CoffeeSiteWithCsStatus> getCoffeeSiteWithStatus(String coffeeSiteId);

    @Query("SELECT * FROM CoffeeSiteStatus")
    LiveData<List<CoffeeSiteStatus>> getAllCoffeeSiteStatuses();

    @Query("SELECT * FROM CoffeeSiteStatus WHERE status LIKE :stringValue LIMIT 1")
    Flowable<CoffeeSiteStatus> getCoffeeSiteStatus(String stringValue);

    @Insert
    void insertAll(List<CoffeeSiteStatus> coffeeSiteStatuses);

    @Insert
    void insertCoffeeSiteStatus(CoffeeSiteStatus coffeeSiteStatuse);
}
