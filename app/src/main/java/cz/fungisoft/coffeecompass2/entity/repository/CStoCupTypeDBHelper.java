package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;

/**
 * SQLite helper popisujici spojovaci ManyToMany relaci/tabulku mezi CoffeeSite a CupType
 */
public class CStoCupTypeDBHelper extends SQLiteHelperBase<CStoCupTypeDBHelper.CStoCupType> {

    private static final String TABLE_NAME = "COFFEE_SITE_TO_CUP_TYPE";

    // jmena sloupcu tabulky CStoCupType
    public static final String COL_COFFEE_SITE_ID = "coffee_site_id";
    public static final String COL_CUP_TYPE_ID = "cup_type_id";

    // Creating table query
    private static final String CREATE_CS_TO_CUP_TYPE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_COFFEE_SITE_ID + " INTEGER NOT NULL, "
            + COL_CUP_TYPE_ID + " INTEGER NOT NULL);";

    private static String[] columnNames = new String[] { ID, COL_COFFEE_SITE_ID, COL_CUP_TYPE_ID};

    public CStoCupTypeDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_CS_TO_CUP_TYPE_TABLE;
    }

    @Override
    public ContentValues getContentValues(CStoCupType entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_COFFEE_SITE_ID, entity.getCoffeeSiteId());
        contentValue.put(COL_CUP_TYPE_ID, entity.getCupTypeId());

        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CStoCupType getValue(Cursor cursor) {
        CStoCupType cStoCupType = new CStoCupType();
        cStoCupType.setId(getColumnIntValue(cursor, ID));
        cStoCupType.setCoffeeSiteId( getColumnIntValue(cursor, COL_COFFEE_SITE_ID));
        cStoCupType.setCupTypeId(getColumnIntValue(cursor, COL_CUP_TYPE_ID));

        return cStoCupType;
    }

    /**
     * Holder class to hold relation between CoffeeSite ID and Coffee sort ID
     */
    class CStoCupType extends CoffeeSiteEntity {

        public int getCoffeeSiteId() {
            return coffeeSiteId;
        }

        public void setCoffeeSiteId(int coffeeSiteId) {
            this.coffeeSiteId = coffeeSiteId;
        }

        public int getCupTypeId() {
            return cupTypeId;
        }

        public void setCupTypeId(int cupTypeId) {
            this.cupTypeId = cupTypeId;
        }

        private int coffeeSiteId;

        private int cupTypeId;
    }

}
