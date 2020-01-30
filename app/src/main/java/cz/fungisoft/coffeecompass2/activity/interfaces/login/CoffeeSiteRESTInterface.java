package cz.fungisoft.coffeecompass2.activity.interfaces.login;

import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentAndStarsToSave;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.Comment;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CoffeeSiteRESTInterface {

    String GET_NUMBER_OF_STARS_URL = "https://coffeecompass.cz/rest/site/stars/";

    String GET_COFFEE_SITE_URL = "https://coffeecompass.cz/rest/site/";

    String COFFEE_SITE_SECURED_URL = "https://coffeecompass.cz/rest/secured/site/";

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
    @GET("site/{siteId}")
    Call<CoffeeSite> getCoffeeSiteById(@Path("siteId") long siteId);

    /**
     * REST call for obtaining all CoffeeSites created by userId
     * https://coffeecompass.cz/rest/site/5
     * @param userId
     * @return
     */
    @GET("site/{siteId}")
    Call<List<CoffeeSite>> getAllCoffeeSitesByUser(@Path("userId") long userId);

    /**
     * Calls saving of CoffeeSite instance. Expects CoffeeSite's new id
     * is returned.
     *
     * @param coffeeSite
     * @return
     */
    @POST("create")
    Call<Integer> createCoffeeSite(@Body CoffeeSite coffeeSite);

    /**
     * Calls update of CoffeeSite instance. Expects same siteId returned as positive response.
     *
     * @param coffeeSite
     * @param siteId
     * @return
     */
    @PUT("update/{siteId}")
    Call<Integer> updateCoffeeSite(@Path("siteId") int siteId, @Body CoffeeSite coffeeSite);

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
    Call<Boolean> activateCoffeeSite(@Path("siteId") int siteId);

    @PUT("{siteId}/deactivate")
    Call<Boolean> deactivateCoffeeSite(@Path("siteId") int siteId);

    @PUT("{siteId}/cancel")
    Call<Boolean> cancelCoffeeSite(@Path("siteId") int siteId);


    /**
     * Deletes comment of commentID. Return commentID back or 0? if the delete request failed
     * Requires Authorization header.
     *
     * @param commentID
     * @return
     */
//    @DELETE("deleteComment/{commentID}")
//    Call<Integer> deleteComment(@Path("commentID") int commentID);

}
