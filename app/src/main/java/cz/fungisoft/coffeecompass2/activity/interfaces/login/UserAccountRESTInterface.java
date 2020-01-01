package cz.fungisoft.coffeecompass2.activity.interfaces.login;

import android.content.res.Resources;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.JwtUserToken;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.UserLoginOrRegisterInputData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Interface for Retrofit framework to define REST endpoints
 * for user login and registration.
 */
public interface UserAccountRESTInterface {
    //Resources.getSystem().getString(R.strings.key);

    String LOGIN_URL = "https://coffeecompass.cz/rest/public/user/";

    String CURRENT_USER_URL = "https://coffeecompass.cz/rest/secured/user/";

    String LOGOUT_USER_URL = "https://coffeecompass.cz/rest/secured/user/";

    String DELETE_USER_URL = "https://coffeecompass.cz/rest/secured/user/";

    String REGISTER_USER_URL = "https://coffeecompass.cz/rest/public/user/";

    @POST("login")
    Call<JwtUserToken> postUserLogin(@Body UserLoginOrRegisterInputData loginDataBody);

    @GET("current")
    Call<String> getCurrentUser();

    @GET("logout")
    Call<Boolean> logoutCurrentUser();

    @DELETE("delete/{userName}")
    Call<String> deleteUser(@Path("userName") String userName);

    @POST("register")
    Call<JwtUserToken> registerNewUser(@Body UserLoginOrRegisterInputData loginDataBody);

}
