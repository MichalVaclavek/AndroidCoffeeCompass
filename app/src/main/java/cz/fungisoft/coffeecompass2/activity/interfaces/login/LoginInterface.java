package cz.fungisoft.coffeecompass2.activity.interfaces.login;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.JwtUserToken;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.UserLoginInputData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LoginInterface {

    String LOGINURL = "https://coffeecompass.cz/rest/public/user/";

    String CURRENT_USER_URL = "https://coffeecompass.cz/rest/secured/user/";

    @POST("login")
    Call<JwtUserToken> getUserLogin(@Body UserLoginInputData loginDataBody);

    @GET("current")
    Call<String> getCurrentUser();
}
