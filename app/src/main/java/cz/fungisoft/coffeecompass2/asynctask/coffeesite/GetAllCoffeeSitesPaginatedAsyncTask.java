package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
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
public class GetAllCoffeeSitesPaginatedAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetAllSitesPageAsnT";

    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSitesRESTResultListener callingListenerService;

    private Result.Error error;

    private final int requestedPage;

    private final int pageSize;


    public GetAllCoffeeSitesPaginatedAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                                           int requestedPage,
                                                           int pageSize,
                                                           CoffeeSitesRESTResultListener callingService) {
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
        this.requestedPage = requestedPage;
        this.pageSize = pageSize;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Log.i(TAG, "start");
        //operationResult = "";
        operationError = "";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
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

        Call<CoffeeSitePageEnvelope> call = api.getAllCoffeeSitesPaginated(requestedPage, pageSize);

        Log.i(TAG, "start call");

        call.enqueue(new Callback<CoffeeSitePageEnvelope>() {
            @Override
            public void onResponse(Call<CoffeeSitePageEnvelope> call, Response<CoffeeSitePageEnvelope> response) {
                if (response.isSuccessful()) {
                    int responseCode = response.code();
                    if (responseCode == 504) {return;}

                    if (response.body() != null) {
                        Log.i(TAG, "onSuccess()");
                        CoffeeSitePageEnvelope coffeeSites = response.body();
                        //operationResult = "OK";
                        Result.Success<CoffeeSitePageEnvelope> result = new Result.Success<>(coffeeSites);
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSitesPageReturned(requestedRESTOperationCode, result);
                        }
                    } else {
                        Log.i(TAG, "Returned empty response for loading CoffeeSites from user REST request.");
                        error = new Result.Error(new IOException("Error saving CoffeeSite. Response empty."));
                        operationError = error.toString();
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSitesPageReturned(requestedRESTOperationCode, error);
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
                        callingListenerService.onCoffeeSitesPageReturned(requestedRESTOperationCode, error);
                    }
                }
            }

            @Override
            public void onFailure(Call<CoffeeSitePageEnvelope> call, Throwable t) {
                Log.e(TAG, "Error loading CoffeeSites from User REST request." + t.getMessage());
                error = new Result.Error(new IOException("Error loading CoffeeSites from user REST request.", t));
                operationError = error.toString();
                if (callingListenerService != null) {
                    callingListenerService.onCoffeeSitesPageReturned(requestedRESTOperationCode, error);
                }
            }
        });
        return null;
    }

}
