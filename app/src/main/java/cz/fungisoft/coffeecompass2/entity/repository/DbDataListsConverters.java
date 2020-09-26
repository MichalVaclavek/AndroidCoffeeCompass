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
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;

public class DbDataListsConverters implements Serializable {

    /* **** Converter for List of CoffeeSort **** */

    @TypeConverter
    public String fromCoffeeSorts(List<CoffeeSort> coffeeSorts){
        if(coffeeSorts == null){
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<CoffeeSort>>(){}.getType();
        String json = gson.toJson(coffeeSorts, type);
        return json;
    }

    @TypeConverter
    public List<CoffeeSort> toCoffeeSorts(String coffeeSortsString){
        if(coffeeSortsString == null){
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<CoffeeSort>>(){}.getType();
        List<CoffeeSort> coffeeSorts = gson.fromJson(coffeeSortsString, type);
        return coffeeSorts;
    }

    /* **** Converter for List of CupType **** */

    @TypeConverter
    public String fromCupTypes(List<CupType> cupTypes){
        if(cupTypes == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<CupType>>(){}.getType();
        String json = gson.toJson(cupTypes, type);
        return json;
    }

    @TypeConverter
    public List<CupType> toCupTypes(String cupTypesString){
        if(cupTypesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<CupType>>(){}.getType();
        List<CupType> cupTypes = gson.fromJson(cupTypesString, type);
        return cupTypes;
    }

    /* **** Converter for List of NextToMachineType **** */

    @TypeConverter
    public String fromNextToMachineTypes(List<NextToMachineType> nextToMachineTypes){
        if(nextToMachineTypes == null){
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<NextToMachineType>>(){}.getType();
        String json = gson.toJson(nextToMachineTypes, type);
        return json;
    }

    @TypeConverter
    public List<NextToMachineType> toNextToMachineTypes(String nextToMachineTypesString){
        if(nextToMachineTypesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<NextToMachineType>>(){}.getType();
        List<NextToMachineType> nextToMachineTypes = gson.fromJson(nextToMachineTypesString, type);
        return nextToMachineTypes;
    }

    /* **** Converter for List of OtherOffer **** */

    @TypeConverter
    public String fromOtherOffers(List<OtherOffer> otherOffers){
        if(otherOffers == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<OtherOffer>>(){}.getType();
        String json = gson.toJson(otherOffers, type);
        return json;
    }

    @TypeConverter
    public List<OtherOffer> toOtherOffers(String otherOffersString){
        if(otherOffersString == null){
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<OtherOffer>>(){}.getType();
        List<OtherOffer> otherOffers = gson.fromJson(otherOffersString, type);
        return otherOffers;
    }

}
