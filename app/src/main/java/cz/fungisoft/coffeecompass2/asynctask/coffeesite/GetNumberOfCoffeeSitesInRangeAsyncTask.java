package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesFoundFromServerResultListener;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Async Task to run REST api request to obtain number of ccoffeeSites within current distance range.
 */
public class GetNumberOfCoffeeSitesInRangeAsyncTask {

    private static final String TAG = "GetNumOfSitesInRangeAsT";

    /**
     * A Service, which invokes this async. task
     */
    private final CoffeeSitesFoundFromServerResultListener callingService;

    private String error;

    private double latFrom;
    private double longFrom;
    private Map<String, List<Integer>> ranges;
    private String coffeeSort;


    public GetNumberOfCoffeeSitesInRangeAsyncTask(CoffeeSitesFoundFromServerResultListener parentService,
                                                  double latFrom, double longFrom, List<Integer> ranges, String coffeeSort) {
        this.callingService = parentService;
        initSearchParameters(latFrom, longFrom, ranges, coffeeSort);
    }

    /**
     * Method to perform initialization of search parameters before REST request is sent to
     * server.
     */
    private void initSearchParameters(double latFrom, double longFrom, List<Integer> ranges, String coffeeSort) {
        this.latFrom = latFrom;
        this.longFrom = longFrom;
        this.ranges = new HashMap<>();
        this.ranges.put("distances", ranges);
        this.coffeeSort = coffeeSort.isEmpty() ? "?" : coffeeSort;
    }


    public void execute() {

        Log.i(TAG, "start");

        CoffeeSiteRESTInterface api = RetrofitClientProvider.getInstance()
                .getRetrofit(CoffeeSiteRESTInterface.COFFEESITE_API_PUBLIC_SEARCH_URL)
                .create(CoffeeSiteRESTInterface.class);

        Call<Map<String, Integer>> call = api.getNumbersOfCoffeeSitesInRanges(this.latFrom, this.longFrom, this.ranges);

        Log.i(TAG, "start call");

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call, Response<Map<String, Integer>> response) {
                Map<String, Integer> numOfCoffeeSites;
                if (response.isSuccessful()) {
                    Log.i(TAG, "onSuccess()");
                    if (response.body() != null) {
                        numOfCoffeeSites = response.body();
                        if (callingService != null) {
                            callingService.onNumberOfSitesInRangesReturnedFromServer(numOfCoffeeSites);
                        }
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            error = response.errorBody().string();
                            callingService.onSitesInRangeReturnedFromServerError(error);
                        }
                    } catch (IOException e) {
                        error = e.getMessage();
                        Log.e(TAG, error);
                        callingService.onSitesInRangeReturnedFromServerError(error);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                error = "Error loading number of CoffeeSites in ranges REST request." + t.getMessage();
                Log.e(TAG, error);
                callingService.onSitesInRangeReturnedFromServerError(error);
            }
        });
    }
}
