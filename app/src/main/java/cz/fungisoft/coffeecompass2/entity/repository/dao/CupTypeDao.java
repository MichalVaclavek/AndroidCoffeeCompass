package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.entity.CupType;
import io.reactivex.Flowable;

@Dao
public interface CupTypeDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM CupType")
    LiveData<List<CupType>> getAllCupTypes();

    @Query("SELECT * FROM CupType WHERE cupType LIKE :stringValue  LIMIT 1")
    Flowable<CupType> getCupType(String stringValue);

    @Insert
    void insertAll(List<CupType> cupTypes);

    @Insert
    void insertCupType(CupType cupType);
}
