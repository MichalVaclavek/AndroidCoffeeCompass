package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import io.reactivex.Single;

@Dao
public interface CoffeeSortDao {

    @Query("SELECT * FROM coffee_sort_table")
    LiveData<List<CoffeeSort>> getAllCoffeeSorts();

    @Query("SELECT * FROM coffee_sort_table")
    Single<List<CoffeeSort>> getAllCoffeeSortsSingle();

    @Query("SELECT * FROM coffee_sort_table WHERE coffeeSort = :stringValue LIMIT 1")
    Single<CoffeeSort> getCoffeeSort(String stringValue);

    @Query("DELETE FROM coffee_sort_table")
    void deleteAll();

    @Insert
    void insertAll(List<CoffeeSort> coffeeSorts);

    @Insert
    void insertCoffeeSort(CoffeeSort coffeeSort);
}
