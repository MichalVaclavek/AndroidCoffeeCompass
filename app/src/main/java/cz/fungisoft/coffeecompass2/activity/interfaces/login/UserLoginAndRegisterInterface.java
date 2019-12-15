package cz.fungisoft.coffeecompass2.activity.interfaces.login;

import cz.fungisoft.coffeecompass2.activity.data.model.rest.JwtUserToken;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.UserLoginOrRegisterInputData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Interface for Retrofit framework to define REST endpoints
 * for user login and registration.
 */
public interface UserLoginAndRegisterInterface {

    String LOGIN_URL = "https://coffeecompass.cz/rest/public/user/";

    String CURRENT_USER_URL = "https://coffeecompass.cz/rest/secured/user/";

    String REGISTER_USER_URL = "https://coffeecompass.cz/rest/public/user/";

    @POST("login")
    Call<JwtUserToken> getUserLogin(@Body UserLoginOrRegisterInputData loginDataBody);

    @GET("current")
    Call<String> getCurrentUser();

    @POST("register")
    Call<JwtUserToken> registerNewUser(@Body UserLoginOrRegisterInputData loginDataBody);
}
