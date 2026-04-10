package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Async Task to run REST api request to obtain all coffeeSites
 * created by current logged-in User
 */
public class GetCoffeeSitesFromCurrentUserAsyncTask {

    private static final String TAG = "GetSitesFromUserAsnTsk";

    private final UserAccountActionsProvider userAccountService;

    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSitesRESTResultListener callingListenerService;

    private Result.Error error;


    public GetCoffeeSitesFromCurrentUserAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                                  UserAccountActionsProvider userAccountService,
                                                  CoffeeSitesRESTResultListener callingService) {
        this.userAccountService = userAccountService;
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
    }

    public void execute() {

        Log.i(TAG, "start");
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

            Call<List<CoffeeSite>> call = api.getAllCoffeeSitesByCurrentUser();

            Log.i(TAG, "start call");

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<List<CoffeeSite>> call, Response<List<CoffeeSite>> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            List<CoffeeSite> coffeeSites = response.body();
                            //operationResult = "OK";
                            Result.Success<List<CoffeeSite>> result = new Result.Success<>(coffeeSites);
                            if (callingListenerService != null) {
                                callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, result);
                            }
                        } else {
                            Log.i(TAG, "Returned empty response for loading CoffeeSites from user REST request.");
                            error = new Result.Error(new IOException("Errorloading CoffeeSites from user. Response empty."));
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
                public void onFailure(Call<List<CoffeeSite>> call, Throwable t) {
                    Log.e(TAG, "Error loading CoffeeSites from User REST request." + t.getMessage());
                    error = new Result.Error(new IOException("Error loading CoffeeSites from user REST request.", t));
                    operationError = error.toString();

                    if (callingListenerService != null) {
                        callingListenerService.onCoffeeSitesReturned(requestedRESTOperationCode, error);
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
