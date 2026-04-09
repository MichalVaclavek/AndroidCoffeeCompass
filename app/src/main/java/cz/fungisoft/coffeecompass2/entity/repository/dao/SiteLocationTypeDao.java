package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import io.reactivex.Single;

@Dao
public interface SiteLocationTypeDao {

    @Query("SELECT * FROM site_location_type_table")
    LiveData<List<SiteLocationType>> getAllSiteLocationTypes();

    @Query("SELECT * FROM site_location_type_table")
    Single<List<SiteLocationType>> getAllSiteLocationTypesSingle();

    @Query("SELECT * FROM site_location_type_table WHERE locationType = :locationType LIMIT 1")
    Single<SiteLocationType> getSiteLocationType(String locationType);

    @Query("DELETE FROM site_location_type_table")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SiteLocationType> siteLocationTypes);

    @Insert
    void insertSiteLocationType(SiteLocationType siteLocationType);
}
