package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfRatings;

@Dao
public interface AverageStarsWithNumOfRatingsDao {

    @Query("SELECT * FROM average_stars_with_numOfRatings_table")
    LiveData<List<AverageStarsWithNumOfRatings>> getAllAverageStarsWithNumOfHodnoceni();

    @Query("DELETE FROM average_stars_with_numOfRatings_table")
    void deleteAll();

    @Insert
    void insertAll(List<AverageStarsWithNumOfRatings> averageStarsWithNumOfHodnoceniList);

    @Insert
    void insert(AverageStarsWithNumOfRatings averageStarsWithNumOfHodnoceni);
}
