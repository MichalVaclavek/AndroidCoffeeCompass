package cz.fungisoft.coffeecompass2.activity.data.model.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;

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
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Class to create, run and process REST request for obtaining current logged-in user profile
 * from server.
 */
public class CurrentUserRESTRequest {

    // TODO vlozit do strings resources String url = getResources().getString(R.string.json_get_url);
    static final String REQ_TAG = "CurrentUserRESTRequest";

    private static int PERFORM_LOGIN = 1;
    private static int PERFORM_REGISTER = 2;

    final JwtUserToken userJwtToken;

    private final LoggedInUser currentUser;

    private final UserAccountService userLoginAndRegisterService;

    public CurrentUserRESTRequest(JwtUserToken userLoginRESTResponse, UserAccountService userLoginAndRegisterService) {
        super();
        this.userJwtToken = userLoginRESTResponse;
        currentUser = new LoggedInUser(userJwtToken);
        this.userLoginAndRegisterService = userLoginAndRegisterService;
    }

    public void performRequestAfterLogin() {
        performRequest(PERFORM_LOGIN);
    }

    public void performRequestAfterRegister() {
        performRequest(PERFORM_REGISTER);
    }

    private void performRequest(final int requestType) {

        Log.d(REQ_TAG, "CurrentUserRESTRequest initiated");

        Gson gson = new GsonBuilder()
                .setDateFormat("dd.MM. yyyy HH:mm")
                .create();

        Interceptor headerAuthorizationInterceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request();
                Headers headers = request.headers().newBuilder().add("Authorization", userJwtToken.getTokenType() + " " + userJwtToken.getAccessToken()).build();
                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            }
        };

        //Add the interceptor to the client builder.
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(headerAuthorizationInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                                        .client(client)
                                        .baseUrl(UserAccountRESTInterface.CURRENT_USER_URL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build();

        UserAccountRESTInterface api = retrofit.create(UserAccountRESTInterface.class);

        Call<String> call = api.getCurrentUser();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        Log.i("onSuccess", jsonResponse);
                        try {
                            currentUser.setupUserDataFromJson(jsonResponse);
                            if (requestType == PERFORM_LOGIN) {
                                userLoginAndRegisterService.evaluateLoginResult(new Result.Success<>(currentUser));
                            } else {
                                userLoginAndRegisterService.evaluateRegisterResult((new Result.Success<>(currentUser)));
                            }
                        } catch (JSONException e) {
                            if (requestType == PERFORM_LOGIN) {
                                userLoginAndRegisterService.evaluateLoginResult(new Result.Error(new IOException("Error JSON parsing current user data.", e)));
                            } else {
                                userLoginAndRegisterService.evaluateRegisterResult(new Result.Error(new IOException("Error JSON parsing current user data.", e)));
                            }
                        }
                        return;
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(REQ_TAG, "Current user response failure.");
                    if (requestType == PERFORM_LOGIN) {
                        userLoginAndRegisterService.evaluateLoginResult(new Result.Error(Utils.getRestError(response.errorBody().toString())));
                    } else {
                        userLoginAndRegisterService.evaluateRegisterResult(new Result.Error(Utils.getRestError(response.errorBody().toString())));
                    }
                }
                // Answer to login not correct
                //userLoginAndRegisterService.evaluateLoginResult(new Result.Error(Utils.getRestError(response.errorBody().toString())));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(REQ_TAG, "Error waiting for CurrentUserRESTAsyncTask" + t.getMessage());
                if (requestType == PERFORM_LOGIN) {
                    userLoginAndRegisterService.evaluateLoginResult(new Result.Error(new IOException("Error reading current user.", t)));
                } else {
                    userLoginAndRegisterService.evaluateRegisterResult(new Result.Error(new IOException("Error reading current user.", t)));
                }
            }
        });
    }

}
