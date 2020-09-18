package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import io.reactivex.Flowable;

@Dao
public interface CoffeeSortDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM coffee_sort_table")
    LiveData<List<CoffeeSort>> getAllCoffeeSorts();

    @Query("SELECT * FROM coffee_sort_table WHERE coffeeSort LIKE :stringValue  LIMIT 1")
    Flowable<CoffeeSort> getCoffeeSort(String stringValue);

    @Query("DELETE FROM coffee_sort_table")
    void deleteAll();

    @Insert
    void insertAll(List<CoffeeSort> coffeeSorts);

    @Insert
    void insertCoffeeSort(CoffeeSort coffeeSort);
}
