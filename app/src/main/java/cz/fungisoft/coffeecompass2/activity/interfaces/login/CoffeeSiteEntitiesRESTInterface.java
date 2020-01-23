package cz.fungisoft.coffeecompass2.activity.interfaces.login;

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

public interface CoffeeSiteEntitiesRESTInterface {

    String GET_ENTITY_BASE = "https://coffeecompass.cz/rest/site/";

    /**
     * REST call for obtain OtherOffer enetities
     * URL example https://coffeecompass.cz/rest/site/allOtherOffers
     * @return
     */
    @GET("allOtherOffers")
    Call<OtherOffer> getAllOtherOffers();

    @GET("allSiteStatuses")
    Call<CoffeeSiteStatus> getAllCoffeeSiteSiteStatuses();

    @GET("allSiteRecordStatuses")
    Call<CoffeeSiteRecordStatus> getAllCoffeeSiteRecordStatuses();

    @GET("allHodnoceniKavyStars")
    Call<StarsQualityDescription> getAllStarsQualityDescriptions();

    @GET("allPriceRanges")
    Call<PriceRange> getAllPriceRanges();

    @GET("allLocationTypes")
    Call<SiteLocationType> getAllSiteLocationTypes();

    @GET("allCupTypes")
    Call<CupType> getAllCupTypes();

    @GET("allCoffeeSorts")
    Call<CoffeeSort> getAllCoffeeSorts();

    @GET("allNextToMachineTypes")
    Call<NextToMachineType> getAllNextToMachineTypes();

    @GET("allCoffeeSiteTypes")
    Call<CoffeeSiteType> getAllCoffeeSiteTypes();

}
