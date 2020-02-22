package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;
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

    private final CoffeeSiteService callingService;

    private final long coffeeSiteId;

    private String operationResult = "";
    private String operationError = "";

    public GetCoffeeSiteAsyncTask(CoffeeSiteService callingService, long coffeeSiteId) {
        this.callingService = callingService;
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
                        callingService.sendCoffeeSiteLoadResultToClient(coffeeSite, operationResult, "");
                    } else {
                        Log.i(TAG, "Returned empty response for loading CoffeeSite request.");
                        Result.Error error = new Result.Error(new IOException("Error loading CoffeeSite. Response empty."));
                        operationError = error.toString();
                        callingService.sendCoffeeSiteLoadResultToClient(null,"", operationError);
                    }
                } else {
                    try {
                        operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        operationError = callingService.getResources().getString(R.string.coffeesiteservice_error_message_not_available);
                    }
                    callingService.sendCoffeeSiteLoadResultToClient(null,"", operationError);
                }
            }

            @Override
            public void onFailure(Call<CoffeeSite> call, Throwable t) {
                Log.e(TAG, "Error loading CoffeeSite REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error loading CoffeeSite.", t));
                operationError = error.toString();
                callingService.sendCoffeeSiteLoadResultToClient(null,"", operationError);
            }
        });
        return null;
    }
}
