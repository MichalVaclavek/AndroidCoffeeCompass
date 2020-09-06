package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfRatings;

@Dao
public interface AverageStarsWithNumOfRatingsDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM AverageStarsWithNumOfRatings")
    LiveData<List<AverageStarsWithNumOfRatings>> getAllAverageStarsWithNumOfHodnoceni();

//    @Query("SELECT * FROM AverageStarsWithNumOfHodnoceni WHERE coffeeSiteType LIKE :stringValue")
//    Flowable<AverageStarsWithNumOfHodnoceni> getAverageStarsWithNumOfRatings(String stringValue);

    @Insert
    void insertAll(List<AverageStarsWithNumOfRatings> averageStarsWithNumOfHodnoceniList);

    @Insert
    void insert(AverageStarsWithNumOfRatings averageStarsWithNumOfHodnoceni);
}
