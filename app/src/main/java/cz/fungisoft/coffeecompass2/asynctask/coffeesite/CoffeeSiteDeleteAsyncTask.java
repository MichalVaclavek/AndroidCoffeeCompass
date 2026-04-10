package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteIdRESTResultListener;
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
 * AsyncTask pro Delete operaci s CoffeeSite
 */
public class CoffeeSiteDeleteAsyncTask {

    private final CoffeeSite coffeeSite;

    /**
     * Provides current logged-in user info
     */
    private final UserAccountActionsProvider userAccountService;

    //private String operationResult = "";
    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;

    private final CoffeeSiteIdRESTResultListener callingListenerDeleteService;

    private Result.Error error;

    private final String tag;

    public CoffeeSiteDeleteAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                            CoffeeSite coffeeSite,
                                            UserAccountActionsProvider userAccountService,
                                            CoffeeSiteIdRESTResultListener callingDeleteService) {

        this.coffeeSite = coffeeSite;
        this.userAccountService = userAccountService;
        this.callingListenerDeleteService = callingDeleteService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;

        tag = "SiteOperationAsyncTask";
    }

    public void execute() {
        Log.i(tag, "start");
        //operationResult = "";
        operationError = "";

        Log.i(tag, "currentUSer is null? " + (userAccountService.getLoggedInUser() == null));
        if (userAccountService.getLoggedInUser() != null) {
            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor;
            headerAuthorizationInterceptor = new Interceptor() {
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

            Call<Integer> call = api.deleteCoffeeSite(coffeeSite.getId());

            Log.i(tag, "start call");

            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(tag, "onSuccess()");
                            //operationResult = "OK";
                            Integer coffeeSite = response.body();
                            Result.Success<Integer> result = new Result.Success<>(coffeeSite);
                            if (callingListenerDeleteService != null) {
                                callingListenerDeleteService.onCoffeeSitesIdReturned(requestedRESTOperationCode, result);
                            }
                        } else {
                            Log.i(tag, "Returned empty response for deleting CoffeeSite request.");
                            error = new Result.Error(new IOException("Error deleting CoffeeSite. Response empty."));
                            operationError = error.toString();
                            if (callingListenerDeleteService != null) {
                                callingListenerDeleteService.onCoffeeSitesIdReturned(requestedRESTOperationCode, error);
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
                        if (callingListenerDeleteService != null) {
                            callingListenerDeleteService.onCoffeeSitesIdReturned(requestedRESTOperationCode, error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e(tag, "Error deleting CoffeeSite REST request." + t.getMessage());
                    error = new Result.Error(new IOException("Error deleting CoffeeSite.", t));
                    operationError = error.toString();

                    if (callingListenerDeleteService != null) {
                        callingListenerDeleteService.onCoffeeSitesIdReturned(requestedRESTOperationCode, error);
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
