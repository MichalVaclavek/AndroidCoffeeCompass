package cz.fungisoft.coffeecompass2.entity.repository;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
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
            = new Class[] { CoffeeSiteRecordStatus.class, CoffeeSiteStatus.class, CoffeeSiteType.class,
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

    private DBManager dbManager;


    private static CoffeeSiteEntitiesRepository instance;

    private CoffeeSiteEntitiesRepository(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public static CoffeeSiteEntitiesRepository getInstance(DBManager dbManager) {
        if (instance == null) {
            instance = new CoffeeSiteEntitiesRepository(dbManager);
        }
        return instance;
    }


    /** SETTERS **/

    public void setAllCoffeeSiteTypes(List<CoffeeSiteType> allCoffeeSiteTypes) {
        this.allCoffeeSiteTypes = allCoffeeSiteTypes;

        CoffeeSiteTypeDBHelper coffeeSiteTypeDBHelper = new CoffeeSiteTypeDBHelper(dbManager.getContext());
        dbManager.open(coffeeSiteTypeDBHelper);
        for (CoffeeSiteType coffeeSiteType : this.allCoffeeSiteTypes) {
            dbManager.insert(coffeeSiteType);
        }

        dbManager.close();
    }

    public void setAllCoffeeSiteRecordStatuses(List<CoffeeSiteRecordStatus> allCoffeeSiteRecordStatuses) {
        this.allCoffeeSiteRecordStatuses = allCoffeeSiteRecordStatuses;

        CoffeeSiteRecordStatusDBHelper recordStatusDBHelper = new CoffeeSiteRecordStatusDBHelper(dbManager.getContext());
        dbManager.open(recordStatusDBHelper);
        for (CoffeeSiteRecordStatus recordStatus : this.allCoffeeSiteRecordStatuses) {
            dbManager.insert(recordStatus);
        }

        dbManager.close();
    }

    public void setAllCoffeeSiteStatuses(List<CoffeeSiteStatus> allCoffeeSiteStatuses) {
        this.allCoffeeSiteStatuses = allCoffeeSiteStatuses;

        CoffeeSiteStatusDBHelper coffeeSiteStatusDBHelper = new CoffeeSiteStatusDBHelper(dbManager.getContext());
        dbManager.open(coffeeSiteStatusDBHelper);
        for (CoffeeSiteStatus siteStatus : this.allCoffeeSiteStatuses) {
            dbManager.insert(siteStatus);
        }

        dbManager.close();
    }

    public void setAllCoffeeSorts(List<CoffeeSort> allCoffeeSorts) {
        this.allCoffeeSorts = allCoffeeSorts;

        CoffeeSortDBHelper coffeeSortDBHelper = new CoffeeSortDBHelper(dbManager.getContext());
        dbManager.open(coffeeSortDBHelper);
        for (CoffeeSort coffeeSort : this.allCoffeeSorts) {
            dbManager.insert(coffeeSort);
        }

        dbManager.close();
    }

    public void setAllCupTypes(List<CupType> allCupTypes) {

        this.allCupTypes = allCupTypes;

        CupTypeDBHelper cupTypeDBHelper = new CupTypeDBHelper(dbManager.getContext());
        dbManager.open(cupTypeDBHelper);
        for (CupType cupType : this.allCupTypes) {
            dbManager.insert(cupType);
        }

        dbManager.close();
    }

    public void setAllNextToMachineTypes(List<NextToMachineType> allNextToMachineTypes) {
        this.allNextToMachineTypes = allNextToMachineTypes;

        NextToMachineTypeDBHelper nextToMachineTypeDBHelper = new NextToMachineTypeDBHelper(dbManager.getContext());
        dbManager.open(nextToMachineTypeDBHelper);
        for (NextToMachineType nextToMachineType : this.allNextToMachineTypes) {
            dbManager.insert(nextToMachineType);
        }

        dbManager.close();
    }

    public void setAllOtherOffers(List<OtherOffer> allOtherOffers) {
        this.allOtherOffers = allOtherOffers;

        OtherOfferDBHelper otherOfferDBHelper = new OtherOfferDBHelper(dbManager.getContext());
        dbManager.open(otherOfferDBHelper);
        for (OtherOffer otherOffer : this.allOtherOffers) {
            dbManager.insert(otherOffer);
        }

        dbManager.close();
    }

    public void setAllPriceRanges(List<PriceRange> allPriceRanges) {
        this.allPriceRanges = allPriceRanges;

        PriceRangeDBHelper priceRangeDBHelper = new PriceRangeDBHelper(dbManager.getContext());
        dbManager.open(priceRangeDBHelper);
        for (PriceRange priceRange : this.allPriceRanges) {
            dbManager.insert(priceRange);
        }

        dbManager.close();
    }

    public void setAllSiteLocationTypes(List<SiteLocationType> allSiteLocationTypes) {
        this.allSiteLocationTypes = allSiteLocationTypes;

        SiteLocationTypeDBHelper siteLocationTypeDBHelper = new SiteLocationTypeDBHelper(dbManager.getContext());
        dbManager.open(siteLocationTypeDBHelper);
        for (SiteLocationType locationType : this.allSiteLocationTypes) {
            dbManager.insert(locationType);
        }

        dbManager.close();
    }

    public void setAllStarsQualityDescriptions(List<StarsQualityDescription> allStarsQualityDescriptions) {
        CoffeeSiteEntitiesRepository.allStarsQualityDescriptions = allStarsQualityDescriptions;

        StarsQualityDescriptionDBHelper qualityDescriptionDBHelper = new StarsQualityDescriptionDBHelper(dbManager.getContext());
        dbManager.open(qualityDescriptionDBHelper);
        for (StarsQualityDescription qualityDescription : CoffeeSiteEntitiesRepository.allStarsQualityDescriptions) {
            dbManager.insert(qualityDescription);
        }

        dbManager.close();
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
