package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfHodnoceni;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;

/**
 * Helper class to save {@link AverageStarsWithNumOfHodnoceni} into DB.
 */
public class AverageStarsWithNumOfHodnoceniDBHelper extends SQLiteHelperBase<AverageStarsWithNumOfHodnoceni> {

    private static final String TABLE_NAME = "AVERAGE_STARS_WITH_NUM_OF_STARS";

    // jmena sloupcu tabulky AverageStarsWithNumOfHodnoceni
    public static final String COL_AVG_STARS = "avg_stars";
    public static final String COL_NUM_OF_HODNOCENI = "num_of_hodnoceni";
    public static final String COL_COMMON = "common";

    // Creating table query
    private static final String CREATE_AVERAGE_STARS_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_AVG_STARS + " REAL NOT NULL, "
            + COL_NUM_OF_HODNOCENI + " INTEGER, "
            + COL_COMMON + " TEXT);";

    private static String[] columnNames = new String[] { ID, COL_AVG_STARS, COL_NUM_OF_HODNOCENI,
            COL_COMMON};

    public AverageStarsWithNumOfHodnoceniDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_AVERAGE_STARS_TABLE;
    }

    @Override
    public ContentValues getContentValues(AverageStarsWithNumOfHodnoceni entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_AVG_STARS, entity.getAvgStars());
        contentValue.put(COL_NUM_OF_HODNOCENI, entity.getNumOfHodnoceni());
        contentValue.put(COL_COMMON, entity.getCommon());

        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public AverageStarsWithNumOfHodnoceni getValue(Cursor cursor) {
        AverageStarsWithNumOfHodnoceni avgStars = new AverageStarsWithNumOfHodnoceni();
        avgStars.setId(getColumnIntValue(cursor, ID));
        avgStars.setAvgStars((float) getColumnRealValue(cursor, COL_AVG_STARS));
        avgStars.setNumOfHodnoceni(getColumnIntValue(cursor, COL_NUM_OF_HODNOCENI));
        avgStars.setCommon(getColumnStringValue(cursor, COL_COMMON));

        return avgStars;
    }

}
