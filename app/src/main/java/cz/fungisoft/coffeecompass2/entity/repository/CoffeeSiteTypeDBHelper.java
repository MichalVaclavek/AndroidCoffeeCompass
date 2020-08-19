package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;

public class CoffeeSiteTypeDBHelper extends SQLiteHelperBase<CoffeeSiteType> {

    private static final String TABLE_NAME = "COFFEE_SITE_TYPE";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_SITE_TYPE = "site_type";

    // Creating table query
    private static final String CREATE_COFFEE_SITE_TYPE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_SITE_TYPE + " TEXT);";

    private static String[] columnNames = new String[] {ID, COL_SITE_TYPE};

    public CoffeeSiteTypeDBHelper(Context context) {
        super(context);
    }


    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_COFFEE_SITE_TYPE_TABLE;
    }

    @Override
    public ContentValues getContentValues(CoffeeSiteType entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_SITE_TYPE, entity.getCoffeeSiteType());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CoffeeSiteType getValue(Cursor cursor) {

        CoffeeSiteType coffeeSiteType = new CoffeeSiteType();
        coffeeSiteType.setId(getColumnIntValue(cursor, ID));
        coffeeSiteType.setCoffeeSiteType(getColumnStringValue(cursor, COL_SITE_TYPE));
        return coffeeSiteType;
    }
}
