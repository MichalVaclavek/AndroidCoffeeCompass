package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

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
                      version = 6, exportSchema = false)
@TypeConverters(DbDataConverters.class)
public abstract class CoffeeSiteDatabase extends RoomDatabase {

    private static CoffeeSiteDatabase DB_INSTANCE;

    public interface DbDeleteEndListener {
        default void onCSEntitiesDeletedEnd() {}
        default void onCoffeeSitesDeletedEnd() {}
        default void onCommentsDeletedEnd() {}
    }

    private List<DbDeleteEndListener> dbDeleteEndListeners = new ArrayList<>();

    public synchronized void addDbDeleteEndListener(DbDeleteEndListener dbDeleteEndListener) {
        dbDeleteEndListeners.add(dbDeleteEndListener);
    }

    public synchronized void removeDbDeleteEndListener(DbDeleteEndListener dbDeleteEndListener) {
        dbDeleteEndListeners.remove(dbDeleteEndListener);
    }


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
                            .addCallback(sCoffeeSiteDatabaseCallback)
                            .build();
                }
            }
        }
        return DB_INSTANCE;
    }

    private static CoffeeSiteDatabase.Callback sCoffeeSiteDatabaseCallback =
            new CoffeeSiteDatabase.Callback() {

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    //new DeleteCSEntitiesAsync(DB_INSTANCE).execute();
                }
            };


    private void fireCSEntitiesContentDeleted() {
        for (DbDeleteEndListener listener : dbDeleteEndListeners) {
            listener.onCSEntitiesDeletedEnd();
        }
    }
    private void fireCoffeeSitesContentDeleted() {
        for (DbDeleteEndListener listener : dbDeleteEndListeners) {
            listener.onCoffeeSitesDeletedEnd();
        }
    }
    private void fireCommentsContentDeleted() {
        for (DbDeleteEndListener listener : dbDeleteEndListeners) {
            listener.onCommentsDeletedEnd();
        }
    }

    public void deleteCSEntitiesAsync() {
        new DeleteCSEntitiesAsync(DB_INSTANCE).execute();
    }
    public void deleteCoffeeSitesAsync() {
        new DeleteCoffeeSitesAsync(DB_INSTANCE).execute();
    }
    public void deleteCommentsAsync() {
        new DeleteCommentsAsync(DB_INSTANCE).execute();
    }


    /**
     * Deletes the database in the background.
     */
    private static class DeleteCSEntitiesAsync extends AsyncTask<Void, Void, Void> {

        private CoffeeSiteDatabase mDB;

        private final AverageStarsWithNumOfRatingsDao averageStarsWithNumOfRatingsDao;
//        private final CoffeeSiteDao coffeeSiteDao;
        private final CoffeeSiteRecordStatusDao coffeeSiteRecordStatusDao;
        private final CoffeeSiteStatusDao coffeeSiteStatusDao;
        private final CoffeeSiteTypeDao coffeeSiteTypeDao;
        private final CoffeeSortDao coffeeSortDao;
//        private final CommentDao commentDao;
        private final CupTypeDao cupTypeDao;
        private final NextToMachineTypeDao nextToMachineTypeDao;
        private final OtherOfferDao otherOfferDao;
        private final PriceRangeDao priceRangeDao;
        private final SiteLocationTypeDao siteLocationTypeDao;
        private final StarsQualityDescriptionDao starsQualityDescriptionDao;


        DeleteCSEntitiesAsync(CoffeeSiteDatabase db) {
            this.mDB = db;

            averageStarsWithNumOfRatingsDao = db.averageStarsWithNumOfHodnoceniDao();
//            coffeeSiteDao = db.coffeeSiteDao();
            coffeeSiteRecordStatusDao = db.coffeeSiteRecordStatusDao();
            coffeeSiteStatusDao = db.coffeeSiteStatusDao();
            coffeeSiteTypeDao = db.coffeeSiteTypeDao();
            coffeeSortDao = db.coffeeSortDao();
//            commentDao = db.commentDao();
            cupTypeDao = db.cupTypeDao();
            nextToMachineTypeDao = db.nextToMachineTypeDao();
            otherOfferDao = db.otherOfferDao();
            priceRangeDao = db.priceRangeDao();
            siteLocationTypeDao = db.siteLocationTypeDao();
            starsQualityDescriptionDao = db.starsQualityDescriptionDao();
        }


        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate the database
            // when it is first created
            averageStarsWithNumOfRatingsDao.deleteAll();
//            coffeeSiteDao.deleteAll();
            coffeeSiteRecordStatusDao.deleteAll();
            coffeeSiteStatusDao.deleteAll();
            coffeeSiteTypeDao.deleteAll();
            coffeeSortDao.deleteAll();
//            commentDao.deleteAll();
            cupTypeDao.deleteAll();
            nextToMachineTypeDao.deleteAll();
            otherOfferDao.deleteAll();
            priceRangeDao.deleteAll();
            siteLocationTypeDao.deleteAll();
            starsQualityDescriptionDao.deleteAll();

            mDB.fireCSEntitiesContentDeleted();

            return null;
        }
    }

    /**
     * Deletes the CoffeeSites database in the background.
     */
    private static class DeleteCoffeeSitesAsync extends AsyncTask<Void, Void, Void> {

        private CoffeeSiteDatabase mDB;

        private final CoffeeSiteDao coffeeSiteDao;

        DeleteCoffeeSitesAsync(CoffeeSiteDatabase db) {
            this.mDB = db;
            coffeeSiteDao = db.coffeeSiteDao();
        }


        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate the database
            // when it is first created
            coffeeSiteDao.deleteAll();
            mDB.fireCoffeeSitesContentDeleted();

            return null;
        }
    }

    /**
     * Deletes the Comments database in the background.
     */
    private static class DeleteCommentsAsync extends AsyncTask<Void, Void, Void> {

        private CoffeeSiteDatabase mDB;

        private final CommentDao commentDao;

        DeleteCommentsAsync(CoffeeSiteDatabase db) {
            this.mDB = db;
            commentDao = db.commentDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate the database
            // when it is first created
            commentDao.deleteAll();
            mDB.fireCommentsContentDeleted();

            return null;
        }
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
