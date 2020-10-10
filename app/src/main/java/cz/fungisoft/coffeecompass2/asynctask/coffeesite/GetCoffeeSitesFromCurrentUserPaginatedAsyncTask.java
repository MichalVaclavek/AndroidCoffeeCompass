package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Async Task to run REST api request to obtain all coffeeSites
 * created by current logged-in User
 */
public class GetCoffeeSitesFromCurrentUserPaginatedAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetSitesFromUserPageAT";

    private final LoggedInUser currentUser;

    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSitesRESTResultListener callingListenerService;

    private Result.Error error;

    private int requestedPage;

    private int pageSize;


    public GetCoffeeSitesFromCurrentUserPaginatedAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                                           int requestedPage,
                                                           int pageSize,
                                                           LoggedInUser user,
                                                           CoffeeSitesRESTResultListener callingService) {
        this.currentUser = user;
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
        this.requestedPage = requestedPage;
        this.pageSize = pageSize;
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
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(25, TimeUnit.SECONDS)
                    .addInterceptor(headerAuthorizationInterceptor)
                    .build();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("dd. MM. yyyy HH:mm")
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(CoffeeSiteRESTInterface.COFFEE_SITE_SECURED_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            CoffeeSiteRESTInterface api = retrofit.create(CoffeeSiteRESTInterface.class);

            Call<CoffeeSitePageEnvelope> call = api.getAllCoffeeSitesFromCurrentUserPaginated(requestedPage, pageSize);

            Log.i(TAG, "start call");

            call.enqueue(new Callback<CoffeeSitePageEnvelope>() {
                @Override
                public void onResponse(Call<CoffeeSitePageEnvelope> call, Response<CoffeeSitePageEnvelope> response) {
                    if (response.isSuccessful()) {
                        int responseCode = response.code();
                        if (responseCode == 504) {return;}

                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            CoffeeSitePageEnvelope coffeeSites = response.body();
                            //operationResult = "OK";
                            Result.Success<CoffeeSitePageEnvelope> result = new Result.Success<>(coffeeSites);
                            if (callingListenerService != null) {
                                callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, result);
                            }
                        } else {
                            Log.i(TAG, "Returned empty response for loading CoffeeSites from user REST request.");
                            error = new Result.Error(new IOException("Error saving CoffeeSite. Response empty."));
                            operationError = error.toString();
                            if (callingListenerService != null) {
                                callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
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
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<CoffeeSitePageEnvelope> call, Throwable t) {
                    Log.e(TAG, "Error loading CoffeeSites from User REST request." + t.getMessage());
                    error = new Result.Error(new IOException("Error loading CoffeeSites from user REST request.", t));
                    operationError = error.toString();
                    if (callingListenerService != null) {
                        callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
                    }
                }
            });
        }
        return null;
    }

}
