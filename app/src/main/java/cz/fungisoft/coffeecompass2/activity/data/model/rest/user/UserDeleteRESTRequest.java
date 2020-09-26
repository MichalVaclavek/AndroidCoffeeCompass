package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsEvaluator;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountRESTInterface;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * REST user delete request to be sent to server coffeecompass.cz
 */
public class UserDeleteRESTRequest {

    static final String REQ_TAG = "UserDeleteREST";

    private UserAccountActionsEvaluator userAccountService;

    /**
     * User account to be deleted
     */
    private final LoggedInUser user;

    /**
     * Standard Constructor
     *
     * @param user user account to be deleted
     * @param userDeleteService userAccount service to handle results of the delete REST call
     */
    public UserDeleteRESTRequest(LoggedInUser user, UserAccountActionsEvaluator userDeleteService) {
        super();
        this.userAccountService = userDeleteService;
        this.user = user;
    }


    public void performDeleteRequest() {

        Log.d(REQ_TAG, "UserDeleteRESTRequest started.");

        // Inserts user authorization token to Authorization header
        Interceptor headerAuthorizationInterceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request();
                Headers headers = request.headers().newBuilder().add("Authorization", user.getLoginToken().getTokenType() + " " + user.getLoginToken().getAccessToken()).build();
                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            }
        };

        //Add the interceptor to the client builder.
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(headerAuthorizationInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                                        .client(client)
                                        .baseUrl(UserAccountRESTInterface.DELETE_USER_URL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .build();

        UserAccountRESTInterface api = retrofit.create(UserAccountRESTInterface.class);

        Call<String> call = api.deleteUserById(user.getUserId());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, response.body());
                        // overeni, ze v odpovedi se vratilo ID, ktere bylo pozadovano ke smazani
                        if (response.body().equals(String.valueOf(user.getUserId()))) {
                            userAccountService.evaluateDeleteResult(new Result.Success<>(user.getUserName()));
                        } else {
                            userAccountService.evaluateDeleteResult(new Result.Error(new IOException("Error delete user. Response user ID doesn't equal to requested ID.")));
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for delete user account request.");
                        userAccountService.evaluateDeleteResult(new Result.Error(new IOException("Error delete user. Response empty.")));
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
                Log.e(REQ_TAG, "Error executing delete user account REST request." + t.getMessage());
                userAccountService.evaluateDeleteResult(new Result.Error(new IOException("Error deleting user.", t)));
            }
        });
    }

}