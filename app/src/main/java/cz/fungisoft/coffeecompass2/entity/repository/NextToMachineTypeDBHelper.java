package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;

public class NextToMachineTypeDBHelper extends SQLiteHelperBase<NextToMachineType> {

    private static final String TABLE_NAME = "NEXT_TO_MACHINE_TYPE";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_NEXT_TO_MACHINE_TYPE = "next_to_machine_type";

    // Creating table query
    private static final String CREATE_NEXT_TO_MACHINE_TYPE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NEXT_TO_MACHINE_TYPE + " TEXT);";

    private static String[] columnNames = new String[] { ID, COL_NEXT_TO_MACHINE_TYPE };

    public NextToMachineTypeDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_NEXT_TO_MACHINE_TYPE_TABLE;
    }

    @Override
    public ContentValues getContentValues(NextToMachineType entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_NEXT_TO_MACHINE_TYPE, entity.getType());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public NextToMachineType getValue(Cursor cursor) {

        NextToMachineType nextToMachineType = new NextToMachineType();
        nextToMachineType.setId(getColumnIntValue(cursor, ID));
        nextToMachineType.setType(getColumnStringValue(cursor, COL_NEXT_TO_MACHINE_TYPE));
        return nextToMachineType;
    }
}
