package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
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

public class DBManager {

    private SQLiteHelperBase dbHelper;

    private Context context;

    public Context getContext() {
        return context;
    }

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    private Cursor cursor;


    public DBManager open(SQLiteHelperBase dbHelper) throws SQLException {
        this.dbHelper = dbHelper;
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(CoffeeSiteEntity entity) {
        database.insert(dbHelper.getDbTableName(), null, dbHelper.getContentValues(entity));
    }

    public Cursor fetch() {
        cursor = database.query(dbHelper.getDbTableName(), dbHelper.getColumnNames(), null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public <T extends CoffeeSiteEntity> T getValue(int id) {
        fetch();

        CoffeeSiteEntity retVal = null;
        if (cursor.moveToFirst()) {
            do {
                if (dbHelper.getClass() == CoffeeSiteDBHelper.class) {
                    retVal = ((CoffeeSiteDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == CommentDBHelper.class) {
                    retVal = ((CommentDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == CoffeeSortDBHelper.class) {
                    retVal = ((CoffeeSortDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == CoffeeSiteStatusDBHelper.class) {
                    retVal = ((CoffeeSiteStatusDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == NextToMachineTypeDBHelper.class) {
                    retVal = ((NextToMachineTypeDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == CupTypeDBHelper.class) {
                    retVal = ((CupTypeDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == SiteLocationTypeDBHelper.class) {
                    retVal = ((SiteLocationTypeDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == PriceRangeDBHelper.class) {
                    retVal = ((PriceRangeDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == CoffeeSiteRecordStatusDBHelper.class) {
                    retVal = ((CoffeeSiteRecordStatusDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == CoffeeSiteTypeDBHelper.class) {
                    retVal = ((CoffeeSiteTypeDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == OtherOfferDBHelper.class) {
                    retVal = ((OtherOfferDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == SiteLocationTypeDBHelper.class) {
                    retVal = ((SiteLocationTypeDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == StarsQualityDescriptionDBHelper.class) {
                    retVal = ((StarsQualityDescriptionDBHelper) dbHelper).getValue(cursor);
                }

                if (retVal.getId() == id) break;
            } while (cursor.moveToNext());
        }
        return (T) retVal;
    }

    /**
     * If returned 0, then entity is not in DB
     *
     * @param entity
     * @return
     */
    public long getEntityId(CoffeeSiteEntity entity) {
        fetch();

        CoffeeSiteEntity dbEntity = null;
        if (cursor.moveToFirst()) {
            do {
                if (dbHelper.getClass() == CoffeeSiteDBHelper.class) {
                    dbEntity = ((CoffeeSiteDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == CommentDBHelper.class) {
                    dbEntity = ((CommentDBHelper) dbHelper).getValue(cursor);
                }

                if (dbHelper.getClass() == CoffeeSortDBHelper.class) {
                    dbEntity = ((CoffeeSortDBHelper) dbHelper).getValue(cursor);

                }
                if (dbHelper.getClass() == CoffeeSiteStatusDBHelper.class) {
                    dbEntity = ((CoffeeSiteStatusDBHelper) dbHelper).getValue(cursor);
                }

                if (dbHelper.getClass() == NextToMachineTypeDBHelper.class) {
                    dbEntity = ((NextToMachineTypeDBHelper) dbHelper).getValue(cursor);

                }
                if (dbHelper.getClass() == CupTypeDBHelper.class) {
                    dbEntity = ((CupTypeDBHelper) dbHelper).getValue(cursor);
                }

                if (dbHelper.getClass() == SiteLocationTypeDBHelper.class) {
                    dbEntity = ((SiteLocationTypeDBHelper) dbHelper).getValue(cursor);

                }
                if (dbHelper.getClass() == PriceRangeDBHelper.class) {
                    dbEntity = ((PriceRangeDBHelper) dbHelper).getValue(cursor);
                }

                if (dbHelper.getClass() == CoffeeSiteRecordStatusDBHelper.class) {
                    dbEntity = ((CoffeeSiteRecordStatusDBHelper) dbHelper).getValue(cursor);

                }
                if (dbHelper.getClass() == CoffeeSiteTypeDBHelper.class) {
                    dbEntity = ((CoffeeSiteTypeDBHelper) dbHelper).getValue(cursor);
                }

                if (dbHelper.getClass() == OtherOfferDBHelper.class) {
                    dbEntity = ((OtherOfferDBHelper) dbHelper).getValue(cursor);

                }
                if (dbHelper.getClass() == SiteLocationTypeDBHelper.class) {
                    dbEntity = ((SiteLocationTypeDBHelper) dbHelper).getValue(cursor);
                }
                if (dbHelper.getClass() == StarsQualityDescriptionDBHelper.class) {
                    dbEntity = ((StarsQualityDescriptionDBHelper) dbHelper).getValue(cursor);
                }

                if (dbEntity.equals(entity)) {
                    return dbEntity.getId();
                }

            } while (cursor.moveToNext());
        }

        return 0;
    }

    public <T extends CoffeeSiteEntity> List<T> getAllValues() {
        fetch();

        List<T> retVal = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                if (dbHelper.getClass() == CoffeeSiteDBHelper.class) {
                    CoffeeSite coffeeSite = ((CoffeeSiteDBHelper) dbHelper).getValue(cursor);

                    retVal.add((T) coffeeSite);
                }
                if (dbHelper.getClass() == CommentDBHelper.class) {
                    Comment comment = ((CommentDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }

                if (dbHelper.getClass() == CoffeeSortDBHelper.class) {
                    CoffeeSort comment = ((CoffeeSortDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == CoffeeSiteStatusDBHelper.class) {
                    CoffeeSiteStatus comment = ((CoffeeSiteStatusDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == NextToMachineTypeDBHelper.class) {
                    NextToMachineType comment = ((NextToMachineTypeDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == CupTypeDBHelper.class) {
                    CupType comment = ((CupTypeDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == SiteLocationTypeDBHelper.class) {
                    SiteLocationType comment = ((SiteLocationTypeDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == PriceRangeDBHelper.class) {
                    PriceRange comment = ((PriceRangeDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == CoffeeSiteRecordStatusDBHelper.class) {
                    CoffeeSiteRecordStatus comment = ((CoffeeSiteRecordStatusDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == CoffeeSiteTypeDBHelper.class) {
                    CoffeeSiteType comment = ((CoffeeSiteTypeDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == OtherOfferDBHelper.class) {
                    OtherOffer comment = ((OtherOfferDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == SiteLocationTypeDBHelper.class) {
                    SiteLocationType comment = ((SiteLocationTypeDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }
                if (dbHelper.getClass() == StarsQualityDescriptionDBHelper.class) {
                    StarsQualityDescription comment = ((StarsQualityDescriptionDBHelper) dbHelper).getValue(cursor);
                    retVal.add((T) comment);
                }

            } while (cursor.moveToNext());
        }
        return retVal;
    }


    public int update(CoffeeSiteEntity entity) {
        return database.update(dbHelper.getDbTableName(), dbHelper.getContentValues(entity), SQLiteHelperBase.ID + " = " + entity.getId(), null);
    }

    public void delete(long id) {
        database.delete(dbHelper.getDbTableName(), SQLiteHelperBase.ID + "=" + id, null);
    }

}
