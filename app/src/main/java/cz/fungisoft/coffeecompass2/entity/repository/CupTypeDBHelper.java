package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CupType;

public class CupTypeDBHelper extends SQLiteHelperBase<CupType> {

    private static final String TABLE_NAME = "CUP_TYPE";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_CUP_TYPE = "cup_type";

    // Creating table query
    private static final String CREATE_CUP_TYPE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_CUP_TYPE + " TEXT);";

    private static String[] columnNames = new String[] { ID, COL_CUP_TYPE };

    public CupTypeDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_CUP_TYPE_TABLE;
    }

    @Override
    public ContentValues getContentValues(CupType entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_CUP_TYPE, entity.getCupType());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CupType getValue(Cursor cursor) {
        return null;
    }
}
