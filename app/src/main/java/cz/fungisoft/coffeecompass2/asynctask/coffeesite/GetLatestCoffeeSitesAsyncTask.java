package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Async Task to run REST api request to load CoffeeSites activated within last X days.
 */
public class GetLatestCoffeeSitesAsyncTask {

    private static final String TAG = "GetALatestSitesAsyncT";

    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSitesRESTResultListener callingListenerService;

    private Result.Error error;

    private int numberOfDaysBack = 1;


    public GetLatestCoffeeSitesAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                         CoffeeSitesRESTResultListener callingService, int numberOfDaysBack) {
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
        this.numberOfDaysBack = numberOfDaysBack;
    }

    public void execute() {

        Log.i(TAG, "start");
        operationError = "";

        CoffeeSiteRESTInterface api = RetrofitClientProvider.getInstance()
                .getRetrofit(CoffeeSiteRESTInterface.GET_COFFEE_SITE_URL)
                .create(CoffeeSiteRESTInterface.class);

        Call<List<CoffeeSite>> call = api.getLatestCoffeeSites(this.numberOfDaysBack);

        Log.i(TAG, "start call");

        call.enqueue(new Callback<List<CoffeeSite>>() {
            @Override
            public void onResponse(Call<List<CoffeeSite>> call, Response<List<CoffeeSite>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(TAG, "onSuccess()");
                        List<CoffeeSite> coffeeSites = response.body();
                        Result.Success<List<CoffeeSite>> result = new Result.Success<>(coffeeSites);
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, result);
                        }
                    } else {
                        Log.i(TAG, "Returned empty response for loading latest CoffeeSites REST request.");
                        error = new Result.Error(new IOException("Error loading latest CoffeeSites. Response empty."));
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
                Log.e(TAG, "Error loading latest CoffeeSites REST request." + t.getMessage());
                error = new Result.Error(new IOException("Error loading latest CoffeeSites REST request.", t));
                operationError = error.toString();
                if (callingListenerService != null) {
                    callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                }
            }
        });
    }

}
