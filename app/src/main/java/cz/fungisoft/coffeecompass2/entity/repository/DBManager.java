package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
import cz.fungisoft.coffeecompass2.entity.Comment;

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
            } while (cursor.moveToNext());
        }
        return retVal;
    }


    public int update(CoffeeSiteEntity entity) {
        int i = database.update(dbHelper.getDbTableName(), dbHelper.getContentValues(entity), SQLiteHelperBase.ID + " = " + entity.getId(), null);
        return i;
    }

    public void delete(long id) {
        database.delete(dbHelper.getDbTableName(), SQLiteHelperBase.ID + "=" + id, null);
    }

}
