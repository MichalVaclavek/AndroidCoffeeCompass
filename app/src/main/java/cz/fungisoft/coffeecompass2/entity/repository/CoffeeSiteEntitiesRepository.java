package cz.fungisoft.coffeecompass2.entity.repository;

import android.util.Log;

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
import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import retrofit2.Response;

/**
 * Class to hold instancies of CoffeeSite entities readed from server
 * Can return instancies of any such type.
 */
public class CoffeeSiteEntitiesRepository {

    /**
     *  Array of all CoffeeSiteEntity Classes to be loaded from server to repository
     *  needed to correct function of creating/updating CoffeeSite instancies
     */
    public static final Class<? extends CoffeeSiteEntity>[] COFFEE_SITE_ENTITY_CLASSES
            = new Class[]{CoffeeSiteRecordStatus.class, CoffeeSiteStatus.class, CoffeeSiteType.class,
            CoffeeSort.class, CupType.class, NextToMachineType.class, OtherOffer.class, PriceRange.class,
            SiteLocationType.class, StarsQualityDescription.class};


    private static List<CoffeeSiteType> allCoffeeSiteTypes = new ArrayList<>();

    private static List<CoffeeSiteRecordStatus> allCoffeeSiteRecordStatuses = new ArrayList<>();

    private static List<CoffeeSiteStatus> allCoffeeSiteStatuses = new ArrayList<>();

    private static List<CoffeeSort> allCoffeeSorts = new ArrayList<>();

    private static List<CupType> allCupTypes = new ArrayList<>();

    private static List<NextToMachineType> allNextToMachineTypes = new ArrayList<>();

    private static List<OtherOffer> allOtherOffers = new ArrayList<>();

    private static List<PriceRange> allPriceRanges = new ArrayList<>();

    private static List<SiteLocationType> allSiteLocationTypes = new ArrayList<>();

    private static List<StarsQualityDescription> allStarsQualityDescriptions = new ArrayList<>();


    public static boolean isDataReadedFromServer() {
        return dataReadedFromServer;
    }

    public void setDataReadedFromServer(boolean dataReadedFromServer) {
        this.dataReadedFromServer = dataReadedFromServer;
    }

    // Indication that data are available in the repository i.e. where readed from server
    private static boolean dataReadedFromServer = false;



    private static CoffeeSiteEntitiesRepository instance;

    private CoffeeSiteEntitiesRepository() {
    }

    public static CoffeeSiteEntitiesRepository getInstance() {
        if (instance == null) {
            instance = new CoffeeSiteEntitiesRepository();
        }
        return instance;
    }


    /** SETTERS **/

    public void setAllCoffeeSiteTypes(List<CoffeeSiteType> allCoffeeSiteTypes) {
        this.allCoffeeSiteTypes = allCoffeeSiteTypes;
    }

    public void setAllCoffeeSiteRecordStatuses(List<CoffeeSiteRecordStatus> allCoffeeSiteRecordStatuses) {
        this.allCoffeeSiteRecordStatuses = allCoffeeSiteRecordStatuses;
    }

    public void setAllCoffeeSiteStatuses(List<CoffeeSiteStatus> allCoffeeSiteStatuses) {
        this.allCoffeeSiteStatuses = allCoffeeSiteStatuses;
    }

    public void setAllCoffeeSorts(List<CoffeeSort> allCoffeeSorts) {
        this.allCoffeeSorts = allCoffeeSorts;
    }

    public void setAllCupTypes(List<CupType> allCupTypes) {
        this.allCupTypes = allCupTypes;
    }

    public void setAllNextToMachineTypes(List<NextToMachineType> allNextToMachineTypes) {
        this.allNextToMachineTypes = allNextToMachineTypes;
    }

    public void setAllOtherOffers(List<OtherOffer> allOtherOffers) {
        this.allOtherOffers = allOtherOffers;
    }

    public void setAllPriceRanges(List<PriceRange> allPriceRanges) {
        this.allPriceRanges = allPriceRanges;
    }

    public void setAllSiteLocationTypes(List<SiteLocationType> allSiteLocationTypes) {
        this.allSiteLocationTypes = allSiteLocationTypes;
    }

    public static void setAllStarsQualityDescriptions(List<StarsQualityDescription> allStarsQualityDescriptions) {
        CoffeeSiteEntitiesRepository.allStarsQualityDescriptions = allStarsQualityDescriptions;
    }


    public <T extends List<? extends CoffeeSiteEntity>> void setEntities(T response) {
        if (response.size() > 0) {
            if (response.get(0) instanceof CoffeeSiteType) {
                setAllCoffeeSiteTypes((List<CoffeeSiteType>) response);
            }
            if (response.get(0) instanceof CoffeeSiteRecordStatus) {
                setAllCoffeeSiteRecordStatuses((List<CoffeeSiteRecordStatus>) response);
            }
            if (response.get(0) instanceof CoffeeSiteStatus) {
                setAllCoffeeSiteStatuses((List<CoffeeSiteStatus>) response);
            }
            if (response.get(0) instanceof CoffeeSort) {
                setAllCoffeeSorts((List<CoffeeSort>) response);
            }
            if (response.get(0) instanceof CupType) {
                setAllCupTypes((List<CupType>) response);
            }

            if (response.get(0) instanceof NextToMachineType) {
                setAllNextToMachineTypes((List<NextToMachineType>) response);
            }
            if (response.get(0) instanceof OtherOffer) {
                setAllOtherOffers((List<OtherOffer>) response);
            }
            if (response.get(0) instanceof PriceRange) {
                setAllPriceRanges((List<PriceRange>) response);
            }
            if (response.get(0) instanceof SiteLocationType) {
                setAllSiteLocationTypes((List<SiteLocationType>) response);
            }
            if (response.get(0) instanceof StarsQualityDescription) {
                setAllStarsQualityDescriptions((List<StarsQualityDescription>) response);
            }

        }
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
     * If not found returns null.
     * Expects that values in input entityInstances List<T> sre unique
     *
     * @param value
     * @param entityInstances
     * @param <T>
     * @return
     */
    private static <T extends CoffeeSiteEntity> T getEntityInstance(String value, List<T> entityInstances) {
        if (entityInstances != null) {
            for (T entity : entityInstances) {
                if (entity.toString().equalsIgnoreCase(value)) {
                    return entity;
                }
            }
        }
        return null;
    }


    /** ------------- GETTERS ------------------------------ */

    public static List<CoffeeSiteStatus> getAllCoffeeSiteStatuses() {
        return allCoffeeSiteStatuses;
    }

    public static List<CoffeeSiteType> getAllCoffeeSiteTypes() {
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

    public static List<StarsQualityDescription> getAllStarsQualityDescriptions() {
        return allStarsQualityDescriptions;
    }

}
