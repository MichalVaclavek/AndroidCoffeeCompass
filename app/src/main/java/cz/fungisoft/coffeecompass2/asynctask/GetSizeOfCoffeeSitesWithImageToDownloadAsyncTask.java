package cz.fungisoft.coffeecompass2.asynctask;

import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.datadownload.DataDownloadSizesRESTInterface;
import cz.fungisoft.coffeecompass2.services.interfaces.DataDownloadSizeRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Runs Async Task to call REST API endpoint for obtaining size of data to be downloaded,
 * when requesting Offline mode i.e. all CoffeeSites including their images.
 */
public class GetSizeOfCoffeeSitesWithImageToDownloadAsyncTask {

    static final String REQ_TAG = "SitesImagesDownloadSize";


    private final DataDownloadSizeRESTResultListener parentActivity;

    public GetSizeOfCoffeeSitesWithImageToDownloadAsyncTask(DataDownloadSizeRESTResultListener parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void execute() {
        Log.d(REQ_TAG, "GetSizeOfCoffeeSitesWithImageToDownloadAsyncTask REST request initiated");

        //Add the interceptor to the client builder.
        Retrofit retrofit = new Retrofit.Builder()
                .client(Utils.getOkHttpClientBuilder().build())
                .baseUrl(DataDownloadSizesRESTInterface.GET_DATA_DOWNLOAD_SIZE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        DataDownloadSizesRESTInterface api = retrofit.create(DataDownloadSizesRESTInterface.class);

        Call<Integer> call = api.getSizeOfAllToDownload();

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, "onResponse()");
                        if (parentActivity != null) {
                            Result.Success<Integer> result = new Result.Success<>(response.body());
                            parentActivity.onSizeOfAllDataToDownload(result);
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for obtaining size of data to be downloaded.");
                        Result.Error error = new Result.Error(new IOException("Error obtaining size of data to be downloaded. Response empty."));
                        if (parentActivity != null) {
                            parentActivity.onSizeOfDataToDownloadError(error);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (parentActivity != null) {
                            parentActivity.onSizeOfDataToDownloadError(new Result.Error(Utils.getRestError(errorBody)));
                        }
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error obtaining size of data to be downloaded." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error obtaining size of data to be downloaded.", e));
                        if (parentActivity != null) {
                            parentActivity.onSizeOfDataToDownloadError(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(REQ_TAG, "Error obtaining size of data to be downloaded." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error obtaining size of data to be downloaded.", t));
                if (parentActivity != null) {
                    parentActivity.onSizeOfDataToDownloadError(error);
                }
            }
        });

    }

}
