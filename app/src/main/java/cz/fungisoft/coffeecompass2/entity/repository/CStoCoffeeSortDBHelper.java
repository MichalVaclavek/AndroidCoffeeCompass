package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;

/**
 * SQLite helper popisujici spojovaci ManyToMany relaci/tabulku mezi CoffeeSite a CoffeeSort
 */
public class CStoCoffeeSortDBHelper extends SQLiteHelperBase<CStoCoffeeSortDBHelper.CStoCoffeeSort> {

    private static final String TABLE_NAME = "COFFEE_SITE_TO_COFFEE_SORT";

    // jmena sloupcu tabulky CStoCoffeeSort
    public static final String COL_COFFEE_SITE_ID = "coffee_site_id";
    public static final String COL_COFFEE_SORT_ID = "coffee_sort_id";

    // Creating table query
    private static final String CREATE_CS_TO_COFFEE_SORT_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_COFFEE_SITE_ID + " INTEGER NOT NULL, "
            + COL_COFFEE_SORT_ID + " INTEGER NOT NULL);";

    private static String[] columnNames = new String[] { ID, COL_COFFEE_SITE_ID, COL_COFFEE_SORT_ID};

    public CStoCoffeeSortDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_CS_TO_COFFEE_SORT_TABLE;
    }

    @Override
    public ContentValues getContentValues(CStoCoffeeSort entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_COFFEE_SITE_ID, entity.getCoffeeSiteId());
        contentValue.put(COL_COFFEE_SORT_ID, entity.getCoffeeSortId());

        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CStoCoffeeSort getValue(Cursor cursor) {
        CStoCoffeeSort cStoCoffeeSort = new CStoCoffeeSort();
        cStoCoffeeSort.setId(getColumnIntValue(cursor, ID));
        cStoCoffeeSort.setCoffeeSiteId( getColumnIntValue(cursor, COL_COFFEE_SITE_ID));
        cStoCoffeeSort.setCoffeeSortId(getColumnIntValue(cursor, COL_COFFEE_SORT_ID));

        return cStoCoffeeSort;
    }

    /**
     * Holder class to hold relation between CoffeeSite ID and Coffee sort ID
     */
    class CStoCoffeeSort extends CoffeeSiteEntity {

        public int getCoffeeSiteId() {
            return coffeeSiteId;
        }

        public void setCoffeeSiteId(int coffeeSiteId) {
            this.coffeeSiteId = coffeeSiteId;
        }

        public int getCoffeeSortId() {
            return coffeeSortId;
        }

        public void setCoffeeSortId(int coffeeSortId) {
            this.coffeeSortId = coffeeSortId;
        }

        private int coffeeSiteId;

        private int coffeeSortId;
    }

}
