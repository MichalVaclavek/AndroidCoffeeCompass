package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;

/**
 * SQLite helper popisujici spojovaci ManyToMany relaci/tabulku mezi CoffeeSite a CoffeeSort
 */
public class CStoNextToMachineTypeDBHelper extends SQLiteHelperBase<CStoNextToMachineTypeDBHelper.CStoNextToMachineType> {

    private static final String TABLE_NAME = "COFFEE_SITE_TO_NEXT_TO_MACHINE_TYPE";

    // jmena sloupcu tabulky CStoCoffeeSort
    public static final String COL_COFFEE_SITE_ID = "coffee_site_id";
    public static final String COL_NEXT_TO_MACHINE_TYPE_ID = "next_to_machine_type_id";

    // Creating table query
    private static final String CREATE_CS_TO_NEXT_TO_MACHINE_TYPE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_COFFEE_SITE_ID + " INTEGER NOT NULL, "
            + COL_NEXT_TO_MACHINE_TYPE_ID + " INTEGER NOT NULL);";

    private static String[] columnNames = new String[] { ID, COL_COFFEE_SITE_ID, COL_NEXT_TO_MACHINE_TYPE_ID};

    public CStoNextToMachineTypeDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_CS_TO_NEXT_TO_MACHINE_TYPE_TABLE;
    }

    @Override
    public ContentValues getContentValues(CStoNextToMachineType entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_COFFEE_SITE_ID, entity.getCoffeeSiteId());
        contentValue.put(COL_NEXT_TO_MACHINE_TYPE_ID, entity.getNextToMachineTypeId());

        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CStoNextToMachineType getValue(Cursor cursor) {
        CStoNextToMachineType cStoNextToMachineType = new CStoNextToMachineType();
        cStoNextToMachineType.setId(getColumnIntValue(cursor, ID));
        cStoNextToMachineType.setCoffeeSiteId( getColumnIntValue(cursor, COL_COFFEE_SITE_ID));
        cStoNextToMachineType.setNextToMachineTypeId(getColumnIntValue(cursor, COL_NEXT_TO_MACHINE_TYPE_ID));

        return cStoNextToMachineType;
    }

    /**
     * Holder class to hold relation between CoffeeSite ID and Coffee sort ID
     */
    class CStoNextToMachineType extends CoffeeSiteEntity {

        public int getCoffeeSiteId() {
            return coffeeSiteId;
        }

        public void setCoffeeSiteId(int coffeeSiteId) {
            this.coffeeSiteId = coffeeSiteId;
        }

        public int getNextToMachineTypeId() {
            return nextToMachineTypeId;
        }

        public void setNextToMachineTypeId(int nextToMachineTypeId) {
            this.nextToMachineTypeId = nextToMachineTypeId;
        }

        private int coffeeSiteId;

        private int nextToMachineTypeId;
    }

}
