package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import io.reactivex.Flowable;

@Dao
public interface NextToMachineTypeDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM next_to_machine_type_table")
    LiveData<List<NextToMachineType>> getAllNextToMachineTypes();

    @Query("SELECT * FROM next_to_machine_type_table WHERE type LIKE :stringValue  LIMIT 1")
    Flowable<NextToMachineType> getNextToMachineType(String stringValue);

    @Query("DELETE FROM next_to_machine_type_table")
    void deleteAll();

    @Insert
    void insertAll(List<NextToMachineType> nextToMachineTypes);

    @Insert
    void insertNextToMachineType(NextToMachineType nextToMachineType);
}
