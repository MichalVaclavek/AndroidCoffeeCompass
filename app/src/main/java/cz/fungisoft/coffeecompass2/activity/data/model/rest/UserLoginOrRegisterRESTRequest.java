package cz.fungisoft.coffeecompass2.activity.data.model.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.services.UserLoginAndRegisterService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserLoginAndRegisterInterface;

/**
 * REST user login or register request to be sent to server coffeecompass.cz
 * {@link JwtUserToken} is container for the answer to this request.
 */
public class UserLoginOrRegisterRESTRequest {

    static final int INTERNET_REQ = 23;
    static final String REQ_TAG = "UserLoginOrRegisterREST";

    private UserLoginOrRegisterInputData userLoginOrRegisterInputData;

    private UserLoginAndRegisterService userLoginAndRegisterService;

    private final LoggedInUser currentUser;

    private static int PERFORM_LOGIN = 1;
    private static int PERFORM_REGISTER = 2;

    /**
     *
     * @param deviceID
     * @param email
     * @param userName
     * @param password
     */
    public UserLoginOrRegisterRESTRequest(String deviceID, String email, String userName, String password, UserLoginAndRegisterService userLoginAndRegisterService) {
        super();
        this.userLoginAndRegisterService = userLoginAndRegisterService;
        userLoginOrRegisterInputData = new UserLoginOrRegisterInputData(userName, deviceID, email, password);
        currentUser = new LoggedInUser();
    }


    public void performLoginRequest() {
        performRequest(PERFORM_LOGIN);
    }

    public void performRegisterRequest() {
        performRequest(PERFORM_REGISTER);
    }

    private void performRequest(final int requestType) {

        Log.d(REQ_TAG, "UserLoginOrRegisterRESTRequest initiated");

        Gson gson = new GsonBuilder()
                .setDateFormat("dd.MM.yyyy HH:mm")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl((requestType == PERFORM_LOGIN) ? UserLoginAndRegisterInterface.LOGIN_URL : UserLoginAndRegisterInterface.REGISTER_USER_URL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build();

        UserLoginAndRegisterInterface api = retrofit.create(UserLoginAndRegisterInterface.class);

        Call<JwtUserToken> call;
        if ((requestType == PERFORM_LOGIN)) {
            call = api.getUserLogin(userLoginOrRegisterInputData);
        } else {
            call = api.registerNewUser(userLoginOrRegisterInputData);
        }

        call.enqueue(new Callback<JwtUserToken>() {
            @Override
            public void onResponse(Call<JwtUserToken> call, Response<JwtUserToken> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("onSuccess", response.body().toString());

                        CurrentUserRESTRequest currentUserRESTRequest = new CurrentUserRESTRequest(response.body(), userLoginAndRegisterService);
                        currentUserRESTRequest.performRequest();
                        return;
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                        if (requestType == PERFORM_LOGIN) {
                            userLoginAndRegisterService.evaluateLoginResult(new Result.Error(new IOException("Error logging user. Response empty.")));
                        } else {
                            userLoginAndRegisterService.evaluateRegisterResult(new Result.Error(new IOException("Error registering user. Response failed.  Response empty.")));
                        }
                    }
                } else {
                    if (requestType == PERFORM_LOGIN) {
                        userLoginAndRegisterService.evaluateLoginResult(new Result.Error(new IOException("Error logging user. Response failed. Response: " + response.errorBody())));
                    } else {
                        userLoginAndRegisterService.evaluateRegisterResult(new Result.Error(new IOException("Error registering user. Response failed. Response: " + response.errorBody())));
                    }
                }
            }

            @Override
            public void onFailure(Call<JwtUserToken> call, Throwable t) {
                Log.e(REQ_TAG, "Error executing Login user REST request." + t.getMessage());
                userLoginAndRegisterService.evaluateLoginResult(new Result.Error(new IOException("Error logging user.", t)));
            }
        });
    }

}



