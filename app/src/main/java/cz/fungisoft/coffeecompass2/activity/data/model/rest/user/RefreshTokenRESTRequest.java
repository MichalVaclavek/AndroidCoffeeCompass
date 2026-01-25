package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountRESTInterface;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * REST request to refresh Access token using refresh token.
 */
public class RefreshTokenRESTRequest {

    static final String REQ_TAG = "RefreshTokenREST";

    /**
     * Refresh token with deviceId as request input
     */
    private final RefreshTokenRequestData refreshTokenRequestData;

    /**
     *
     * @param deviceID - identification of device from which the user is trying to login or register
     * @param refreshToken
     */
    public RefreshTokenRESTRequest(String deviceID, String refreshToken) {
        super();
        this.refreshTokenRequestData = new RefreshTokenRequestData(refreshToken, deviceID);
    }

    /**
     * Main method to perform refresh Token request synchronously
     *
     * @param requestType
     * @return
     */
    public JwtUserToken performRequest() throws IOException {

        Log.d(REQ_TAG, "RefreshTokenRESTRequest initiated");

        Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy HH:mm")
                                     .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = Utils.getOkHttpClientBuilder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl(UserAccountRESTInterface.REFRESH_TOKEN_URL)
                                        .client(client)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build();

        UserAccountRESTInterface api = retrofit.create(UserAccountRESTInterface.class);

        Call<JwtUserToken> call = api.refreshToken(refreshTokenRequestData);

        return call.execute().body();
    }
}