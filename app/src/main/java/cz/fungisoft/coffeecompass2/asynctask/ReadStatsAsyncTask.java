package cz.fungisoft.coffeecompass2.asynctask;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.activity.AboutActivity;
import cz.fungisoft.coffeecompass2.activity.interfaces.stats.StatisticsRESTInterface;
import cz.fungisoft.coffeecompass2.entity.Statistics;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * AsyncTask to read basic statistics from coffeecompass.cz about saved CoffeeSites.
 * The task runs at the start of the AboutActivity.
 * Uses Retrofit for REST call and Gson for JSON deserialization.
 */
public class ReadStatsAsyncTask {

    private static final String TAG = "ReadStatsAsyncTask";

    private final WeakReference<AboutActivity> parentActivity;

    public ReadStatsAsyncTask(AboutActivity parentActivity) {
        this.parentActivity = new WeakReference<>(parentActivity);
    }

    public void execute() {
        Log.d(TAG, "ReadStatsAsyncTask REST request initiated");

        OkHttpClient client = Utils.getOkHttpClientBuilder().build();

        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(StatisticsRESTInterface.HOME_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        StatisticsRESTInterface api = retrofit.create(StatisticsRESTInterface.class);

        Call<Statistics> call = api.getStatistics();

        call.enqueue(new Callback<Statistics>() {
            @Override
            public void onResponse(Call<Statistics> call, Response<Statistics> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(TAG, "onResponse() success");
                        if (parentActivity.get() != null) {
                            parentActivity.get().showAndSaveStatistics(response.body());
                        }
                    } else {
                        Log.e(TAG, "Returned empty response for statistics request.");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "unknown error";
                        Log.e(TAG, "Error reading statistics. Server response: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading statistics." + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<Statistics> call, Throwable t) {
                Log.e(TAG, "Error reading statistics REST request." + t.getMessage());
            }
        });

    }
}
