package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;

public class CoffeeSortDBHelper extends SQLiteHelperBase<CoffeeSort> {

    private static final String TABLE_NAME = "COFFEE_SORT";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_COFFEE_SORT = "coffee_sort";

    // Creating table query
    private static final String CREATE_COFFEE_SORT_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_COFFEE_SORT + " TEXT);";

    private static String[] columnNames = new String[] {ID, COL_COFFEE_SORT};

    public CoffeeSortDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_COFFEE_SORT_TABLE;
    }

    @Override
    public ContentValues getContentValues(CoffeeSort entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_COFFEE_SORT, entity.getCoffeeSort());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CoffeeSort getValue(Cursor cursor) {

        CoffeeSort coffeeSort = new CoffeeSort();
        coffeeSort.setId(getColumnIntValue(cursor, ID));
        coffeeSort.setCoffeeSort(getColumnStringValue(cursor, COL_COFFEE_SORT));
        return coffeeSort;
    }
}
