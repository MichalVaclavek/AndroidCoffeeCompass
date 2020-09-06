package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.repository.relations.CoffeeSiteWithCsStatus;
import io.reactivex.Flowable;

@Dao
public interface SiteLocationTypeDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM SiteLocationType")
    LiveData<List<SiteLocationType>> getAllSiteLocationTypes();

    @Query("SELECT * FROM SiteLocationType WHERE locationType LIKE :stringValue  LIMIT 1")
    Flowable<SiteLocationType> getSiteLocationType(String stringValue);

    @Insert
    void insertAll(List<SiteLocationType> siteLocationTypes);

    @Insert
    void insertSiteLocationType(SiteLocationType siteLocationType);
}
