package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;

public class CoffeeSiteStatusDBHelper extends SQLiteHelperBase<CoffeeSiteStatus> {

    private static final String TABLE_NAME = "COFFEE_SITE_STATUS";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_STATUS = "site_status";

    // Creating table query
    private static final String CREATE_COFFEE_SITE_STATUS_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_STATUS + " TEXT);";

    private static String[] columnNames = new String[] {ID, COL_STATUS};

    public CoffeeSiteStatusDBHelper(Context context) {
        super(context);
    }


    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_COFFEE_SITE_STATUS_TABLE;
    }

    @Override
    public ContentValues getContentValues(CoffeeSiteStatus entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_STATUS, entity.getStatus());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CoffeeSiteStatus getValue(Cursor cursor) {
        CoffeeSiteStatus csStatus = new CoffeeSiteStatus();
        csStatus.setId(getColumnIntValue(cursor, ID));
        csStatus.setStatus(getColumnStringValue(cursor, COL_STATUS));
        return csStatus;
    }
}
