package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.repository.relations.CoffeeSiteWithCsStatus;
import io.reactivex.Flowable;

@Dao
public interface CoffeeSiteDao {

    @Transaction
    @Query("SELECT * FROM CoffeeSite")
    public LiveData<List<CoffeeSiteWithCsStatus>> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM CoffeeSite")
    LiveData<List<CoffeeSite>> getAllCoffeeSites();

    @Query("SELECT * FROM CoffeeSite WHERE id = :id LIMIT 1")
    Flowable<CoffeeSite> getCoffeeSiteById(int id);

    @Insert
    void insertAll(List<CoffeeSite> coffeeSites);

    @Insert
    void insertCoffeeSite(CoffeeSite coffeeSite);
}
