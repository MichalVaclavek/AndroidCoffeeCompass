package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
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
 * Async Task to run REST api request to obtain All CoffeeSites. Used for "OFF-LINE" mode
 * of operation.
 */
public class GetAllCoffeeSitesAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetAllSitesAsyncTask";

    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSitesRESTResultListener callingListenerService;

    private Result.Error error;


    public GetAllCoffeeSitesAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                      CoffeeSitesRESTResultListener callingService) {
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Log.i(TAG, "start");
        operationError = "";

        //Add the interceptor to the client builder.
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(360, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("dd. MM. yyyy HH:mm")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(CoffeeSiteRESTInterface.GET_COFFEE_SITE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CoffeeSiteRESTInterface api = retrofit.create(CoffeeSiteRESTInterface.class);

        Call<List<CoffeeSite>> call = api.getAllCoffeeSites();

        Log.i(TAG, "start call");

        call.enqueue(new Callback<List<CoffeeSite>>() {
            @Override
            public void onResponse(Call<List<CoffeeSite>> call, Response<List<CoffeeSite>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(TAG, "onSuccess()");
                        List<CoffeeSite> coffeeSites = response.body();
                        //operationResult = "OK";
                        Result.Success<List<CoffeeSite>> result = new Result.Success<>(coffeeSites);
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, result);
                        }
                    } else {
                        Log.i(TAG, "Returned empty response for loading All CoffeeSites REST request.");
                        error = new Result.Error(new IOException("Error loading all CoffeeSites. Response empty."));
                        operationError = error.toString();
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                        }
                    }
                } else {
                    try {
                        error = new Result.Error(Utils.getRestError(response.errorBody().string()));
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        operationError = "Chyba komunikace se serverem.";
                    }
                    Log.e(TAG, "response Not successful");
                    if (error == null) {
                        error = new Result.Error(operationError);
                    }
                    if (callingListenerService != null) {
                        callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoffeeSite>> call, Throwable t) {
                Log.e(TAG, "Error loading All  CoffeeSites REST request." + t.getMessage());
                error = new Result.Error(new IOException("Error loading ALL CoffeeSites REST request.", t));
                operationError = error.toString();
                if (callingListenerService != null) {
                    callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                }
            }
        });
        return null;
    }

}
