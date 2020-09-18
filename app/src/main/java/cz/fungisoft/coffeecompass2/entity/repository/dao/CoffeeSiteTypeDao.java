package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import io.reactivex.Flowable;

@Dao
public interface CoffeeSiteTypeDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM coffee_site_type_table")
    LiveData<List<CoffeeSiteType>> getAllCoffeeSiteTypes();

    @Query("SELECT * FROM coffee_site_type_table WHERE coffeeSiteType LIKE :stringValue LIMIT 1")
    Flowable<CoffeeSiteType> getCoffeeSiteType(String stringValue);

    @Query("DELETE FROM coffee_site_type_table")
    void deleteAll();

    @Insert
    void insertAll(List<CoffeeSiteType> coffeeSiteTypes);

    @Insert
    void insertCoffeeSiteType(CoffeeSiteType coffeeSiteType);
}
