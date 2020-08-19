package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.PriceRange;

public class PriceRangeDBHelper extends SQLiteHelperBase<PriceRange> {

    private static final String TABLE_NAME = "PRICE_RANGE";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_PRICE_RANGE = "price_range";

    // Creating table query
    private static final String CREATE_PRICE_RANGE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_PRICE_RANGE + " TEXT);";

    private static String[] columnNames = new String[] { ID, COL_PRICE_RANGE };

    public PriceRangeDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_PRICE_RANGE_TABLE;
    }

    @Override
    public ContentValues getContentValues(PriceRange entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_PRICE_RANGE, entity.getPriceRange());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public PriceRange getValue(Cursor cursor) {

        PriceRange priceRange = new PriceRange();
        priceRange.setId(getColumnIntValue(cursor, ID));
        priceRange.setPriceRange(getColumnStringValue(cursor, COL_PRICE_RANGE));
        return priceRange;
    }
}
