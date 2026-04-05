package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CupType;
import io.reactivex.Single;

@Dao
public interface CupTypeDao {

    @Query("SELECT * FROM cup_type_table")
    LiveData<List<CupType>> getAllCupTypes();

    @Query("SELECT * FROM cup_type_table WHERE cupType = :stringValue LIMIT 1")
    Single<CupType> getCupType(String stringValue);

    @Query("DELETE FROM cup_type_table")
    void deleteAll();

    @Insert
    void insertAll(List<CupType> cupTypes);

    @Insert
    void insertCupType(CupType cupType);
}
