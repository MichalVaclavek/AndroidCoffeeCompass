package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteRESTResultListener;
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
 * Class to call AsyncTasks operations to change CoffeeSite status. Statuses can be:
 *
 * ACTIVATE
 * DEACTIVATE
 * CANCEL
 *
 */
public class ChangeStatusOfCoffeeSiteAsyncTask extends AsyncTask<Void, Void, Void> {

    private final CoffeeSite coffeeSiteToModify;

    /**
     * Provides current logged-in user info
     */
    private final UserAccountActionsProvider userAccountService;

    private String operationError = "";

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;
    private final CoffeeSiteRESTResultListener callingListenerService;

    private Result.Error error;

    private final String tag;

    public ChangeStatusOfCoffeeSiteAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                             CoffeeSite coffeeSite,
                                             UserAccountActionsProvider userAccountService,
                                             CoffeeSiteRESTResultListener callingService) {
        this.coffeeSiteToModify = coffeeSite;
        this.userAccountService = userAccountService;
        this.callingListenerService = callingService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
        tag = "SiteStatusAsynTask";
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(tag, "start");
        //operationResult = "";
        operationError = "";

        Log.i(tag, "currentUser is null? " + (userAccountService.getLoggedInUser() == null));
        if (userAccountService.getLoggedInUser() != null && coffeeSiteToModify != null) {
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

            //Add the interceptor to the client builder.
            OkHttpClient client = Utils.getOkHttpClientBuilder()
                    .addInterceptor(headerAuthorizationInterceptor)
                    .authenticator(new TokenAuthenticator(userAccountService))
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

            Call<CoffeeSite> call = null;

            //TODO overeni, ze CoffeeSite ma aktualni status vhodny ke pozadovane operaci
            // tj. atributy canBeActivated, atd. viz DTO object
            switch (this.requestedRESTOperationCode) {
                case COFFEE_SITE_ACTIVATE:
                    call = api.activateCoffeeSite(coffeeSiteToModify.getId());
                    break;
                case COFFEE_SITE_DEACTIVATE:
                    call = api.deactivateCoffeeSite(coffeeSiteToModify.getId());
                    break;
                case COFFEE_SITE_CANCEL:
                    call = api.cancelCoffeeSite(coffeeSiteToModify.getId());
                    break;
            }

            Log.i(tag, "start call");

            call.enqueue(new Callback<CoffeeSite>() {
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
                            Log.i(tag, "Returned empty response for changing CoffeeSite state request.");
                            error = new Result.Error(new IOException("Error changing CoffeeSite state. Response empty."));
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
                    Log.e(tag, "Error changing CoffeeSite state REST request." + t.getMessage());
                    error = new Result.Error(new IOException("Error changing CoffeeSite state.", t));
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
        return null;
    }

}
