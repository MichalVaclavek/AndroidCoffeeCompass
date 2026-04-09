package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.PriceRange;
import io.reactivex.Single;

@Dao
public interface PriceRangeDao {

    @Query("SELECT * FROM price_range_table")
    LiveData<List<PriceRange>> getAllPriceRanges();

    @Query("SELECT * FROM price_range_table")
    Single<List<PriceRange>> getAllPriceRangesSingle();

    @Query("SELECT * FROM price_range_table WHERE priceRange = :stringValue LIMIT 1")
    Single<PriceRange> getPriceRange(String stringValue);

    @Query("DELETE FROM price_range_table")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PriceRange> priceRanges);

    @Insert
    void insertPriceRange(PriceRange priceRange);

}
