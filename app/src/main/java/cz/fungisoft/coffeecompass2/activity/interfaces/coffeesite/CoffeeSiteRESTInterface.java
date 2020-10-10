package cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite;

import java.util.List;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit calls interface for CoffeeSite operations
 */
public interface CoffeeSiteRESTInterface {

    String COFFEESITE_API_PUBLIC_SEARCH_URL = BuildConfig.COFFEESITE_API_PUBLIC_SEARCH_URL;

    String GET_NUMBER_OF_STARS_URL = BuildConfig.STARS_API_PUBLIC_URL;

    String GET_COFFEE_SITE_URL = BuildConfig.COFFEESITE_API_PUBLIC_URL;

    String COFFEE_SITE_SECURED_URL = BuildConfig.COFFEESITE_API_SECURED_URL;

    /**
     * REST call for obtaining number of Stars, which were given by the User to the CoffeeSite.
     * URL example https://coffeecompass.cz/rest/site/stars/?siteID=2&userID=5
     * @param siteID
     * @return
     */
    @GET("number/")
    Call<Integer> getNumberOfStars(@Query("siteID") int siteID, @Query("userID") long userID);

    /**
     * REST call for obtaining one CoffeeSite by it's ID
     * URL example https://coffeecompass.cz/rest/site/5
     * @param siteId
     * @return
     */
    @GET("{siteId}")
    Call<CoffeeSite> getCoffeeSiteById(@Path("siteId") long siteId);

    /**
     * sURL = sURLCore + "?lat1=" + latFrom + "&lon1=" + longFrom + "&range=" + this.searchRange + "&sort=" + this.searchCoffeeSort;
     *
     * @return
     */
    @GET("searchSites/")
    Call<List<CoffeeSite>> getCoffeeSitesInRange(@Query("lat1") double lat1, @Query("lon1") double lon1, @Query("range") int range, @Query("sort") String sort);

    /**
     * REST call for obtaining all CoffeeSites created by userId
     *
     * @param userId
     * @return
     */
    @GET("site/byUser/{userId}")
    Call<List<CoffeeSite>> getAllCoffeeSitesByUser(@Path("userId") long userId);

    /**
     * REST call for obtaining all CoffeeSites created by current user
     * https://coffeecompass.cz/rest/site/mySites
     *
     * @return
     */
    @GET("mySites")
    Call<List<CoffeeSite>> getAllCoffeeSitesByCurrentUser();

    /**
     * REST call for obtaining all CoffeeSites created by current user
     * https://coffeecompass.cz/rest/site/mySites
     *
     * @return
     */
    @GET("mySitesPaginated/")
    Call<CoffeeSitePageEnvelope> getAllCoffeeSitesFromCurrentUserPaginated(@Query("page") int page, @Query("size") int size);

    /**
     * REST call for obtaining all CoffeeSites
     * https://coffeecompass.cz/rest/site/allSites/
     *
     * @return
     */
    @GET("allSites/")
    Call<List<CoffeeSite>> getAllCoffeeSites();

    /**
     * REST call for obtaining all CoffeeSites
     * https://coffeecompass.cz/rest/site/allSites/
     *
     * @return
     */
    @GET("allSitesPaginated/")
    Call<CoffeeSitePageEnvelope> getAllCoffeeSitesPaginated(@Query("page") int page, @Query("size") int size);

    /**
     * REST call for obtaining number of CoffeeSites created by current User
     * https://coffeecompass.cz/rest/site/mySitesNumber
     * @return
     */
    @GET("mySitesNotCanceledNumber")
    Call<Integer> getNumberOfSitesNotCanceledFromCurrentUser();

    /**
     * REST call for obtaining number of CoffeeSites created by current User
     * https://coffeecompass.cz/rest/site/mySitesNumber
     * @return
     */
    @GET("mySitesNumber")
    Call<Integer> getNumberOfAllCoffeeSitesFromCurrentUser();

    /**
     * Calls saving of CoffeeSite instance. Expects CoffeeSite's new id
     * is returned.
     *
     * @param coffeeSite
     * @return
     */
    @POST("create")
    Call<CoffeeSite> createCoffeeSite(@Body CoffeeSite coffeeSite);

    /**
     * Calls update of CoffeeSite instance. Expects same siteId returned as positive response.
     *
     * @param coffeeSite
     * @param siteId
     * @return
     */
    @PUT("update/{siteId}")
    Call<CoffeeSite> updateCoffeeSite(@Path("siteId") int siteId, @Body CoffeeSite coffeeSite);

    /**
     * Calls update of CoffeeSite instance. Expects same siteId returned as positive response.
     *
     * @param siteId
     * @return
     */
    @DELETE("delete/{siteId}")
    Call<Integer> deleteCoffeeSite(@Path("siteId") int siteId);


    /** STATUS change operations **/

    @PUT("{siteId}/activate")
    Call<CoffeeSite> activateCoffeeSite(@Path("siteId") int siteId);

    @PUT("{siteId}/deactivate")
    Call<CoffeeSite> deactivateCoffeeSite(@Path("siteId") int siteId);

    @PUT("{siteId}/cancel")
    Call<CoffeeSite> cancelCoffeeSite(@Path("siteId") int siteId);

}
