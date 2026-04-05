package cz.fungisoft.coffeecompass2.asynctask.places;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.places.CuzkCandidates;
import cz.fungisoft.coffeecompass2.activity.interfaces.places.PlacesCandidatesCUZKRESTInterface;
import cz.fungisoft.coffeecompass2.services.interfaces.PlacesCandidatesCUZKRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Async Task to run REST api request to get {@link cz.fungisoft.coffeecompass2.activity.data.model.rest.places.Candidate}
 * from CUZK Places API.
 */
public class GetPlacesCandidatesCUZKTask {

    private static final String TAG = "GetPlacesCUZKTask";

    private String operationError = "";

    private Result.Error error;

    private final String nameOfPlace;

    private final int maxPlaces;

    private final PlacesCandidatesCUZKRESTResultListener resultListener;

    public GetPlacesCandidatesCUZKTask(String nameOfPlace,
                                       int maxPlaces,
                                       PlacesCandidatesCUZKRESTResultListener resultListener) {
        this.nameOfPlace = nameOfPlace;
        this.maxPlaces = maxPlaces;
        this.resultListener = resultListener;
    }

    public void execute() {

        Log.i(TAG, "start");

        operationError = "";
        Call<CuzkCandidates> call;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(PlacesCandidatesCUZKRESTInterface.CUZK_PLACES_API_SEARCH_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        PlacesCandidatesCUZKRESTInterface api = retrofit.create(PlacesCandidatesCUZKRESTInterface.class);
        call = api.getPlacesCandidates(this.nameOfPlace, this.maxPlaces);

        Log.i(TAG, "start call");
        if (call != null) {
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<CuzkCandidates> call, Response<CuzkCandidates> response) {
                    if (response.isSuccessful()) {
                        int responseCode = response.code();
                        if (responseCode == 504) {
                            return;
                        }

                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            CuzkCandidates candidates = response.body();
                            //operationResult = "OK";
                            Result.Success<CuzkCandidates> result = new Result.Success<>(candidates);
                            if (resultListener != null) {
                                resultListener.onCandidatesReturned(result);
                            }
                        } else {
                            Log.i(TAG, "Returned empty response for loading places candidates.");
                            error = new Result.Error(new IOException("Error obtaining places candidates. Response empty."));
                            operationError = error.toString();
                            if (resultListener != null) {
                                resultListener.onCandidatesReturned(error);
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
                        if (resultListener != null) {
                            resultListener.onCandidatesReturned(error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<CuzkCandidates> call, Throwable t) {
                    Log.e(TAG, "Error loading places candidates." + t.getMessage());
                    error = new Result.Error(new IOException("Error loading places candidates.", t));
                    operationError = error.toString();
                    if (resultListener != null) {
                        resultListener.onCandidatesReturned(error);
                    }
                }
            });
        }

    }

}
