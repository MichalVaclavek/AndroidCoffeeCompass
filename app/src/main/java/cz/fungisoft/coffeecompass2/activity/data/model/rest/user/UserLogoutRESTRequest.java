package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsEvaluator;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountRESTInterface;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * REST user logout request to be sent to server coffeecompass.cz
 */
public class UserLogoutRESTRequest {

    static final String REQ_TAG = "UserLogoutREST";

    // Activity or Service implementing interface for evaluation of the
    // user logout REST call attempt response
    private UserAccountActionsEvaluator userAccountService;

    /**
     * User to be logged-out
     */
    final LoggedInUser currentUser;

    /**
     * Standard constructor.
     *
     * @param currentUser - user to be logged-out
     * @param userLogoutService - service to process logout attempt response. Usually UserAccountService responsible
     *                          for user's account actions.
     */
    public UserLogoutRESTRequest(LoggedInUser currentUser, UserAccountService userLogoutService) {
        super();
        this.userAccountService = userLogoutService;
        this.currentUser = currentUser;
    }

    /**
     * Main class to call Retrofit library methods to perform logout REST API call
     * and for passing the result of the call to {@link UserAccountService}
     */
    public void performLogoutRequest() {

        Log.d(REQ_TAG, "UserLogoutRESTRequest initiated");

        Interceptor headerAuthorizationInterceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request();
                Headers headers = request.headers().newBuilder().add("Authorization", currentUser.getLoginToken().getTokenType() + " " + currentUser.getLoginToken().getAccessToken()).build();
                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            }
        };

        //Add the interceptor to the client builder.
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(headerAuthorizationInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                                        .client(client)
                                        .baseUrl(UserAccountRESTInterface.LOGOUT_USER_URL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .build();

        UserAccountRESTInterface api = retrofit.create(UserAccountRESTInterface.class);

        Call<Boolean> call = api.logoutCurrentUserWithId(currentUser.getUserId());

        call.enqueue(new Callback<Boolean>() {

            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, response.body().toString());
                        // If true is an answer, then the Logout request was successful
                        if ("true".equals(response.body().toString())) {
                            userAccountService.evaluateLogoutResult(new Result.Success<>(currentUser.getUserName()));
                        } else {
                            userAccountService.evaluateLogoutResult(new Result.Error(new IOException("Error logout user. Response FALSE.")));
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                        userAccountService.evaluateLogoutResult(new Result.Error(new IOException("Error logout user. Response empty.")));
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        userAccountService.evaluateLogoutResult(new Result.Error(Utils.getRestError(errorBody)));
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error logout user." + e.getMessage());
                        userAccountService.evaluateLogoutResult(new Result.Error(new IOException("Error logout user.", e)));
                    }
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(REQ_TAG, "Error executing Logout user REST request." + t.getMessage());
                userAccountService.evaluateLogoutResult(new Result.Error(new IOException("Error logout user.", t)));
            }
        });
    }

}