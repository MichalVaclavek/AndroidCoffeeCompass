package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * ASyncTask to call REST api obtaining one CoffeeSite
 * by its id
 */
public class GetCoffeeSiteAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetCoffeeSiteAsyncTask";

    private final long coffeeSiteId;

    private String operationResult = "";
    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSiteRESTResultListener callingListenerService;

    private Result.Error error;

    public GetCoffeeSiteAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                  CoffeeSiteRESTResultListener callingService, long coffeeSiteId) {
        this.requestedRESTOperationCode = requestedRESTOperationCode;
        this.callingListenerService = callingService;
        this.coffeeSiteId = coffeeSiteId;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "start");
        operationResult = "";
        operationError = "";

        //Add the interceptor to the client builder.
        OkHttpClient client = new OkHttpClient.Builder()
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

        Call<CoffeeSite> call = api.getCoffeeSiteById(coffeeSiteId);

        Log.i(TAG, "start call");

        call.enqueue(new Callback<CoffeeSite>() {
            @Override
            public void onResponse(Call<CoffeeSite> call, Response<CoffeeSite> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(TAG, "onSuccess()");
                        //Log.i("onSuccess", response.body());
                        CoffeeSite coffeeSite = response.body();
                        operationResult = "OK";
                        Result.Success<CoffeeSite> result = new Result.Success<>(coffeeSite);
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSiteReturned(requestedRESTOperationCode, result);
                        }
                    } else {
                        Log.i(TAG, "Returned empty response for loading CoffeeSite request.");
                        error = new Result.Error(new IOException("Error loading CoffeeSite. Response empty."));
                        operationError = error.toString();
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSiteReturned(requestedRESTOperationCode, error);
                        }
                    }
                } else {
                    try {
                        operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                        error = new Result.Error(Utils.getRestError(response.errorBody().string()));
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        operationError = "Chyba komunikace se serverem.";
                    }
                    if (error == null) {
                        error = new Result.Error(operationError);
                    }
                    if (callingListenerService != null) {
                        callingListenerService.onCoffeeSiteReturned(requestedRESTOperationCode, error);
                    }
                }
            }

            @Override
            public void onFailure(Call<CoffeeSite> call, Throwable t) {
                Log.e(TAG, "Error loading CoffeeSite REST request." + t.getMessage());
                error = new Result.Error(new IOException("Error loading CoffeeSite.", t));
                operationError = error.toString();
                if (callingListenerService != null) {
                    callingListenerService.onCoffeeSiteReturned(requestedRESTOperationCode, error);
                }
            }
        });
        return null;
    }

}
