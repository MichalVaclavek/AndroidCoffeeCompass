package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.repository.relations.CoffeeSiteWithCsStatus;
import io.reactivex.Flowable;

@Dao
public interface PriceRangeDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM PriceRange")
    LiveData<List<PriceRange>> getAllPriceRanges();

    @Query("SELECT * FROM PriceRange WHERE priceRange LIKE :stringValue  LIMIT 1")
    Flowable<PriceRange> getPriceRange(String stringValue);

    @Insert
    void insertAll(List<PriceRange> priceRanges);

    @Insert
    void insertPriceRange(PriceRange priceRange);
}
