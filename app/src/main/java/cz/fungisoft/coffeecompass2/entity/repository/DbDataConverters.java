package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfRatings;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;

public class DbDataConverters implements Serializable {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

        /* **** Converter for CoffeeSiteType **** */

        @TypeConverter
        public String fromCoffeeSiteTypeToString(CoffeeSiteType coffeeSiteType) {
            if (coffeeSiteType == null) {
                return (null);
            }
            Gson gson = new Gson();
            Type type = new TypeToken<CoffeeSiteType>() {
            }.getType();
            String json = gson.toJson(coffeeSiteType, type);
            return json;
        }

        @TypeConverter
        public CoffeeSiteType toCoffeeSiteType(String coffeeSiteTypeString) {
            if (coffeeSiteTypeString == null) {
                return (null);
            }
            Gson gson = new Gson();
            Type type = new TypeToken<CoffeeSiteType>() {
            }.getType();
            CoffeeSiteType coffeeSiteType = gson.fromJson(coffeeSiteTypeString, type);
            return coffeeSiteType;
        }

    /* **** Converter for CoffeeSiteRecordStatus **** */

    @TypeConverter
    public String fromCoffeeSiteRecordStatusToString(CoffeeSiteRecordStatus coffeeSiteRecordStatus) {
        if (coffeeSiteRecordStatus == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<CoffeeSiteRecordStatus>() {
        }.getType();
        String json = gson.toJson(coffeeSiteRecordStatus, type);
        return json;
    }

    @TypeConverter
    public CoffeeSiteRecordStatus toCoffeeSiteRecordStatus(String coffeeSiteRecordStatusString) {
        if (coffeeSiteRecordStatusString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<CoffeeSiteRecordStatus>() {
        }.getType();
        CoffeeSiteRecordStatus coffeeSiteRecordStatus = gson.fromJson(coffeeSiteRecordStatusString, type);
        return coffeeSiteRecordStatus;
    }

    /* **** Converter for CoffeeSiteRecordStatus **** */

    @TypeConverter
    public String fromCoffeeSiteStatusToString(CoffeeSiteStatus coffeeSiteStatus) {
        if (coffeeSiteStatus == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<CoffeeSiteStatus>() {
        }.getType();
        String json = gson.toJson(coffeeSiteStatus, type);
        return json;
    }

    @TypeConverter
    public CoffeeSiteStatus toCoffeeSiteStatus(String coffeeSiteStatusString) {
        if (coffeeSiteStatusString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<CoffeeSiteStatus>() {
        }.getType();
        CoffeeSiteStatus coffeeSiteStatus = gson.fromJson(coffeeSiteStatusString, type);
        return coffeeSiteStatus;
    }

    /* **** Converter for AverageStarsWithNumOfRatings **** */

    @TypeConverter
    public String fromAverageStarsWithNumOfRatingsToString(AverageStarsWithNumOfRatings averageStarsWithNumOfRatings) {
        if (averageStarsWithNumOfRatings == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<AverageStarsWithNumOfRatings>() {
        }.getType();
        String json = gson.toJson(averageStarsWithNumOfRatings, type);
        return json;
    }

    @TypeConverter
    public AverageStarsWithNumOfRatings toAverageStarsWithNumOfRatings(String averageStarsWithNumOfRatingsString) {
        if (averageStarsWithNumOfRatingsString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<AverageStarsWithNumOfRatings>() {
        }.getType();
        AverageStarsWithNumOfRatings averageStarsWithNumOfRatings = gson.fromJson(averageStarsWithNumOfRatingsString, type);
        return averageStarsWithNumOfRatings;
    }

    /* **** Converter for SiteLocationType **** */

    @TypeConverter
    public String fromSiteLocationTypeToString(SiteLocationType siteLocationType) {
        if (siteLocationType == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<SiteLocationType>() {
        }.getType();
        String json = gson.toJson(siteLocationType, type);
        return json;
    }

    @TypeConverter
    public SiteLocationType toSiteLocationType(String siteLocationTypeString) {
        if (siteLocationTypeString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<SiteLocationType>() {
        }.getType();
        SiteLocationType siteLocationType = gson.fromJson(siteLocationTypeString, type);
        return siteLocationType;
    }

    /* **** Converter for PriceRange **** */

    @TypeConverter
    public String fromPriceRangeToString(PriceRange priceRange) {
        if (priceRange == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<PriceRange>() {
        }.getType();
        String json = gson.toJson(priceRange, type);
        return json;
    }

    @TypeConverter
    public PriceRange toPriceRange(String priceRangeString) {
        if (priceRangeString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<PriceRange>() {
        }.getType();
        PriceRange priceRange = gson.fromJson(priceRangeString, type);
        return priceRange;
    }

}
