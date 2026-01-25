package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountRESTInterface;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * REST user deleteUser request to be sent to server coffeecompass.cz
 */
public class UserDeleteRESTRequest {

    static final String REQ_TAG = "UserDeleteREST";

    private final UserAccountActionsProvider userAccountService;

    /**
     * Standard Constructor
     *
     * @param userDeleteService userAccount service to handle results of the deleteUser REST call
     */
    public UserDeleteRESTRequest(UserAccountActionsProvider userDeleteService) {
        super();
        this.userAccountService = userDeleteService;
    }


    public void performDeleteRequest() {

        Log.d(REQ_TAG, "UserDeleteRESTRequest started.");

        // Inserts user authorization token to Authorization header
        Interceptor headerAuthorizationInterceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request();
                Headers headers = request.headers().newBuilder().add("Authorization", userAccountService.getAccessTokenType() + " " + userAccountService.getAccessToken()).build();
                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            }
        };

        // Add the interceptor to the client builder.
        OkHttpClient.Builder clientBuilder = Utils.getOkHttpClientBuilder();

        OkHttpClient client = clientBuilder.authenticator(new TokenAuthenticator(userAccountService))
                                              .addInterceptor(headerAuthorizationInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                                        .client(client)
                                        .baseUrl(UserAccountRESTInterface.DELETE_USER_URL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .build();

        UserAccountRESTInterface api = retrofit.create(UserAccountRESTInterface.class);

        Call<String> call = api.deleteUserById(userAccountService.getLoggedInUser().getUserId());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, response.body());
                        // overeni, ze v odpovedi se vratilo ID, ktere bylo pozadovano ke smazani
                        if (response.body().equals(String.valueOf(userAccountService.getLoggedInUser().getUserId()))) {
                            userAccountService.evaluateDeleteResult(new Result.Success<>(userAccountService.getLoggedInUser().getUserName()));
                        } else {
                            userAccountService.evaluateDeleteResult(new Result.Error(new IOException("Error deleteUser user. Response user ID doesn't equal to requested ID.")));
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for deleteUser user account request.");
                        userAccountService.evaluateDeleteResult(new Result.Error(new IOException("Error deleteUser user. Response empty.")));
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        userAccountService.evaluateDeleteResult(new Result.Error(Utils.getRestError(errorBody)));
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error deleting user." + e.getMessage());
                        userAccountService.evaluateDeleteResult(new Result.Error(new IOException("Error deleting user.", e)));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(REQ_TAG, "Error executing deleteUser user account REST request." + t.getMessage());

                userAccountService.evaluateDeleteResult(new Result.Error(new IOException("Error deleting user.", t)));
                if (t.getMessage().startsWith("Refreshing access token failed")) {
                    userAccountService.clearLoggedInUser();
                    // go to login activity
                    Utils.openLoginActivityOnRefreshTokenFailed((Context) userAccountService);
                }
            }
        });
    }

}