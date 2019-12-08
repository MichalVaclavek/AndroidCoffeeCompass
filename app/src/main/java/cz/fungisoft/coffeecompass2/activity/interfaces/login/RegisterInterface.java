package cz.fungisoft.coffeecompass2.activity.interfaces.login;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RegisterInterface {

    String REGIURL = "https://coffeecompass.cz/rest/public/user/register";
    @FormUrlEncoded
    @POST("simpleregister.php")
    Call<String> getUserRegi(
            @Field("name") String name,
            @Field("hobby") String hobby,
            @Field("username") String uname,
            @Field("password") String password
    );
}
