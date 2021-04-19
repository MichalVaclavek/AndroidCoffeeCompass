package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntityRepositories;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Data Model to be connected to Activities, which needs CoffeeSiteEntity data available.
 */
public class CoffeeSiteEntitiesViewModel extends AndroidViewModel {

    private static final String TAG = "CSEntitiesViewModel";

    private final CoffeeSiteDatabase db;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    /**
     * Common repository
     */
    private final CoffeeSiteEntityRepositories mRepositories;


    /* ===== Coffee site types ===== */

    private final LiveData<List<CoffeeSiteType>> allCoffeeSiteTypes;

    private final Single<List<CoffeeSiteType>> allCoffeeSiteTypesSingle;

    public Single<List<CoffeeSiteType>> getCoffeeSiteTypesSingle() {
        return allCoffeeSiteTypesSingle;
    }

    private static List<CoffeeSiteType> mCoffeeSiteTypes;

    public List<CoffeeSiteType> getCoffeeSiteTypes() {
        return mCoffeeSiteTypes;
    }

    /* ===== Coffee site types ===== */

    /* ===== Coffee site record statuses ===== */

    private final LiveData<List<CoffeeSiteRecordStatus>> allCoffeeSiteRecordStatuses;



    /* ===== Coffee site record statuses ===== */

    /* ===== Coffee site statuses ===== */

    private final LiveData<List<CoffeeSiteStatus>> allCoffeeSiteStatuses;

    private final Single<List<CoffeeSiteStatus>> allCoffeeSiteStatusesSingle;

    public Single<List<CoffeeSiteStatus>> getCoffeeSiteStatusesSingle() {
        return allCoffeeSiteStatusesSingle;
    }

    private static List<CoffeeSiteStatus> mCoffeeSiteStatuses;

    /* ===== Coffee site statuses ===== */

    /* ===== Cup types ===== */

    private final LiveData<List<CupType>> allCupTypes;

    /* ===== Cup types ===== */

    /* ===== Next-to-machine types ===== */

    private final LiveData<List<NextToMachineType>> allNextToMachineTypes;

    /* ===== Next-to-machine types ===== */

    /* ===== Price ranges ===== */

    private final LiveData<List<PriceRange>> allPriceRanges;

    private final Single<List<PriceRange>> allPriceRangesSingle;

    public Single<List<PriceRange>> getAllPriceRangesSingle() {
        return allPriceRangesSingle;
    }

    private static List<PriceRange> mPriceRanges;

    public List<PriceRange> getPriceRanges() {
        return mPriceRanges;
    }

    /* ===== Price ranges ===== */

    /* ===== Site location types ===== */

    private final LiveData<List<SiteLocationType>> allSiteLocationTypes;

    private final Single<List<SiteLocationType>> allSiteLocationTypesSingle;

    public Single<List<SiteLocationType>> getAllSiteLocationTypesSingle() {
        return allSiteLocationTypesSingle;
    }

    private static List<SiteLocationType> mSiteLocationTypes;

    public List<SiteLocationType> getSiteLocationTypes() {
        return mSiteLocationTypes;
    }

    /* ===== Site location types ===== */

    /* ===== Stars quality descriptions ===== */

    private final LiveData<List<StarsQualityDescription>> allStarsQualityDescriptions;

    private final Single<List<StarsQualityDescription>> allStarsQualityDescriptionsSingle;

    public Single<List<StarsQualityDescription>> getAllStarsQualityDescriptionsSingle() {
        return allStarsQualityDescriptionsSingle;
    }

    private static List<StarsQualityDescription> mStarsQualityDescriptions;

    public List<StarsQualityDescription> getStarsQualityDescriptions() {
        return mStarsQualityDescriptions;
    }

    /* ===== Stars quality descriptions ===== */

    /* ===== Coffee sorts ===== */

    private final LiveData<List<CoffeeSort>> allCoffeeSorts;

    private final Single<List<CoffeeSort>> allCoffeeSortsSingle;

    public Single<List<CoffeeSort>> getAllCoffeeSortsSingle() {
        return allCoffeeSortsSingle;
    }

    private static List<CoffeeSort> mCoffeeSorts;

    public List<CoffeeSort> getCoffeeSorts() {
        return mCoffeeSorts;
    }

    /* ===== Coffee sorts ===== */

    /* ===== Other offers ===== */

    private final LiveData<List<OtherOffer>> allOtherOffers;

    private final Single<List<OtherOffer>> allOtherOffersSingle;

    public Single<List<OtherOffer>> getAllOtherOffersSingle() {
        return allOtherOffersSingle;
    }

    private static List<OtherOffer> mOtherOffers;

    public List<OtherOffer> getOtherOffers() {
        return mOtherOffers;
    }

    /* ===== Other offers ===== */

    /**
     * Standard constructor
     *
     * @param application
     */
    public CoffeeSiteEntitiesViewModel(@NonNull Application application) {
        super(application);

        db = CoffeeSiteDatabase.getDatabase(application.getApplicationContext());
        mRepositories = CoffeeSiteEntityRepositories.getInstance(db, application);

        allCoffeeSiteRecordStatuses = mRepositories.getCoffeeSiteRecordStatusRepository().getAllCoffeeSiteRecordStatuses();

        allCoffeeSiteStatuses = mRepositories.getCoffeeSiteStatusRepository().getAllCoffeeSiteStatuses();

        allCoffeeSiteStatusesSingle = mRepositories.getCoffeeSiteStatusRepository().getAllCoffeeSiteStatusesSingle();
        mDisposable.add(allCoffeeSiteStatusesSingle.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe((Consumer<List<CoffeeSiteStatus>>) coffeeSiteStatuses -> mCoffeeSiteStatuses = coffeeSiteStatuses));

        allCoffeeSiteTypes = mRepositories.getCoffeeSiteTypesRepository().getAllCoffeeSiteTypes();
        allCoffeeSiteTypesSingle = mRepositories.getCoffeeSiteTypesRepository().getAllCoffeeSiteTypesSingle();
        mDisposable.add(allCoffeeSiteTypesSingle.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe((Consumer<List<CoffeeSiteType>>) coffeeSiteTypes -> mCoffeeSiteTypes = coffeeSiteTypes));

        allCoffeeSorts = mRepositories.getCoffeeSortRepository().getAllCoffeeSorts();
        allCoffeeSortsSingle = mRepositories.getCoffeeSortRepository().getAllCoffeeSortsSingle();
        mDisposable.add(allCoffeeSortsSingle.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe((Consumer<List<CoffeeSort>>) coffeeSorts -> mCoffeeSorts = coffeeSorts));

        allOtherOffers = mRepositories.getOtherOfferRepository().getAllOtherOffers();
        allOtherOffersSingle = mRepositories.getOtherOfferRepository().geAlltOtherOffersSingle();
        mDisposable.add(allOtherOffersSingle.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe((Consumer<List<OtherOffer>>) otherOffers -> mOtherOffers = otherOffers));

        allPriceRanges = mRepositories.getPriceRangeRepository().getAllPriceRanges();
        allPriceRangesSingle = mRepositories.getPriceRangeRepository().getAllPriceRangesSingle();
        mDisposable.add(allPriceRangesSingle.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe((Consumer<List<PriceRange>>) priceRanges -> mPriceRanges = priceRanges));

        allSiteLocationTypes = mRepositories.getSiteLocationTypeRepository().getAllSiteLocationTypes();
        allSiteLocationTypesSingle = mRepositories.getSiteLocationTypeRepository().getAllSiteLocationTypesSingle();
        mDisposable.add(allSiteLocationTypesSingle.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe((Consumer<List<SiteLocationType>>) siteLocationTypes -> mSiteLocationTypes = siteLocationTypes));

        allStarsQualityDescriptions = mRepositories.getStarsQualityDescriptionRepository().getAllStarsQualityDescriptions();
        allStarsQualityDescriptionsSingle = mRepositories.getStarsQualityDescriptionRepository().getAllCoffeeSiteTypesSingle();
        mDisposable.add(allStarsQualityDescriptionsSingle.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe((Consumer<List<StarsQualityDescription>>) starsQualityDescriptions -> mStarsQualityDescriptions = starsQualityDescriptions));

        allCupTypes = mRepositories.getCupTypeRepository().getAllCupTypes();
        allNextToMachineTypes = mRepositories.getNextToMachineTypeRepository().getAllNextToMachineTypes();
    }


    /** Methods to return one instance of the selected type from this repository's list of available values of selected type **/

    /**
     * Gets {@code Single<>} instance of CoffeeSiteType based on value of the type
     * from list of all available this.allCoffeeSiteTypes.
     *
     * @param value
     * @return
     */
    public Single<CoffeeSiteType> getCoffeeSiteTypeSingle(String value) {
        return mRepositories.getCoffeeSiteTypesRepository().getCoffeeSiteType(value);
    }

    public CoffeeSiteType getCoffeeSiteType(String value) {

        CoffeeSiteType retVal = mCoffeeSiteTypes != null ? mCoffeeSiteTypes.get(0)
                                                         : new CoffeeSiteType();
        for (CoffeeSiteType cst : mCoffeeSiteTypes) {
            if (cst.getCoffeeSiteType().equals(value)) {
                retVal = cst;
                break;
            }
        }
        return retVal;
    }

    public Single<CoffeeSiteRecordStatus> getCoffeeSiteRecordStatus(String value) {
        return mRepositories.getCoffeeSiteRecordStatusRepository().getCoffeeSiteRecordStatus(value);
    }

    public Single<CoffeeSiteStatus> getCoffeeSiteStatusSingle(String value) {
        return mRepositories.getCoffeeSiteStatusRepository().getCoffeeSiteStatus(value);
    }

    public CoffeeSiteStatus getCoffeeSiteStatus(String value) {
        CoffeeSiteStatus retVal = mCoffeeSiteStatuses != null ? mCoffeeSiteStatuses.get(0)
                                                              : new CoffeeSiteStatus();
        for (CoffeeSiteStatus coffeeSiteStatus : mCoffeeSiteStatuses) {
            if (coffeeSiteStatus.getStatus().equals(value)) {
                retVal = coffeeSiteStatus;
                break;
            }
        }
        return retVal;
    }

    public Single<CoffeeSort> getCoffeeSort(String value) {
        return mRepositories.getCoffeeSortRepository().getCoffeeSort(value);
    }

    public Single<CupType> getCupType(String value) {
        return mRepositories.getCupTypeRepository().getCupType(value);
    }

    public Single<NextToMachineType> getNextToMachineType(String value) {
        return mRepositories.getNextToMachineTypeRepository().getNextToMachineType(value);
    }

    public Single<OtherOffer> getOtherOffer(String value) {
        return mRepositories.getOtherOfferRepository().getOtherOffer(value);
    }

    public Single<PriceRange> getPriceRangeSingle(String value) {
        return mRepositories.getPriceRangeRepository().getPriceRange(value);
    }

    public PriceRange getPriceRange(String value) {
        PriceRange retVal = mPriceRanges != null ? mPriceRanges.get(0)
                                                 : new PriceRange();
        for (PriceRange priceRange : mPriceRanges) {
            if (priceRange.getPriceRange().equals(value)) {
                retVal = priceRange;
                break;
            }
        }
        return retVal;
    }

    public Single<SiteLocationType> getSiteLocationTypeSingle(String value) {
        return mRepositories.getSiteLocationTypeRepository().getSiteLocationType(value);
    }

    public SiteLocationType getSiteLocationType(String value) {
        SiteLocationType retVal = mSiteLocationTypes != null ? mSiteLocationTypes.get(0)
                                                             : new SiteLocationType();
        for (SiteLocationType siteLocationType : mSiteLocationTypes) {
            if (siteLocationType.getLocationType().equals(value)) {
                retVal = siteLocationType;
                break;
            }
        }
        return retVal;
    }

    /**
     * Gets List of CoffeeSiteEntity CoffeeSort instances based on it's values,
     * taken from reposiory list of available instances.
     *
     * @param coffeeSortValues
     * @return
     */
    public List<CoffeeSort> createCoffeeSortsList(String[] coffeeSortValues) {
        List<CoffeeSort> retVal = new ArrayList<>();
        for (String coffeeSortValue : coffeeSortValues) {
            for (CoffeeSort coffeeSort : getCoffeeSorts()) {
                if (coffeeSort.getCoffeeSort().equalsIgnoreCase(coffeeSortValue)) {
                    retVal.add(coffeeSort);
                }
            }
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
        for (String otherOfferValue : otherOfferValues) {
            for (OtherOffer otherOffer : getOtherOffers()) {
                if (otherOffer.getOffer().equalsIgnoreCase(otherOfferValue)) {
                    retVal.add(otherOffer);
                }
            }
        }
        return retVal;
    }


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

    @Override
    public void onCleared() {
        super.onCleared();
        // clear all the Single observable subscriptions
        mDisposable.clear();
    }

}
