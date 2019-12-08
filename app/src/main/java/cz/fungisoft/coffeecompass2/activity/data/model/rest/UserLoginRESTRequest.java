package cz.fungisoft.coffeecompass2.activity.data.model.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cz.fungisoft.coffeecompass2.activity.ui.login.LoginViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.concurrent.CountDownLatch;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.LoginInterface;

/**
 * REST user login or register request to be sent to server coffeecompass.cz
 * {@link JwtUserToken} is container for the answer to this request.
 */
public class UserLoginRESTRequest {

    // TODO vlozit do strings resources String url = getResources().getString(R.string.json_get_url);
    static final int INTERNET_REQ = 23;
    static final String REQ_TAG = "UserLoginRESTRequest";

    private UserLoginInputData userLoginInputData;

    private LoginViewModel loginViewModel;

    /**
     *
     * @param deviceID
     * @param email
     * @param userName
     * @param password
     */
    public UserLoginRESTRequest(String deviceID, String email, String userName, String password, LoginViewModel loginViewModel) {

        super();
        this.loginViewModel = loginViewModel;
        userLoginInputData = new UserLoginInputData(userName, deviceID,email, password);
    }

    public void performRequest() {

        Log.d(REQ_TAG, "UserLoginRESTRequest initiated");

        Gson gson = new GsonBuilder()
                .setDateFormat("dd.MM.yyyy HH:mm")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl(LoginInterface.LOGINURL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build();

        LoginInterface api = retrofit.create(LoginInterface.class);

        Call<JwtUserToken> call = api.getUserLogin(userLoginInputData);

        call.enqueue(new Callback<JwtUserToken>() {
            @Override
            public void onResponse(Call<JwtUserToken> call, Response<JwtUserToken> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("onSuccess", response.body().toString());

                        CurrentUserRESTRequest currentUserRESTRequest = new CurrentUserRESTRequest(response.body(), loginViewModel);
                        currentUserRESTRequest.performRequest();
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JwtUserToken> call, Throwable t) {
                Log.e(REQ_TAG, "Error executing Login user REST request." + t.getMessage());
            }
        });

        Log.d(REQ_TAG, "Thread t Ends");
    }

}



