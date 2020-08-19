package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;

public abstract class SQLiteHelperBase<T extends CoffeeSiteEntity> extends SQLiteOpenHelper {

    protected static final String DB_NAME = "COFFEE_COMPASS.DB";

    protected static final int DB_VERSION = 1;

    public static final String ID = "_id";

    /**
     * DBManager to allow access to other DBHelper classes to obtain all other CoffeeSiteEntity objects
     */
    protected static DBManager dbManager;

    public SQLiteHelperBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateTableScript());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + getDbTableName());
        onCreate(db);
    }

    public abstract String getDbTableName();

    public abstract String getCreateTableScript();

    public abstract ContentValues getContentValues(T entity);

    public abstract String[] getColumnNames();

    public abstract T getValue(Cursor cursor);


    protected String getColumnStringValue(Cursor cursor, String columnName) {
        return  cursor.getString(cursor.getColumnIndex(columnName));
    }

    protected int getColumnIntValue(Cursor cursor, String columnName) {
        return  cursor.getInt(cursor.getColumnIndex(columnName));
    }

    protected double getColumnRealValue(Cursor cursor, String columnName) {
        return  cursor.getDouble(cursor.getColumnIndex(columnName));
    }

}
