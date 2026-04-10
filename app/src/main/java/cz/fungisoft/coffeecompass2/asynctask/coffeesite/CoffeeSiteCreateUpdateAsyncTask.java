package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteRESTResultListener;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * AsyncTasky pro Create, Update operace s CoffeeSite
 */
public class CoffeeSiteCreateUpdateAsyncTask {

    private final CoffeeSite coffeeSite;

    /**
     * Provides current logged-in user info
     */
    private final UserAccountActionsProvider userAccountService;

    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSiteRESTResultListener callingListenerService;

    private Result.Error error;

    private final String tag;

    public CoffeeSiteCreateUpdateAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                           CoffeeSite coffeeSite,
                                           UserAccountActionsProvider userAccountService,
                                           CoffeeSiteRESTResultListener callingService) {

        this.coffeeSite = coffeeSite;
        this.userAccountService = userAccountService;
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;

        tag = "SiteOperationAsyncTask";
    }

    public void execute() {
        Log.i(tag, "start");
        operationError = "";

        Log.i(tag, "currentUSer is null? " + (userAccountService.getLoggedInUser() == null));
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

            CoffeeSiteRESTInterface api = RetrofitClientProvider.getInstance()
                    .getRetrofitWithAuth(CoffeeSiteRESTInterface.COFFEE_SITE_SECURED_URL,
                            headerAuthorizationInterceptor,
                            new TokenAuthenticator(userAccountService))
                    .create(CoffeeSiteRESTInterface.class);

            Call<CoffeeSite> call = null;

            switch (this.requestedRESTOperationCode) {
                case COFFEE_SITE_SAVE:
                    call = api.createCoffeeSite(coffeeSite);
                    break;
                case COFFEE_SITE_UPDATE:
                    call = api.updateCoffeeSite(coffeeSite.getId(), coffeeSite);
                    break;
            }

            Log.i(tag, "start call");

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<CoffeeSite> call, Response<CoffeeSite> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(tag, "onSuccess()");
                            //operationResult = "OK";
                            CoffeeSite coffeeSite = response.body();
                            Result.Success<CoffeeSite> result = new Result.Success<>(coffeeSite);
                            if (callingListenerService != null) {
                                callingListenerService.onCoffeeSiteReturned(requestedRESTOperationCode, result);
                            }
                        } else {
                            Log.i(tag, "Returned empty response for saving CoffeeSite request.");
                            error = new Result.Error(new IOException("Error saving CoffeeSite. Response empty."));
                            operationError = error.toString();
                            if (callingListenerService != null) {
                                callingListenerService.onCoffeeSiteReturned(requestedRESTOperationCode, error);
                            }
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                            error = new Result.Error(operationError);
                        } catch (IOException e) {
                            Log.e(tag, e.getMessage());
                            operationError = "Chyba komunikace se serverem.";
                        }
                        if (error == null) {
                            error = new Result.Error(operationError);
                        }
                        if (callingListenerService != null) {
                            callingListenerService.onCoffeeSiteReturned(requestedRESTOperationCode, error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<CoffeeSite> call, Throwable t) {
                    Log.e(tag, "Error saving CoffeeSite REST request." + t.getMessage());
                    error = new Result.Error(new IOException("Error saving CoffeeSite.", t));
                    operationError = error.toString();

                    if (callingListenerService != null) {
                        callingListenerService.onCoffeeSiteReturned(requestedRESTOperationCode, error);
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
