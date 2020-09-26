package cz.fungisoft.coffeecompass2.activity.interfaces.login;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.JwtUserToken;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.UserLoginOrRegisterInputData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Interface for Retrofit framework to define REST endpoints
 * for user account login, registration, delete and for
 * obtaining logged-in user data.
 */
public interface UserAccountRESTInterface {

    // Definition of string constants accessible from interface are defined in build.gradle (app)
    String LOGIN_URL = BuildConfig.USER_API_PUBLIC_URL;
    String REGISTER_USER_URL = BuildConfig.USER_API_PUBLIC_URL;

    String CURRENT_USER_URL = BuildConfig.USER_API_SECURED_URL;
    String LOGOUT_USER_URL = BuildConfig.USER_API_SECURED_URL;
    String DELETE_USER_URL = BuildConfig.USER_API_SECURED_URL;


    @POST("login")
    Call<JwtUserToken> postUserLogin(@Body UserLoginOrRegisterInputData loginDataBody);

    /**
     * Return JSON string containing user's account data - must be parsed to LoggedInUser object
     * @return
     */
    @GET("current")
    Call<String> getCurrentUser();

    @GET("logout")
    Call<Boolean> logoutCurrentUser();

    @GET("logout/{userId}")
    Call<Boolean> logoutCurrentUserWithId(@Path("userId") Long userId);

    @DELETE("delete/{userName}")
    Call<String> deleteUser(@Path("userName") String userName);

    /**
     * Returns user ID of the deleted user or 0 if not successful
     *
     * @param userId
     * @return
     */
    @DELETE("delete/id/{userId}")
    Call<String> deleteUserById(@Path("userId") Long userId);

    @POST("register")
    Call<JwtUserToken> registerNewUser(@Body UserLoginOrRegisterInputData loginDataBody);

}
