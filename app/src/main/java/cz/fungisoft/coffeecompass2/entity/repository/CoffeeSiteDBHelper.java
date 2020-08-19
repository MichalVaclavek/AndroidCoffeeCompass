package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfHodnoceni;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;

public class CoffeeSiteDBHelper extends SQLiteHelperBase<CoffeeSite> {

    private static final String TABLE_NAME = "COFFEE_SITE";

    // jmena sloupcu tabulky CoffeeSite
    public static final String COL_SITE_NAME = "site_name";
    public static final String COL_CREATED_ON = "created_on";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_MAIN_IMAGE_URL = "main_image_url";

    public static final String COL_STATUS_ZARIZENI_ID = "status_zarizeni_id";
    public static final String COL_TYP_PODNIKU_ID = "typ_podniku_id";
    public static final String COL_TYP_LOKALITY_ID = "typ_lokality_id";
    public static final String COL_CENA_ID = "cena_id";

    public static final String COL_MESTO = "mesto";
    public static final String COL_ULICE = "ulice";
    public static final String COL_HODNOCENI_ID = "hodnoceni_id";

    public static final String COL_CREATED_BY_USERNAME = "created_by_username";
    public static final String COL_LAST_EDIT_USERNAME = "last_edit_username";
    public static final String COL_UVODNI_KOMENT = "uvodni_koment";

    public static final String COL_OPENING_DAYS = "opening_days";
    public static final String COL_OPENING_HOURS = "opening_hours";

    public static final String COL_STATUS_ZAZNAMU_ID = "status_zaznamu_id";

    private static String[] columnNames = new String[] {ID, COL_SITE_NAME, COL_CREATED_ON,
            COL_LATITUDE, COL_LONGITUDE, COL_MAIN_IMAGE_URL, COL_STATUS_ZARIZENI_ID,
            COL_TYP_PODNIKU_ID, COL_TYP_LOKALITY_ID, COL_CENA_ID, COL_MESTO, COL_ULICE,
            COL_HODNOCENI_ID, COL_CREATED_BY_USERNAME, COL_LAST_EDIT_USERNAME, COL_UVODNI_KOMENT,
            COL_OPENING_DAYS, COL_OPENING_HOURS, COL_STATUS_ZAZNAMU_ID };

    // Creating table query
    private static final String CREATE_COFFEE_SITE_TABLE = "create table " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_SITE_NAME + " TEXT NOT NULL, "
            + COL_CREATED_ON + " TEXT NOT NULL, "

            + COL_LATITUDE + " REAL NOT NULL, "
            + COL_LONGITUDE + " REAL NOT NULL, "
            + COL_MAIN_IMAGE_URL + " TEXT, "
            + COL_STATUS_ZARIZENI_ID + " INTEGER NOT NULL, "

            + COL_TYP_PODNIKU_ID + " INTEGER NOT NULL, "
            + COL_TYP_LOKALITY_ID + " INTEGER NOT NULL, "
            + COL_CENA_ID + " INTEGER, "

            + COL_MESTO + " TEXT, "
            + COL_ULICE + " TEXT, "
            + COL_HODNOCENI_ID + " INTEGER, "
            + COL_CREATED_BY_USERNAME + " TEXT NOT NULL, "
            + COL_LAST_EDIT_USERNAME + " TEXT, "
            + COL_UVODNI_KOMENT + " TEXT, "
            + COL_OPENING_DAYS + " TEXT, "
            + COL_OPENING_HOURS + " TEXT, "

            + COL_STATUS_ZAZNAMU_ID + " INTEGER NOT NULL);";

    // All other needed DBHelpers (Repositories) to obtain info needed for CoffeeSite
    private PriceRangeDBHelper priceRangeDBHelper;
    private AverageStarsWithNumOfHodnoceniDBHelper averageStarsWithNumOfHodnoceniDBHelper;
    private CoffeeSiteStatusDBHelper statusZarizeniDBHelper;
    private CoffeeSiteTypeDBHelper siteTypeDBHelper;
    private SiteLocationTypeDBHelper siteLocationTypeDBHelper;
    private CoffeeSiteRecordStatusDBHelper siteRecordStatusDBHelper;

    public CoffeeSiteDBHelper(Context context, DBManager dbManager) {
        super(context);
        SQLiteHelperBase.dbManager = dbManager;

        priceRangeDBHelper = new PriceRangeDBHelper(context);
        averageStarsWithNumOfHodnoceniDBHelper = new AverageStarsWithNumOfHodnoceniDBHelper(context);
        statusZarizeniDBHelper = new CoffeeSiteStatusDBHelper(context);
        siteLocationTypeDBHelper = new SiteLocationTypeDBHelper(context);
        siteTypeDBHelper = new CoffeeSiteTypeDBHelper(context);
        siteRecordStatusDBHelper = new CoffeeSiteRecordStatusDBHelper(context);
    }

    @Override
    public String getDbTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableScript() {
        return CREATE_COFFEE_SITE_TABLE;
    }

    @Override
    public ContentValues getContentValues(CoffeeSite entity) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COL_SITE_NAME, entity.getName());
        contentValue.put(COL_CREATED_ON, entity.getCreatedOnString());
        contentValue.put(COL_LATITUDE, entity.getLatitude());
        contentValue.put(COL_LONGITUDE, entity.getLongitude());

        contentValue.put(COL_MAIN_IMAGE_URL, entity.getMainImageURL());
        
        dbManager.open(statusZarizeniDBHelper);
        contentValue.put(COL_STATUS_ZARIZENI_ID, dbManager.getEntityId(entity.getStatusZarizeni()));
        dbManager.close();

        dbManager.open(siteTypeDBHelper);
        contentValue.put(COL_TYP_PODNIKU_ID, dbManager.getEntityId(entity.getTypPodniku()));
        dbManager.close();

        dbManager.open(siteLocationTypeDBHelper);
        contentValue.put(COL_TYP_LOKALITY_ID, dbManager.getEntityId(entity.getTypLokality()));
        dbManager.close();

        dbManager.open(priceRangeDBHelper);
        contentValue.put(COL_CENA_ID, dbManager.getEntityId(entity.getCena()));
        dbManager.close();

        dbManager.open(averageStarsWithNumOfHodnoceniDBHelper);
        contentValue.put(COL_HODNOCENI_ID, dbManager.getEntityId(entity.getHodnoceni()));
        dbManager.close();

        dbManager.open(siteRecordStatusDBHelper);
        contentValue.put(COL_STATUS_ZAZNAMU_ID, dbManager.getEntityId(entity.getStatusZaznamu()));
        dbManager.close();

        contentValue.put(COL_MESTO, entity.getMesto());
        contentValue.put(COL_ULICE, entity.getUliceCP());

        contentValue.put(COL_CREATED_BY_USERNAME, entity.getCreatedByUserName());
        contentValue.put(COL_LAST_EDIT_USERNAME, entity.getLastEditUserName());

        contentValue.put(COL_UVODNI_KOMENT, entity.getUvodniKoment());
        contentValue.put(COL_OPENING_DAYS, entity.getOteviraciDobaDny());
        contentValue.put(COL_OPENING_HOURS, entity.getOteviraciDobaHod());

        //TODO - ManyToMany Relations
        // CoffeeSorts, CupTypes, OtherOffer, NextToMachineTypes

        return contentValue;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public CoffeeSite getValue(Cursor cursor) {
        CoffeeSite coffeeSite = new CoffeeSite();

        coffeeSite.setId(getColumnIntValue(cursor, ID));
        coffeeSite.setName(getColumnStringValue(cursor, COL_SITE_NAME));

        coffeeSite.setCreatedOnString(getColumnStringValue(cursor, COL_CREATED_ON));
        coffeeSite.setLatitude(getColumnRealValue(cursor, COL_LATITUDE));
        coffeeSite.setLongitude(getColumnRealValue(cursor, COL_LONGITUDE));

        coffeeSite.setMainImageURL(getColumnStringValue(cursor, COL_MAIN_IMAGE_URL));

        dbManager.open(siteRecordStatusDBHelper);
        CoffeeSiteRecordStatus recordStatusStatus = dbManager.getValue(getColumnIntValue(cursor, COL_STATUS_ZAZNAMU_ID));
        coffeeSite.setStatusZaznamu(recordStatusStatus);
        dbManager.close();

        dbManager.open(statusZarizeniDBHelper);
        CoffeeSiteStatus siteStatus = dbManager.getValue(getColumnIntValue(cursor, COL_STATUS_ZARIZENI_ID));
        coffeeSite.setStatusZarizeni(siteStatus);
        dbManager.close();

        dbManager.open(siteLocationTypeDBHelper);
        SiteLocationType typLokality = dbManager.getValue(getColumnIntValue(cursor, COL_TYP_LOKALITY_ID));
        coffeeSite.setTypLokality(typLokality);
        dbManager.close();

        dbManager.open(siteTypeDBHelper);
        CoffeeSiteType typPodniku = dbManager.getValue(getColumnIntValue(cursor, COL_TYP_PODNIKU_ID));
        coffeeSite.setTypPodniku(typPodniku);
        dbManager.close();

        dbManager.open(priceRangeDBHelper);
        PriceRange priceRange = dbManager.getValue(getColumnIntValue(cursor, COL_CENA_ID));
        dbManager.close();
        coffeeSite.setCena(priceRange);

        dbManager.open(averageStarsWithNumOfHodnoceniDBHelper);
        AverageStarsWithNumOfHodnoceni hodnoceni = dbManager.getValue(getColumnIntValue(cursor, COL_HODNOCENI_ID));
        coffeeSite.setHodnoceni(hodnoceni);
        dbManager.close();

        coffeeSite.setMesto(getColumnStringValue(cursor, COL_MESTO));
        coffeeSite.setUliceCP(getColumnStringValue(cursor, COL_ULICE));
        coffeeSite.setCreatedByUserName(getColumnStringValue(cursor, COL_CREATED_BY_USERNAME));
        coffeeSite.setLastEditUserName(getColumnStringValue(cursor, COL_LAST_EDIT_USERNAME));
        coffeeSite.setOteviraciDobaDny(getColumnStringValue(cursor, COL_OPENING_DAYS));
        coffeeSite.setOteviraciDobaHod(getColumnStringValue(cursor, COL_OPENING_HOURS));

        coffeeSite.setUvodniKoment(getColumnStringValue(cursor, COL_UVODNI_KOMENT));

        return coffeeSite;
    }

}
