package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfRatings;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass2.entity.repository.dao.AverageStarsWithNumOfRatingsDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteRecordStatusDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteStatusDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSiteTypeDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CoffeeSortDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CommentDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CupTypeDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.NextToMachineTypeDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.OtherOfferDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.PriceRangeDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.SiteLocationTypeDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.StarsQualityDescriptionDao;

@Database(entities = {CoffeeSite.class, CoffeeSiteStatus.class, CoffeeSiteRecordStatus.class,
                      CoffeeSiteType.class, CoffeeSort.class, CupType.class,
                      AverageStarsWithNumOfRatings.class, NextToMachineType.class,
                      OtherOffer.class, PriceRange.class, SiteLocationType.class,
                      StarsQualityDescription.class , Comment.class},
          version = 1, exportSchema = false)
public abstract class CoffeeSiteDatabase extends RoomDatabase {

    private static CoffeeSiteDatabase DB_INSTANCE;

    public static CoffeeSiteDatabase getDatabase(final Context context) {
        if (DB_INSTANCE == null) {
            synchronized (CoffeeSiteDatabase.class) {
                if (DB_INSTANCE == null) {
                    // Create database here
                    DB_INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CoffeeSiteDatabase.class, "COFFEE_COMPASS_2.DB")
                            // Wipes and rebuilds instead of migrating
                            // if no Migration object.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return DB_INSTANCE;
    }

    public abstract CoffeeSiteDao coffeeSiteDao();

    public abstract CoffeeSiteStatusDao coffeeSiteStatusDao();

    public abstract AverageStarsWithNumOfRatingsDao averageStarsWithNumOfHodnoceniDao();

    public abstract CoffeeSiteRecordStatusDao coffeeSiteRecordStatusDao();

    public abstract CoffeeSiteTypeDao coffeeSiteTypeDao();

    public abstract CoffeeSortDao coffeeSortDao();

    public abstract CommentDao commentDao();

    public abstract CupTypeDao cupTypeDao();

    public abstract NextToMachineTypeDao nextToMachineTypeDao();

    public abstract OtherOfferDao otherOfferDao();

    public abstract PriceRangeDao priceRangeDao();

    public abstract SiteLocationTypeDao siteLocationTypeDao();

    public abstract StarsQualityDescriptionDao starsQualityDescriptionDao();

}
