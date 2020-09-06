package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

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
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntityRepositories;

/**
 * Data Model to be connected to Activities, which needs CoffeeSiteEntity data available.
 */
public class CoffeeSiteEntitiesViewModel extends AndroidViewModel {

    /**
     * Common repository
     */
    private CoffeeSiteEntityRepositories mRepositories;


    private LiveData<List<CoffeeSiteType>> allCoffeeSiteTypes;

    private LiveData<List<CoffeeSiteRecordStatus>> allCoffeeSiteRecordStatuses;

    private LiveData<List<CoffeeSiteStatus>> allCoffeeSiteStatuses;

    private LiveData<List<CoffeeSort>> allCoffeeSorts;

    private LiveData<List<CupType>> allCupTypes;

    private LiveData<List<NextToMachineType>> allNextToMachineTypes;

    private LiveData<List<OtherOffer>> allOtherOffers;

    private LiveData<List<PriceRange>> allPriceRanges;

    private LiveData<List<SiteLocationType>> allSiteLocationTypes;

    private LiveData<List<StarsQualityDescription>> allStarsQualityDescriptions;


    /**
     * Standard constructor
     *
     * @param application
     */
    public CoffeeSiteEntitiesViewModel(@NonNull Application application) {
        super(application);
        mRepositories = CoffeeSiteEntityRepositories.getInstance(application.getApplicationContext());

        allCoffeeSiteTypes = mRepositories.getCoffeeSiteTypesRepository().getAllCoffeeSiteTypes();
        allCoffeeSiteRecordStatuses = mRepositories.getCoffeeSiteRecordStatusRepository().getAllCoffeeSiteRecordStatuses();
        allCoffeeSiteStatuses = mRepositories.getCoffeeSiteStatusRepository().getAllCoffeeSiteStatuses();
        allCoffeeSorts = mRepositories.getCoffeeSortRepository().getAllCoffeeSorts();
        allCupTypes = mRepositories.getCupTypeRepository().getAllCupTypes();
        allNextToMachineTypes = mRepositories.getNextToMachineTypeRepository().getAllNextToMachineTypes();
        allOtherOffers = mRepositories.getOtherOfferRepository().getAllOtherOffers();
        allPriceRanges = mRepositories.getPriceRangeRepository().getAllPriceRanges();
        allSiteLocationTypes = mRepositories.getSiteLocationTypeRepository().getAllSiteLocationTypes();
        allStarsQualityDescriptions = mRepositories.getStarsQualityDescriptionRepository().getAllStarsQualityDescriptions();
    }


    /** Methods to return one instance of the selected type from this repository's list of available values of selected type **/

    /**
     * Gets instance of CoffeeSiteType based on value of the typr
     * from list of all available this.allCoffeeSiteTypes.
     * If not in list return null
     * @param value
     * @return
     */
    public CoffeeSiteType getCoffeeSiteType(String value) {
        return mRepositories.getCoffeeSiteTypesRepository().getCoffeeSiteType(value).blockingSingle();
    }

    public CoffeeSiteRecordStatus getCoffeeSiteRecordStatus(String value) {
        return mRepositories.getCoffeeSiteRecordStatusRepository().getCoffeeSiteRecordStatus(value).blockingSingle();
    }

    public CoffeeSiteStatus getCoffeeSiteStatus(String value) {
        return mRepositories.getCoffeeSiteStatusRepository().getCoffeeSiteStatus(value).blockingSingle();
    }

    public CoffeeSort getCoffeeSort(String value) {
        return mRepositories.getCoffeeSortRepository().getCoffeeSort(value).blockingSingle();
    }

    public CupType getCupType(String value) {
        return mRepositories.getCupTypeRepository().getCupType(value).blockingSingle();
    }

    public NextToMachineType getNextToMachineType(String value) {
        return mRepositories.getNextToMachineTypeRepository().getNextToMachineType(value).blockingSingle();
    }

    public OtherOffer getOtherOffer(String value) {
        return mRepositories.getOtherOfferRepository().getOtherOffer(value).blockingSingle();
    }

    public PriceRange getPriceRange(String value) {
        return mRepositories.getPriceRangeRepository().getPriceRange(value).blockingSingle();
    }

    public SiteLocationType getSiteLocationType(String value) {
        return mRepositories.getSiteLocationTypeRepository().getSiteLocationType(value).blockingSingle();
    }


    /**
     * Gets List of CoffeeSiteEntity CoffeeSort instances based on it's values,
     * taken from reposiory list of available instances.
     * @param coffeeSortValues
     * @return
     */
    public List<CoffeeSort> createCoffeeSortsList(String[] coffeeSortValues) {
        List<CoffeeSort> retVal = new ArrayList<>();
        for (String coffeeSortValue : coffeeSortValues) {
            retVal.add(getCoffeeSort(coffeeSortValue));
        }
        return retVal;
    }

    /**
     * Gets List of CoffeeSiteEntity OtherOffer instances based on it's values,
     * taken from repository list of available instances.
     *
     * @param otherOfferValues
     * @return
     */
    public List<OtherOffer> createOtherOffersList(String[] otherOfferValues) {
        List<OtherOffer> retVal = new ArrayList<>();
        for (String coffeeSortValue : otherOfferValues) {
            retVal.add(getOtherOffer(coffeeSortValue));
        }
        return retVal;
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
//    private static <T extends CoffeeSiteEntity> T getEntityInstance(String value, List<T> entityInstances) {
//        if (entityInstances != null) {
//            for (T entity : entityInstances) {
//                if (entity.toString().equalsIgnoreCase(value)) {
//                    return entity;
//                }
//            }
//        }
//        return null;
//    }


    /** ------------- GETTERS ------------------------------ */

    public LiveData<List<CoffeeSiteStatus>> getAllCoffeeSiteStatuses() {
        return allCoffeeSiteStatuses;
    }

    public LiveData<List<CoffeeSiteType>> getAllCoffeeSiteTypes() {
        return allCoffeeSiteTypes;
    }

    public LiveData<List<CoffeeSiteRecordStatus>> getAllCoffeeSiteRecordStatuses() {
        return allCoffeeSiteRecordStatuses;
    }

    public LiveData<List<CoffeeSort>> getAllCoffeeSorts() {
        return allCoffeeSorts;
    }

    public LiveData<List<CupType>> getAllCupTypes() {
        return allCupTypes;
    }

    public LiveData<List<NextToMachineType>> getAllNextToMachineTypes() {
        return allNextToMachineTypes;
    }

    public LiveData<List<OtherOffer>> getAllOtherOffers() {
        return allOtherOffers;
    }

    public LiveData<List<PriceRange>> getAllPriceRanges() {
        return allPriceRanges;
    }

    public LiveData<List<SiteLocationType>> getAllSiteLocationTypes() {
        return allSiteLocationTypes;
    }

    public LiveData<List<StarsQualityDescription>> getAllStarsQualityDescriptions() {
        return allStarsQualityDescriptions;
    }

}
