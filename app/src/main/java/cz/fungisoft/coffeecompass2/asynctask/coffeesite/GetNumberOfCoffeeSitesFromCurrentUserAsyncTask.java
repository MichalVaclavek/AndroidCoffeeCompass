package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class GetNumberOfCoffeeSitesFromCurrentUserAsyncTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "GetSitesFromUserAsnTsk";

    private final LoggedInUser currentUser;

    private final CoffeeSiteService callingService;

    private String operationResult = "";
    private String operationError = "";

    public GetNumberOfCoffeeSitesFromCurrentUserAsyncTask(LoggedInUser user, CoffeeSiteService callingService) {
        this.currentUser = user;
        this.callingService = callingService;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "start");
        operationResult = "";
        operationError = "";

        Log.i(TAG, "currentUSer is null? " + String.valueOf(currentUser == null));
        if (currentUser != null) {

            // Inserts currentUser authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", currentUser.getLoginToken().getTokenType() + " " + currentUser.getLoginToken().getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

            //Add the interceptor to the client builder.
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerAuthorizationInterceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(CoffeeSiteRESTInterface.COFFEE_SITE_SECURED_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            CoffeeSiteRESTInterface api = retrofit.create(CoffeeSiteRESTInterface.class);

            Call<Integer> call = api.getNumberOfSitesNotCanceledFromCurrentUser();

            Log.i(TAG, "start call");

            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            Integer coffeeSitesNumber = response.body();
                            operationResult = "OK";
                            callingService.sendCoffeeSitesFromUserNumberResultToClient(coffeeSitesNumber, operationResult, "");
                        } else {
                            Log.i(TAG, "Returned empty response for obtaining CoffeeSites number created by User REST request.");
                            Result.Error error = new Result.Error(new IOException("Error obtaining CoffeeSites number created by User. Response empty."));
                            operationError = error.toString();
                            callingService.sendCoffeeSitesFromUserNumberResultToClient(0,"", operationError);
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                            operationError = callingService.getResources().getString(R.string.coffeesiteservice_error_message_not_available);
                        }
                        callingService.sendCoffeeSitesFromUserNumberResultToClient(0,"", operationError);
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e(TAG, "Error obtaining CoffeeSites number created by User REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error obtaining CoffeeSites number created by User REST request.", t));
                    operationError = error.toString();
                    callingService.sendCoffeeSitesFromUserNumberResultToClient(0,"", operationError);
                }
            });
        }
        return null;
    }
}
