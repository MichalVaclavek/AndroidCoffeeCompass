package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
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
 * Async Task to run REST API request to obtain all coffeeSites in given town.
 */
public class GetCoffeeSitesInTownAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetSitesInTownAsyncT";

    /**
     * A Service, which invokes this async. task
     */
    private final CoffeeSitesRESTResultListener callingService;

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;

    private Result.Error error;

    private String operationError = "";

    private String townName;


    public GetCoffeeSitesInTownAsyncTask(CoffeeSitesRESTResultListener parentService,
                                         CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                         String townName) {
        this.callingService = parentService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
        this.townName = townName;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        Log.i(TAG, "start");

        //Add the interceptor to the client builder.
        OkHttpClient client = Utils.getOkHttpClientBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
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

        Call<List<CoffeeSite>> call = api.getCoffeeSitesInTown(this.townName);

        Log.i(TAG, "start call");

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<CoffeeSite>> call, Response<List<CoffeeSite>> response) {
                List<CoffeeSiteMovable> coffeeSiteMovables = new ArrayList<>();
                if (response.isSuccessful()) {
                    Log.i(TAG, "onSuccess()");
                    if (response.body() != null) {
                        List<CoffeeSite> coffeeSites = response.body();
                        if (callingService != null) {
                            Result.Success<List<CoffeeSite>> result = new Result.Success<>(coffeeSites);
                            callingService.onCoffeeSitesReturned(requestedRESTOperationCode, result);
                        }
                    } else {
                        error = new Result.Error(new IOException("Error loading CoffeeSites in town REST request. Response empty."));
                        if (callingService != null) {
                            callingService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                        }
                    }
                } else {
                    Log.i(TAG, "No CoffeeSite found in town.");
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
                    if (callingService != null) {
                        callingService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoffeeSite>> call, Throwable t) {
                Log.e(TAG, "Error loading CoffeeSites in town from server." + t.getMessage());
                error = new Result.Error(new IOException("Error loading CoffeeSites in town REST calls.", t));
                operationError = error.toString();
                if (callingService != null) {
                    callingService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                }
            }
        });

        return null;
    }
}
