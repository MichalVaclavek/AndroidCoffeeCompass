package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.Comment;

public class CoffeeSiteRecordStatusDBHelper extends SQLiteHelperBase<CoffeeSiteRecordStatus> {

    private static final String TABLE_NAME = "COFFEE_SITE_RECORD_STATUS";

    // jmena sloupcu tabulky
    public static final String COL_SITE_RECORD_STATUS = "site_record_status";

    // Creating table query
    private static final String CREATE_COFFEE_SITE_TYPE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_SITE_RECORD_STATUS + " TEXT);";

    private static String[] columnNames = new String[] {ID, COL_SITE_RECORD_STATUS};

    public CoffeeSiteRecordStatusDBHelper(Context context) {
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
    public ContentValues getContentValues(CoffeeSiteRecordStatus entity) {

        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_SITE_RECORD_STATUS, entity.getStatus());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CoffeeSiteRecordStatus getValue(Cursor cursor) {

        CoffeeSiteRecordStatus csRecordStatus = new CoffeeSiteRecordStatus();
        csRecordStatus.setId(getColumnIntValue(cursor, ID));
        csRecordStatus.setStatus(getColumnStringValue(cursor, COL_SITE_RECORD_STATUS));
        return csRecordStatus;
    }
}
