package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteNumbersRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AsyncTask to perform REST Retrofit call to coffeecompass.cz to get number of active CoffeeSites
 * created by one user.
 */
public class GetNumberOfCoffeeSitesFromCurrentUserAsyncTask {

    private static final String TAG = "GetNumSitesFromUserAT";

    private final UserAccountActionsProvider userAccountService;

    /**
     * Only one result listener is expected for Async task
     */
    private final CoffeeSiteNumbersRESTResultListener callingListenerService;

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;

    private String operationError = "";
    private Result.Error error;

    public GetNumberOfCoffeeSitesFromCurrentUserAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                                          UserAccountActionsProvider userAccountService,
                                                          CoffeeSiteNumbersRESTResultListener callingService) {
        this.userAccountService = userAccountService;
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
    }

    public void execute() {
        Log.i(TAG, "start");
        //operationResult = "";
        operationError = "";

        Log.i(TAG, "currentUSer is null? " + (userAccountService.getLoggedInUser() == null));
        if (userAccountService.getLoggedInUser() != null) {
            // Inserts currentUser authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", userAccountService.getAccessTokenType() + " " + userAccountService.getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

            CoffeeSiteRESTInterface api = RetrofitClientProvider.getInstance()
                    .getRetrofitWithAuth(CoffeeSiteRESTInterface.COFFEE_SITE_SECURED_URL,
                            headerAuthorizationInterceptor,
                            new TokenAuthenticator(userAccountService))
                    .create(CoffeeSiteRESTInterface.class);

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
                    if (t.getMessage().startsWith("Refreshing access token failed")) {
                        userAccountService.clearLoggedInUser();
                        // go to login activity
                        Utils.openLoginActivityOnRefreshTokenFailed((Context) userAccountService);
                    }
                }
            });
        }
    }

}
