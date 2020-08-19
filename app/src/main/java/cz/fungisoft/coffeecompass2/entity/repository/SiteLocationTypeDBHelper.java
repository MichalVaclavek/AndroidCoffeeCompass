package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;

public class SiteLocationTypeDBHelper extends SQLiteHelperBase<SiteLocationType> {

    private static final String TABLE_NAME = "SITE_LOCATION_TYPE";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_LOCATION_TYPE = "location_type";

    // Creating table query
    private static final String CREATE_SITE_LOCATION_TYPE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_LOCATION_TYPE + " TEXT);";

    private static String[] columnNames = new String[] { ID, COL_LOCATION_TYPE };

    public SiteLocationTypeDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_SITE_LOCATION_TYPE_TABLE;
    }

    @Override
    public ContentValues getContentValues(SiteLocationType entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_LOCATION_TYPE, entity.getLocationType());
        return contentValue;
    }


    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public SiteLocationType getValue(Cursor cursor) {

        SiteLocationType siteLocationType = new SiteLocationType();
        siteLocationType.setId(getColumnIntValue(cursor, ID));
        siteLocationType.setLocationType(getColumnStringValue(cursor, COL_LOCATION_TYPE));
        return siteLocationType;
    }

}
