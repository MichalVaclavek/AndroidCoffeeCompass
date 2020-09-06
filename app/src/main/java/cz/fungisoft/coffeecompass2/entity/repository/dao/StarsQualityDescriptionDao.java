package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass2.entity.repository.relations.CoffeeSiteWithCsStatus;
import io.reactivex.Flowable;

@Dao
public interface StarsQualityDescriptionDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM StarsQualityDescription")
    LiveData<List<StarsQualityDescription>> getAllStarsQualityDescriptions();

    @Query("SELECT * FROM StarsQualityDescription WHERE numOfStars = :number  LIMIT 1")
    Flowable<StarsQualityDescription> getStarsQualityDescriptionByNumber(int number);


    @Insert
    void insertAll(List<StarsQualityDescription> starsQualityDescriptions);

    @Insert
    void insertStarsQualityDescription(StarsQualityDescription starsQualityDescription);
}
