package cz.fungisoft.coffeecompass2.activity.data.model.rest;

import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
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

    private UserAccountService userAccountService;

    final LoggedInUser currentUser;

    /**
     *
     */
    public UserLogoutRESTRequest(LoggedInUser currentUser, UserAccountService userLoginAndRegisterService) {
        super();
        this.userAccountService = userLoginAndRegisterService;
        this.currentUser = currentUser;
    }

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

        Call<Boolean> call = api.logoutCurrentUser();

        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("onSuccess", response.body().toString());
                        userAccountService.evaluateLogoutResult(new Result.Success<>(currentUser.getUserName()));
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                        userAccountService.evaluateLogoutResult(new Result.Error(new IOException("Error logout user. Response empty.")));
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        userAccountService.evaluateLogoutResult(new Result.Error(Utils.getRestError(errorBody)));
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error logout user." + e.getMessage());
                        userAccountService.evaluateLoginResult(new Result.Error(new IOException("Error logout user.", e)));
                    }
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e(REQ_TAG, "Error executing Login user REST request." + t.getMessage());
                userAccountService.evaluateLogoutResult(new Result.Error(new IOException("Error logout user.", t)));
            }
        });
    }

}



