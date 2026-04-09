package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface StarsQualityDescriptionDao {

    @Query("SELECT * FROM stars_quality_description_table")
    LiveData<List<StarsQualityDescription>> getAllStarsQualityDescriptions();

    @Query("SELECT * FROM stars_quality_description_table")
    Single<List<StarsQualityDescription>> getAllStarsQualityDescriptionsSingle();

    @Query("SELECT * FROM stars_quality_description_table WHERE numOfStars = :number  LIMIT 1")
    Flowable<StarsQualityDescription> getStarsQualityDescriptionByNumber(int number);

    @Query("DELETE FROM stars_quality_description_table")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StarsQualityDescription> starsQualityDescriptions);

    @Insert
    void insertStarsQualityDescription(StarsQualityDescription starsQualityDescription);
}
