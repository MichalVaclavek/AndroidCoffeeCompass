package cz.fungisoft.coffeecompass2.activity.interfaces.login;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CoffeeSiteRESTInterface {

    String GET_NUMBER_OF_STARS_URL = "https://coffeecompass.cz/rest/site/stars/";

    /**
     * REST call for obtaining number of Stars, which were given by the User to the CoffeeSite.
     * URL example https://coffeecompass.cz/rest/site/stars/?siteID=2&userID=5
     * @param siteID
     * @return
     */
    @GET("number/")
    Call<Integer> getNumberOfStars(@Query("siteID") int siteID, @Query("userID") int userID);

}
