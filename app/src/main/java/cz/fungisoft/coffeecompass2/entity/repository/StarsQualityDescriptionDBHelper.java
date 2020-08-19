package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;

public class StarsQualityDescriptionDBHelper extends SQLiteHelperBase<StarsQualityDescription> {

    private static final String TABLE_NAME = "STARS_QUALITY_DESCRIPTION";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_QUALITY = "quality";

    public static final String COL_NUM_OF_STARS = "num_of_stars";

    // Creating table query
    private static final String CREATE_COFFEE_SITE_STATUS_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_QUALITY + " TEXT NOT NULL, "
            + COL_NUM_OF_STARS + " INTEGER);";

    private static String[] columnNames = new String[] { ID, COL_QUALITY, COL_NUM_OF_STARS };

    public StarsQualityDescriptionDBHelper(Context context) {
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
    public ContentValues getContentValues(StarsQualityDescription entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_QUALITY, entity.getQuality());
        contentValue.put(COL_NUM_OF_STARS, entity.getNumOfStars());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public StarsQualityDescription getValue(Cursor cursor) {

        StarsQualityDescription starsQualityDescription = new StarsQualityDescription();
        starsQualityDescription.setId(getColumnIntValue(cursor, ID));
        starsQualityDescription.setNumOfStars(getColumnIntValue(cursor, COL_NUM_OF_STARS));
        starsQualityDescription.setQuality(getColumnStringValue(cursor, COL_QUALITY));
        return starsQualityDescription;
    }

}
