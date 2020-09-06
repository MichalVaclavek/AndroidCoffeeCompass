package cz.fungisoft.coffeecompass2.entity.repository.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import io.reactivex.Flowable;

@Dao
public interface OtherOfferDao {

//    @Query("SELECT * FROM CoffeeSite")
//    public List<CoffeeSiteWithCsStatus> loadCoffeeSiteWithCsStatuses();

    @Query("SELECT * FROM OtherOffer")
    LiveData<List<OtherOffer>> getAllOtherOffers();

    @Query("SELECT * FROM OtherOffer WHERE offer LIKE :stringValue  LIMIT 1")
    Flowable<OtherOffer> getOtherOffer(String stringValue);

    @Insert
    void insertAll(List<OtherOffer> otherOffers);

    @Insert
    void insertOtherOffer(OtherOffer otherOffer);
}
