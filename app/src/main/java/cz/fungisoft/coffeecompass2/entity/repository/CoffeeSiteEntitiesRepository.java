package cz.fungisoft.coffeecompass2.entity.repository;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

/**
 * Class to hold instancies of CoffeeSite entities readed from server
 * Can return instancies of any such type.
 */
public class CoffeeSiteEntitiesRepository {

    private static List<CoffeeSiteType> allCoffeeSiteTypes;

    private static List<CoffeeSiteRecordStatus> allCoffeeSiteRecordStatuses;

    private static List<CoffeeSiteStatus> allCoffeeSiteStatuses;

    private static List<CoffeeSort> allCoffeeSorts;

    private static List<CupType> allCupTypes;

    private static List<NextToMachineType> allNextToMachineTypes;

    private static List<OtherOffer> allOtherOffers;

    private static List<PriceRange> allPriceRanges;

    private static List<SiteLocationType> allSiteLocationTypes;


    /** SETTERS **/

    public static void setAllCoffeeSiteTypes(List<CoffeeSiteType> allCoffeeSiteTypes) {
        CoffeeSiteEntitiesRepository.allCoffeeSiteTypes = allCoffeeSiteTypes;
    }

    public static void setAllCoffeeSiteRecordStatuses(List<CoffeeSiteRecordStatus> allCoffeeSiteRecordStatuses) {
        CoffeeSiteEntitiesRepository.allCoffeeSiteRecordStatuses = allCoffeeSiteRecordStatuses;
    }

    public static void setAllCoffeeSiteStatuses(List<CoffeeSiteStatus> allCoffeeSiteStatuses) {
        CoffeeSiteEntitiesRepository.allCoffeeSiteStatuses = allCoffeeSiteStatuses;
    }

    public static void setAllCoffeeSorts(List<CoffeeSort> allCoffeeSorts) {
        CoffeeSiteEntitiesRepository.allCoffeeSorts = allCoffeeSorts;
    }

    public static void setAllCupTypes(List<CupType> allCupTypes) {
        CoffeeSiteEntitiesRepository.allCupTypes = allCupTypes;
    }

    public static void setAllNextToMachineTypes(List<NextToMachineType> allNextToMachineTypes) {
        CoffeeSiteEntitiesRepository.allNextToMachineTypes = allNextToMachineTypes;
    }

    public static void setAllOtherOffers(List<OtherOffer> allOtherOffers) {
        CoffeeSiteEntitiesRepository.allOtherOffers = allOtherOffers;
    }

    public static void setAllPriceRanges(List<PriceRange> allPriceRanges) {
        CoffeeSiteEntitiesRepository.allPriceRanges = allPriceRanges;
    }

    public static void setAllSiteLocationTypes(List<SiteLocationType> allSiteLocationTypes) {
        CoffeeSiteEntitiesRepository.allSiteLocationTypes = allSiteLocationTypes;
    }

   /** Methods to return one instance of the selected type from this repository's list of available values of selected type **/

    /**
     * Gets instance of CoffeeSiteType based on value of the typr
     * from list of all available this.allCoffeeSiteTypes.
     * If not in list return null
     * @param value
     * @return
     */
    public static CoffeeSiteType getCoffeeSiteType(String value) {
        return getEntityInstance(value, allCoffeeSiteTypes);
    }

    public static CoffeeSiteRecordStatus getCoffeeSiteRecordStatus(String value) {
        return getEntityInstance(value, allCoffeeSiteRecordStatuses);
    }

    public static CoffeeSiteStatus getCoffeeSiteStatus(String value) {
        return getEntityInstance(value, allCoffeeSiteStatuses);
    }

    public static CoffeeSort getCoffeeSort(String value) {
        return getEntityInstance(value, allCoffeeSorts);
    }

    /**
     * Gets List of CoffeeSiteEntity CoffeeSort instances based on it's values,
     * taken from reposiory list of available instances.
     * @param coffeeSortValues
     * @return
     */
    public static List<CoffeeSort> getCoffeeSortsList(String[] coffeeSortValues) {
        List<CoffeeSort> retVal = new ArrayList<>();
        for (String coffeeSortValue : coffeeSortValues) {
            retVal.add(getCoffeeSort(coffeeSortValue));
        }
        return retVal;
    }

    public static CupType getCupType(String value) {
        return getEntityInstance(value, allCupTypes);
    }

    public static NextToMachineType getNextToMachineType(String value) {
        return getEntityInstance(value, allNextToMachineTypes);
    }

    public static OtherOffer getOtherOffer(String value) {
        return getEntityInstance(value, allOtherOffers);
    }

    /**
     * Gets List of CoffeeSiteEntity OtherOffer instances based on it's values,
     * taken from reposiory list of available instances.
     * @param otherOfferValues
     * @return
     */
    public static List<OtherOffer> getOtherOffersList(String[] otherOfferValues) {
        List<OtherOffer> retVal = new ArrayList<>();
        for (String coffeeSortValue : otherOfferValues) {
            retVal.add(getOtherOffer(coffeeSortValue));
        }
        return retVal;
    }

    public static PriceRange getPriceRange(String value) {
        return getEntityInstance(value, allPriceRanges);
    }

    public static SiteLocationType getSiteLocationType(String value) {
        return getEntityInstance(value, allSiteLocationTypes);
    }

    /**
     * Helper method to find instance of the CoffeeSiteEntity from this
     * repository internal lists of CoffeeSiteEntities of selected CoffeeSiteEntity type.
     * If not found returns null
     *
     * @param value
     * @param entityInstances
     * @param <T>
     * @return
     */
    private static <T extends CoffeeSiteEntity> T getEntityInstance(String value, List<T> entityInstances) {
        for (T entity : entityInstances) {
            if (entity.getEntityValue().equalsIgnoreCase(value)) {
                return entity;
            }
        }
        return null;
    }


    /** ------------- GETTERS ------------------------------ */

    public static List<CoffeeSiteStatus> getAllCoffeeSiteStatuses() {
        return allCoffeeSiteStatuses;
    }

    public static  List<CoffeeSiteType> getAllCoffeeSiteTypes() {
        return allCoffeeSiteTypes;
    }

    public static List<CoffeeSiteRecordStatus> getAllCoffeeSiteRecordStatuses() {
        return allCoffeeSiteRecordStatuses;
    }

    public static List<CoffeeSort> getAllCoffeeSorts() {
        return allCoffeeSorts;
    }


    public static List<CupType> getAllCupTypes() {
        return allCupTypes;
    }


    public static List<NextToMachineType> getAllNextToMachineTypes() {
        return allNextToMachineTypes;
    }


    public static List<OtherOffer> getAllOtherOffers() {
        return allOtherOffers;
    }


    public static List<PriceRange> getAllPriceRanges() {
        return allPriceRanges;
    }


    public static List<SiteLocationType> getAllSiteLocationTypes() {
        return allSiteLocationTypes;
    }

}
