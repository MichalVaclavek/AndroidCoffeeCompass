package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import io.reactivex.Flowable;

@Dao
public interface NextToMachineTypeDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM NextToMachineType")
    LiveData<List<NextToMachineType>> getAllNextToMachineTypes();

    @Query("SELECT * FROM NextToMachineType WHERE type LIKE :stringValue  LIMIT 1")
    Flowable<NextToMachineType> getNextToMachineType(String stringValue);

    @Insert
    void insertAll(List<NextToMachineType> nextToMachineTypes);

    @Insert
    void insertNextToMachineType(NextToMachineType nextToMachineType);
}
