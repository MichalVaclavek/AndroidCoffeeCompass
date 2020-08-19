package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;

public class OtherOfferDBHelper extends SQLiteHelperBase<OtherOffer> {

    private static final String TABLE_NAME = "OTHER_OFFER";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_OTHER_OFFER = "other_offer";

    // Creating table query
    private static final String CREATE_OTHER_OFFER_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_OTHER_OFFER + " TEXT);";

    private static String[] columnNames = new String[] { ID, COL_OTHER_OFFER };

    public OtherOfferDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_OTHER_OFFER_TABLE;
    }

    @Override
    public ContentValues getContentValues(OtherOffer entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_OTHER_OFFER, entity.getOffer());
        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public OtherOffer getValue(Cursor cursor) {

        OtherOffer otherOffer = new OtherOffer();
        otherOffer.setId(getColumnIntValue(cursor, ID));
        otherOffer.setOffer(getColumnStringValue(cursor, COL_OTHER_OFFER));
        return otherOffer;
    }
}
