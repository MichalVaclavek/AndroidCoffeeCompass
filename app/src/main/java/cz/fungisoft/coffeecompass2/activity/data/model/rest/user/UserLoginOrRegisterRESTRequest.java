package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.login.UserAccountActionsEvaluator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.login.UserAccountRESTInterface;

/**
 * REST user login or register request to be sent to server coffeecompass.cz
 * The request consists of two subsequent REST requests. First is login or
 * register request, which is finished by receiving JWT token only, if successful.
 * {@link JwtUserToken} is container for the answer to the first request
 * and is then used for subsequent REST call obtaining user's account data.
 */
public class UserLoginOrRegisterRESTRequest {

    static final String REQ_TAG = "UserLoginOrRegisterREST";

    /**
     * User register or login REST input data structure expected by server, sent using JSON
     */
    private UserLoginOrRegisterInputData userLoginOrRegisterInputData;

    /**
     * Evaluator of the user register or login REST response
     */
    private UserAccountActionsEvaluator userLoginAndRegisterService;

    /**
     * User data obtained from server as a result of login or register process
     */
    private final LoggedInUser currentUser;

    private static final int PERFORM_LOGIN = 1;
    private static final int PERFORM_REGISTER = 2;

    /**
     *
     * @param deviceID - identification of device from which the user is trying to login or register
     * @param email - email of the user to be used for registration of a new user
     * @param userName - username to be used for registration or login of the user
     * @param password - password entered by user, used to register or login
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

    /**
     * Main method to perform new user Register or Login request.
     * Uses Retrofit library to initiate REST API request.
     * Calls evaluation methods of {@link #userLoginAndRegisterService} to
     * process responses from server.
     *
     * @param requestType
     */
    private void performRequest(final int requestType) {

        Log.d(REQ_TAG, "UserLoginOrRegisterRESTRequest initiated");

        Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy HH:mm")
                                     .create();

        Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl((requestType == PERFORM_LOGIN) ? UserAccountRESTInterface.LOGIN_URL
                                                                                : UserAccountRESTInterface.REGISTER_USER_URL)
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
                        Log.i(REQ_TAG, response.body().toString());
                        // If there is successful response, then it contains JWT token as the login/register process passed
                        // JWT token can be then used for subsequent REST request to obtain user's profile data
                        CurrentUserRESTRequest currentUserRESTRequest = new CurrentUserRESTRequest(response.body(), userLoginAndRegisterService);
                        if (requestType == PERFORM_LOGIN) {
                            currentUserRESTRequest.performRequestAfterLogin();
                        } else {
                            currentUserRESTRequest.performRequestAfterRegister();
                        }
                        return;
                    } else {
                        Log.i(REQ_TAG, "Returned empty response");
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