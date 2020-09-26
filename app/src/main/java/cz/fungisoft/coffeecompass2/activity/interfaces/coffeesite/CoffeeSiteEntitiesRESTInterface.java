package cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite;

import java.util.List;

import cz.fungisoft.coffeecompass2.BuildConfig;
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
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit library interface for operations/REST calls related to
 * CoffeeSiteEntities.
 */
public interface CoffeeSiteEntitiesRESTInterface {

    String GET_ENTITY_BASE = BuildConfig.COFFEESITE_API_PUBLIC_URL;

    /**
     * REST call for obtain OtherOffer entity JSON values. Parsing from JSON
     * to OtherOffer instancies is done by Retrofit lib.
     * URL example https://coffeecompass.cz/rest/site/allOtherOffers
     * @return
     */
    @GET("allOtherOffers")
    Call<List<OtherOffer>> getAllOtherOffers();

    @GET("allSiteStatuses")
    Call<List<CoffeeSiteStatus>> getAllCoffeeSiteSiteStatuses();

    @GET("allSiteRecordStatuses")
    Call<List<CoffeeSiteRecordStatus>> getAllCoffeeSiteRecordStatuses();

    @GET("allHodnoceniKavyStars")
    Call<List<StarsQualityDescription>> getAllStarsQualityDescriptions();

    @GET("allPriceRanges")
    Call<List<PriceRange>> getAllPriceRanges();

    @GET("allLocationTypes")
    Call<List<SiteLocationType>> getAllSiteLocationTypes();

    @GET("allCupTypes")
    Call<List<CupType>> getAllCupTypes();

    @GET("allCoffeeSorts")
    Call<List<CoffeeSort>> getAllCoffeeSorts();

    @GET("allNextToMachineTypes")
    Call<List<NextToMachineType>> getAllNextToMachineTypes();

    @GET("allCoffeeSiteTypes")
    Call<List<CoffeeSiteType>> getAllCoffeeSiteTypes();

}
