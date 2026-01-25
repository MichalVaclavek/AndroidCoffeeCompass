package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesUploadRESTResultListener;
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
 * AsyncTask to call REST API to upload CoffeeSites created/updated when Offline.
 */
public class UploadCoffeeSitesAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "UploadCoffeeSitesAsyncT";

    /**
     * Provides current logged-in user info
     */
    private final UserAccountActionsProvider userAccountService;

    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSitesUploadRESTResultListener callingListenerService;

    private Result.Error error;

    private final List<CoffeeSite> coffeeSitesToUpload;

    /**
     * Starts AsyncTask to load CoffeeSite either by coffeeSiteId or by CoffeeSiteURL
     *
     * @param requestedRESTOperationCode - requested load operation
     * @param callingService - service implementing CoffeeSiteRESTResultListener
     */
    public UploadCoffeeSitesAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                      UserAccountActionsProvider userAccountService, List<CoffeeSite> coffeeSitesToUpload,
                                      CoffeeSitesUploadRESTResultListener callingService) {
        this.requestedRESTOperationCode = requestedRESTOperationCode;
        this.callingListenerService = callingService;
        this.coffeeSitesToUpload = coffeeSitesToUpload;
        this.userAccountService = userAccountService;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "start");
        operationError = "";

        Log.i(TAG, "currentUSer is null? " + (userAccountService.getLoggedInUser() == null));
        if (userAccountService.getLoggedInUser() != null) {

            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", userAccountService.getAccessTokenType() + " " + userAccountService.getAccessToken()).build();

                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            //Add the interceptor to the client builder.
            OkHttpClient client = Utils.getOkHttpClientBuilder()
                    .authenticator(new TokenAuthenticator(userAccountService))
                    .addInterceptor(headerAuthorizationInterceptor)
                    //.addInterceptor(logging)
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

            Call<List<CoffeeSite>> call = api.uploadCoffeeSites(this.coffeeSitesToUpload);

            Log.i(TAG, "start call");

            call.enqueue(new Callback<List<CoffeeSite>>() {
                @Override
                public void onResponse(Call<List<CoffeeSite>> call, Response<List<CoffeeSite>> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            List<CoffeeSite> returnedCoffeeSites = response.body();
                            Result.Success<List<CoffeeSite>> result = new Result.Success<>(returnedCoffeeSites);
                            if (callingListenerService != null) {
                                callingListenerService.onCoffeeSitesUploadedAndReturned(requestedRESTOperationCode, result);
                            }
                        } else {
                            Log.i(TAG, "Returned empty REST response when uploading CoffeeSites.");
                            error = new Result.Error(new IOException("Error uploading CoffeeSite. Response empty."));
                            operationError = error.toString();
                            if (callingListenerService != null) {
                                callingListenerService.onCoffeeSitesUploadedAndReturned(requestedRESTOperationCode, error);
                            }
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                            error = new Result.Error(operationError);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                            operationError = "Chyba komunikace se serverem.";
                        }
                        if (error == null) {
                            error = new Result.Error(operationError);
                        }
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSitesUploadedAndReturned(requestedRESTOperationCode, error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<CoffeeSite>> call, Throwable t) {
                    Log.e(TAG, "Error uploading CoffeeSites REST request." + t.getMessage());
                    error = new Result.Error(new IOException("Error uploading CoffeeSites.", t));
                    operationError = error.toString();

                    if (callingListenerService != null) {
                        callingListenerService.onCoffeeSitesUploadedAndReturned(requestedRESTOperationCode, error);
                    }
                    if (t.getMessage().startsWith("Refreshing access token failed")) {
                        userAccountService.clearLoggedInUser();
                        // go to login activity
                        Utils.openLoginActivityOnRefreshTokenFailed((Context) userAccountService);
                    }
                }
            });
        }
        return null;
    }

}
