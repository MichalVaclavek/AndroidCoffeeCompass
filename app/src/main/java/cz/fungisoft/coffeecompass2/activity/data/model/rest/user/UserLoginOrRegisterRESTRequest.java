package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsEvaluator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountRESTInterface;

/**
 * REST user login or register request to be sent to server coffeecompass.cz
 * {@link JwtUserToken} is container for the answer to this request.
 */
public class UserLoginOrRegisterRESTRequest {

    static final String REQ_TAG = "UserLoginOrRegisterREST";

    private UserLoginOrRegisterInputData userLoginOrRegisterInputData;

    private UserAccountActionsEvaluator userLoginAndRegisterService;

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
    public UserLoginOrRegisterRESTRequest(String deviceID, String email, String userName, String password, UserAccountActionsEvaluator userLoginAndRegisterService) {
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

        Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy HH:mm")
                                     .create();

        Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl((requestType == PERFORM_LOGIN) ? UserAccountRESTInterface.LOGIN_URL : UserAccountRESTInterface.REGISTER_USER_URL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build();

        UserAccountRESTInterface api = retrofit.create(UserAccountRESTInterface.class);

        Call<JwtUserToken> call;
        if ((requestType == PERFORM_LOGIN)) {
            call = api.postUserLogin(userLoginOrRegisterInputData);
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
                        if (requestType == PERFORM_LOGIN) {
                            currentUserRESTRequest.performRequestAfterLogin();
                        } else {
                            currentUserRESTRequest.performRequestAfterRegister();
                        }
                        return;
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");
                        if (requestType == PERFORM_LOGIN) {
                            userLoginAndRegisterService.evaluateLoginResult(new Result.Error(new IOException("Error logging user. Response empty.")));
                        } else {
                            userLoginAndRegisterService.evaluateRegisterResult(new Result.Error(new IOException("Error registering user. Response failed. Response empty.")));
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (requestType == PERFORM_LOGIN) {
                            userLoginAndRegisterService.evaluateLoginResult(new Result.Error(Utils.getRestError(errorBody)));
                        } else {
                            userLoginAndRegisterService.evaluateRegisterResult(new Result.Error(Utils.getRestError(errorBody)));
                        }
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error reading error body." + e.getMessage());
                        if (requestType == PERFORM_LOGIN) {
                            userLoginAndRegisterService.evaluateLoginResult(new Result.Error(new IOException("Error logging user.", e)));
                        }
                        else {
                            userLoginAndRegisterService.evaluateRegisterResult(new Result.Error(new IOException("Error register user.", e)));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JwtUserToken> call, Throwable t) {
                Log.e(REQ_TAG, "Error executing Login user REST request." + t.getMessage());
                userLoginAndRegisterService.evaluateLoginResult(new Result.Error(new IOException("Error logging/register user.", t)));
            }
        });
    }

}



