package cz.fungisoft.coffeecompass2.activity.data.model.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.LoginInterface;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginViewModel;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class CurrentUserRESTRequest {

    // TODO vlozit do strings resources String url = getResources().getString(R.string.json_get_url);
    static final String REQ_TAG = "CurrentUserRESTRequest";

    final JwtUserToken userJwtToken;

    private final LoggedInUser currentUser;

    private LoginViewModel loginViewModel;

    public CurrentUserRESTRequest(JwtUserToken userLoginRESTResponse, LoginViewModel loginViewModel) {
        super();
        this.userJwtToken = userLoginRESTResponse;
        currentUser = new LoggedInUser(userJwtToken);
        this.loginViewModel = loginViewModel;
    }

    public void performRequest() {

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
                .baseUrl(LoginInterface.CURRENT_USER_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        LoginInterface api = retrofit.create(LoginInterface.class);

        Call<String> call = api.getCurrentUser();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        Log.i("onSuccess", jsonResponse);
                        parseCurrentUserResponse(jsonResponse);
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(REQ_TAG, "Error waiting for CurrentUserRESTAsyncTask");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(REQ_TAG, "Error waiting for CurrentUserRESTAsyncTask" + t.getMessage());
                loginViewModel.evaluateLoginResult(new Result.Error(new IOException("Error reading current user: " +  "", t)));
            }
        });
    }


    public void parseCurrentUserResponse(String response) {

        try {
            JSONObject jsonResponse = new JSONObject(response);
            currentUser.setUserId(jsonResponse.getString("id"));
            currentUser.setUserName(jsonResponse.getString("userName"));
            currentUser.setFirstName(jsonResponse.getString("firstName"));
            currentUser.setLastName(jsonResponse.getString("lastName"));
            currentUser.setEmail(jsonResponse.getString("email"));
            currentUser.setCreatedOn(jsonResponse.getString("createdOn"));
            currentUser.setNumOfCreatedSites(jsonResponse.getInt("createdSites"));
            currentUser.setNumOfDeletedSites(jsonResponse.getInt("deletedSites"));
            currentUser.setNumOfUpdatedSites(jsonResponse.getInt("updatedSites"));

            JSONArray userRoles = jsonResponse.getJSONArray("userProfiles");

            for (int i = 0; i < userRoles.length(); i++) {
                JSONObject role = userRoles.getJSONObject(i);
                currentUser.getUserRoles().add(role.getString("type"));
            }
            loginViewModel.evaluateLoginResult(new Result.Success<>(currentUser));

        } catch (JSONException e) {
            Log.e(REQ_TAG, "Exception during parsing ... t: " + e.getMessage());
            loginViewModel.evaluateLoginResult(new Result.Error(new IOException("Error reading current user: " +  "", e)));
        }
    }

}
