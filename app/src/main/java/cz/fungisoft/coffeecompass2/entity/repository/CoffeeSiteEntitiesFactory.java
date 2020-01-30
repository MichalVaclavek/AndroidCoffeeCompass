package cz.fungisoft.coffeecompass2.entity.repository;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;

public class CoffeeSiteEntitiesFactory {

    private static final String TAG = "CoffeeSiteEntitiesFact";

    public static CoffeeSiteEntity getEntity(String entityType, int id, String value) {
        if (entityType == null) {
            return null;
        }

        if (entityType.equalsIgnoreCase("CoffeeSiteRecordStatus")) {
            return new CoffeeSiteRecordStatus(id, value);
        }
        if (entityType.equalsIgnoreCase("CoffeeSiteStatus")) {
            return new CoffeeSiteStatus(id, value);
        }
        if (entityType.equalsIgnoreCase("CoffeeSiteType")) {
            return new CoffeeSiteType(id, value);
        }
        if (entityType.equalsIgnoreCase("CoffeeSort")) {
            return new CoffeeSort(id, value);
        }
        if (entityType.equalsIgnoreCase("CupType")) {
            return new CupType(id, value);
        }
        if (entityType.equalsIgnoreCase("NextToMachineType")) {
            return new NextToMachineType(id, value);
        }
        if (entityType.equalsIgnoreCase("OtherOffer")) {
            return new OtherOffer(id, value);
        }
        if (entityType.equalsIgnoreCase("PriceRange")) {
            return new PriceRange(id, value);
        }
        if (entityType.equalsIgnoreCase("SiteLocationType")) {
            return new SiteLocationType(id, value);
        }

        return null;
    }

    public static CoffeeSiteEntity getEntity(String entityType, JSONObject jsonObject) {

        CoffeeSiteEntity retVal = null;

        if (entityType == null || jsonObject == null) {
            return null;
        }

        try {
            if (entityType.equalsIgnoreCase("CoffeeSiteRecordStatus")) {
                retVal = new CoffeeSiteRecordStatus();
                ((CoffeeSiteRecordStatus) retVal).setStatus(jsonObject.getString("status"));
            }
            if (entityType.equalsIgnoreCase("CoffeeSiteStatus")) {
                retVal = new CoffeeSiteStatus();
                ((CoffeeSiteStatus) retVal).setStatus(jsonObject.getString("status"));
            }
            if (entityType.equalsIgnoreCase("CoffeeSiteType")) {
                retVal = new CoffeeSiteType();
                ((CoffeeSiteType) retVal).setCoffeeSiteType(jsonObject.getString("coffeeSiteType"));
            }
            if (entityType.equalsIgnoreCase("CoffeeSort")) {
                retVal = new CoffeeSort();
                ((CoffeeSort) retVal).setCoffeeSort(jsonObject.getString("coffeeSort"));
            }
            if (entityType.equalsIgnoreCase("CupType")) {
                retVal = new CupType();
                ((CupType) retVal).setCupType(jsonObject.getString("cupType"));
            }
            if (entityType.equalsIgnoreCase("NextToMachineType")) {
                retVal = new NextToMachineType();
                ((NextToMachineType) retVal).setType(jsonObject.getString("type"));
            }
            if (entityType.equalsIgnoreCase("OtherOffer")) {
                retVal = new OtherOffer();
                ((OtherOffer) retVal).setOffer(jsonObject.getString("offer"));
            }
            if (entityType.equalsIgnoreCase("PriceRange")) {
                retVal = new PriceRange();
                ((PriceRange) retVal).setPriceRange(jsonObject.getString("priceRange"));
            }
            if (entityType.equalsIgnoreCase("SiteLocationType")) {
                retVal = new SiteLocationType();
                ((SiteLocationType) retVal).setLocationType(jsonObject.getString("locationType"));
            }

            if (retVal != null) {
                retVal.setId(jsonObject.getInt("id"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Exception during parsing JSON : " + e.getMessage());
        }

        return retVal;
    }

}
