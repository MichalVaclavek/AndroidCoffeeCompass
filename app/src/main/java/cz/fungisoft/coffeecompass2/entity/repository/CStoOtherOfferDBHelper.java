package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;

/**
 * SQLite helper popisujici spojovaci ManyToMany relaci/tabulku mezi CoffeeSite a CoffeeSort
 */
public class CStoOtherOfferDBHelper extends SQLiteHelperBase<CStoOtherOfferDBHelper.CStoOtherOffer> {

    private static final String TABLE_NAME = "COFFEE_SITE_TO_COFFEE_SORT";

    // jmena sloupcu tabulky CStoOtherOffer
    public static final String COL_COFFEE_SITE_ID = "coffee_site_id";
    public static final String COL_OTHER_OFFER_ID = "other_offer_id";

    // Creating table query
    private static final String CREATE_CS_TO_OTHER_OFFER_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_COFFEE_SITE_ID + " INTEGER NOT NULL, "
            + COL_OTHER_OFFER_ID + " INTEGER NOT NULL);";

    private static String[] columnNames = new String[] { ID, COL_COFFEE_SITE_ID, COL_OTHER_OFFER_ID};

    public CStoOtherOfferDBHelper(Context context) {
        super(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_CS_TO_OTHER_OFFER_TABLE;
    }

    @Override
    public ContentValues getContentValues(CStoOtherOffer entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_COFFEE_SITE_ID, entity.getCoffeeSiteId());
        contentValue.put(COL_OTHER_OFFER_ID, entity.getOtherOfferId());

        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CStoOtherOffer getValue(Cursor cursor) {
        CStoOtherOffer cStoOtherOffer = new CStoOtherOffer();
        cStoOtherOffer.setId(getColumnIntValue(cursor, ID));
        cStoOtherOffer.setCoffeeSiteId( getColumnIntValue(cursor, COL_COFFEE_SITE_ID));
        cStoOtherOffer.setOtherOfferId(getColumnIntValue(cursor, COL_OTHER_OFFER_ID));

        return cStoOtherOffer;
    }

    /**
     * Holder class to hold relation between CoffeeSite ID and Coffee sort ID
     */
    class CStoOtherOffer extends CoffeeSiteEntity {

        public int getCoffeeSiteId() {
            return coffeeSiteId;
        }

        public void setCoffeeSiteId(int coffeeSiteId) {
            this.coffeeSiteId = coffeeSiteId;
        }

        public int getOtherOfferId() {
            return otherOfferId;
        }

        public void setOtherOfferId(int otherOfferId) {
            this.otherOfferId = otherOfferId;
        }

        private int coffeeSiteId;

        private int otherOfferId;
    }

}
