package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeResultListener;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Async Task to run REST api request to obtain all coffeeSites
 * within current distance range.
 */
public class GetCoffeeSitesInRangeAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetSitesInRangeAsyncT";

    /**
     * A Service, which invokes this async. task
     */
    private CoffeeSitesInRangeResultListener callingService;

    private String error;

    private double latFrom;
    private double longFrom;
    private int range;
    private String coffeeSort;


    public GetCoffeeSitesInRangeAsyncTask(CoffeeSitesInRangeResultListener parentService,
                                          double latFrom, double longFrom, int range, String coffeeSort) {
        this.callingService = parentService;
        initSearchParameters(latFrom, longFrom, range, coffeeSort);
    }

    /**
     * Method to perform initialization of search parameters before REST request is sent to
     * server.
     */
    private void initSearchParameters(double latFrom, double longFrom, int range, String coffeeSort) {
        this.latFrom = latFrom;
        this.longFrom = longFrom;
        this.range = range;
        this.coffeeSort = coffeeSort.isEmpty() ? "?" : coffeeSort;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        Log.i(TAG, "start");

        //Add the interceptor to the client builder.
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("dd. MM. yyyy HH:mm")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                                        .client(client)
                                        .baseUrl(CoffeeSiteRESTInterface.COFFEESITE_API_PUBLIC_SEARCH_URL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build();

        CoffeeSiteRESTInterface api = retrofit.create(CoffeeSiteRESTInterface.class);

        Call<List<CoffeeSite>> call = api.getCoffeeSitesInRange(this.latFrom, this.longFrom, this.range, this.coffeeSort);

        Log.i(TAG, "start call");

        call.enqueue(new Callback<List<CoffeeSite>>() {
            @Override
            public void onResponse(Call<List<CoffeeSite>> call, Response<List<CoffeeSite>> response) {
                List<CoffeeSiteMovable> coffeeSiteMovables = new ArrayList<>();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(TAG, "onSuccess()");
                        List<CoffeeSite> coffeeSites = response.body();
                        // Convert to CoffeeSiteMovable
                        for (CoffeeSite cs : coffeeSites) {
                            coffeeSiteMovables.add(new CoffeeSiteMovable(cs));
                        }
                        if (callingService != null) {
                            callingService.onSitesInRangeReturnedFromServer(coffeeSiteMovables);
                         }
                    } else {
                        error = "Returned empty response for loading CoffeeSites from user REST request.";
                        Log.i(TAG, error);
                        callingService.onSitesInRangeReturnedFromServer(coffeeSiteMovables);
                    }
                } else {
                    try {
                        if (response.code() == 404) { // No CoffeeSite found
                            if (callingService != null) {
                                callingService.onSitesInRangeReturnedFromServer(coffeeSiteMovables);
                            }
                        } else {
                            error = response.errorBody().string();
                            callingService.onSitesInRangeReturnedFromServerError(error);
                        }
                    } catch (IOException e) {
                        error =  e.getMessage();
                        Log.e(TAG, error);
                        callingService.onSitesInRangeReturnedFromServerError(error);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoffeeSite>> call, Throwable t) {
                error = "Error loading CoffeeSites from User REST request." + t.getMessage();
                Log.e(TAG, error);
                callingService.onSitesInRangeReturnedFromServerError(error);
            }
        });
        return null;
    }

}
