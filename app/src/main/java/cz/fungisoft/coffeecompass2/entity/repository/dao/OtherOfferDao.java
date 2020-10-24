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
import io.reactivex.Single;

@Dao
public interface OtherOfferDao {

    @Query("SELECT * FROM other_offer_table")
    LiveData<List<OtherOffer>> getAllOtherOffers();

    @Query("SELECT * FROM other_offer_table WHERE offer = :stringValue LIMIT 1")
    Single<OtherOffer> getOtherOffer(String stringValue);

    @Query("DELETE FROM other_offer_table")
    void deleteAll();

    @Insert
    void insertAll(List<OtherOffer> otherOffers);

    @Insert
    void insertOtherOffer(OtherOffer otherOffer);
}
