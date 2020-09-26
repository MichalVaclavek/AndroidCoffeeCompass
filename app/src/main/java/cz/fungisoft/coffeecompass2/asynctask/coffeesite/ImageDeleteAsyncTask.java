package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.ImageRESTInterface;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteImageService;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ImageDeleteAsyncTask extends AsyncTask<Void, Void, Void> {

    private final LoggedInUser currentUser;

    /**
     * Id of the CoffeeSite whose image is requested to be deleted
     */
    private int coffeeSiteId;

    private WeakReference<CoffeeSiteImageService> callingService;

    private String operationResult = "";
    private String operationError = "";

    private static final String TAG = "ImageDeleteAsyncTask";


    public ImageDeleteAsyncTask(CoffeeSiteImageService imageService, LoggedInUser currentUser, int coffeeSiteId) {
        this.callingService = new WeakReference<>(imageService);
        this.currentUser = currentUser;
        this.coffeeSiteId = coffeeSiteId;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "start");
        operationResult = "";
        operationError = "";

        Log.i(TAG, "currentUSer is null? " + String.valueOf(currentUser == null));
        if (currentUser != null && coffeeSiteId != 0) {

            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", currentUser.getLoginToken().getTokenType() + " " + currentUser.getLoginToken().getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

            //HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            //logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            //Add the interceptor to the client builder.
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerAuthorizationInterceptor)
                    //.addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(ImageRESTInterface.DELETE_IMAGE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            ImageRESTInterface api = retrofit.create(ImageRESTInterface .class);

            Call<Integer> call = api.deleteImageBySiteId(coffeeSiteId);

            Log.i(TAG, "start call");

            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            operationResult = "OK";
                            int coffeeSiteID = response.body();
                            if (callingService.get() != null) {
                                callingService.get().evaluateImageDeleteResult(new Result.Success<>(coffeeSiteID));
                            }
                        } else {
                            Log.i(TAG, "Returned empty response for deleting image request.");
                            Result.Error error = new Result.Error(new IOException("Error deleting image. Response empty."));
                            if (callingService.get() != null) {
                                callingService.get().evaluateImageDeleteResult(error);
                            }
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                            if (callingService.get() != null) {
                                operationError = callingService.get().getResources().getString(R.string.coffeesiteservice_error_message_not_available);
                            }
                        }
                        Result.Error error = new Result.Error(new IOException(operationError));
                        if (callingService.get() != null) {
                            callingService.get().evaluateImageDeleteResult(error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e(TAG, "Error deleting image REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error deleting image.", t));
                    operationError = error.toString();
                    if (callingService.get() != null) {
                        callingService.get().evaluateImageDeleteResult(error);
                    }
                }
            });
        }
        return null;
    }

}