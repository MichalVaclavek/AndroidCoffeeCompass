package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import io.reactivex.Flowable;

@Dao
public interface StarsQualityDescriptionDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM stars_quality_description_table")
    LiveData<List<StarsQualityDescription>> getAllStarsQualityDescriptions();

    @Query("SELECT * FROM stars_quality_description_table WHERE numOfStars = :number  LIMIT 1")
    Flowable<StarsQualityDescription> getStarsQualityDescriptionByNumber(int number);

    @Query("DELETE FROM stars_quality_description_table")
    void deleteAll();

    @Insert
    void insertAll(List<StarsQualityDescription> starsQualityDescriptions);

    @Insert
    void insertStarsQualityDescription(StarsQualityDescription starsQualityDescription);
}
