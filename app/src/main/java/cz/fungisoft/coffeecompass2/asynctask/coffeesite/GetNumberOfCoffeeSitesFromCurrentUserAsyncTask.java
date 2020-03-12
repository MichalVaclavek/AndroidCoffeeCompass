package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteNumbersRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteRESTInterface;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * AsyncTask to perform REST Retrofit call to coffeecompass.cz to get number of active CoffeeSites
 * created by one user.
 */
public class GetNumberOfCoffeeSitesFromCurrentUserAsyncTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "GetSitesFromUserAsnTsk";

    private final LoggedInUser currentUser;

    /**
     * Only one result listener is expected for Async task
     */
    private final CoffeeSiteNumbersRESTResultListener callingListenerService;

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;

    //private String operationResult = "";
    private String operationError = "";
    private Result.Error error;

    public GetNumberOfCoffeeSitesFromCurrentUserAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                                          LoggedInUser user,
                                                          CoffeeSiteNumbersRESTResultListener callingService) {
        this.currentUser = user;
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "start");
        //operationResult = "";
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
                            //operationResult = "OK";
                            Result.Success<Integer> result = new Result.Success<>(coffeeSitesNumber);
                            if (callingListenerService != null) {
                                callingListenerService.onNumberOfCoffeeSitesReturned(requestedRESTOperationCode, result);
                            }
                        } else {
                            Log.i(TAG, "Returned empty response for obtaining CoffeeSites number created by User REST request.");
                            error = new Result.Error(new IOException("Error obtaining CoffeeSites number created by User. Response empty."));
                            operationError = error.toString();
                            if (callingListenerService != null) {
                                callingListenerService.onNumberOfCoffeeSitesReturned(requestedRESTOperationCode, error);
                            }
                        }
                    } else {
                        try {
                            error = new Result.Error(Utils.getRestError(response.errorBody().string()));
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                            operationError = "Chyba komunikace se serverem."; // TODO read from resources
                        }
                        if (error == null) {
                            error = new Result.Error(operationError);
                        }
                        if (callingListenerService != null) {
                            callingListenerService.onNumberOfCoffeeSitesReturned(requestedRESTOperationCode, error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e(TAG, "Error obtaining CoffeeSites number created by User REST request." + t.getMessage());
                    error = new Result.Error(new IOException("Error obtaining CoffeeSites number created by User REST request.", t));
                    operationError = error.toString();
                    if (callingListenerService != null) {
                        callingListenerService.onNumberOfCoffeeSitesReturned(requestedRESTOperationCode, error);
                    }
                }
            });
        }
        return null;
    }

}
