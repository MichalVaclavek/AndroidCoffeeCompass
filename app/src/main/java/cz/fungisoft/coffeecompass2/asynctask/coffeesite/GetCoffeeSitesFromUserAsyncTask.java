package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;
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
 * Async Task to run REST api request to obtain all coffeeSites
 * created by User
 */
public class GetCoffeeSitesFromUserAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetSitesFromUserAsnTsk";

    private final LoggedInUser currentUser;

    private final CoffeeSiteService callingService;

    private String operationResult = "";
    private String operationError = "";

    public GetCoffeeSitesFromUserAsyncTask(LoggedInUser user, CoffeeSiteService callingService) {
        this.currentUser = user;
        this.callingService = callingService;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "start");
        operationResult = "";
        operationError = "";

        Log.i(TAG, "currentUSer is null? " + String.valueOf(currentUser == null));
        if (currentUser != null) {

            // Inserts currentUser authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", currentUser.getLoginToken().getTokenType() + " " + currentUser.getLoginToken().getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

            //HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            //logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            //Add the interceptor to the client builder.
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerAuthorizationInterceptor)
                    //.addInterceptor(logging)
                    .build();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("dd. MM. yyyy HH:mm")
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(CoffeeSiteRESTInterface.COFFEE_SITE_SECURED_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            CoffeeSiteRESTInterface api = retrofit.create(CoffeeSiteRESTInterface.class);

            Call<List<CoffeeSite>> call = api.getAllCoffeeSitesByUser(currentUser.getUserId());

            Log.i(TAG, "start call");

            call.enqueue(new Callback<List<CoffeeSite>>() {
                @Override
                public void onResponse(Call<List<CoffeeSite>> call, Response<List<CoffeeSite>> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            //Log.i("onSuccess", response.body());
                            List<CoffeeSite> coffeeSites = response.body();
                            operationResult = "OK";
                            callingService.sendCoffeeSitesFromUserLoadResultToClient(coffeeSites, operationResult, "");
                        } else {
                            Log.i(TAG, "Returned empty response for loading CoffeeSites from User REST request.");
                            Result.Error error = new Result.Error(new IOException("Error saving CoffeeSite. Response empty."));
                            operationError = error.toString();
                            callingService.sendCoffeeSitesFromUserLoadResultToClient(null,"", operationError);
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                            operationError = callingService.getResources().getString(R.string.coffeesiteservice_error_message_not_available);
                        }
                        callingService.sendCoffeeSitesFromUserLoadResultToClient(null,"", operationError);
                    }
                }

                @Override
                public void onFailure(Call<List<CoffeeSite>> call, Throwable t) {
                    Log.e(TAG, "Error loading CoffeeSites from User REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error loading CoffeeSites from User REST request.", t));
                    operationError = error.toString();
                    callingService.sendCoffeeSitesFromUserLoadResultToClient(null,"", operationError);
                }
            });
        }
        return null;
    }
}
