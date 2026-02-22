package cz.fungisoft.coffeecompass2.asynctask.notification;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.notification.NotificationSubscription;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.activity.interfaces.notification.NotificationSubscriptionRESTInterface;
import cz.fungisoft.coffeecompass2.activity.ui.notification.NotificationSubscriptionCallListener;
import cz.fungisoft.coffeecompass2.activity.ui.notification.NotificationSubscriptionRequestResult;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * AsyncTask to call REST methods/interface performing notification subscription.
 * If user is not null, then it calls Secured API otherwise public API.
 */
public class NotificationSubscriptionAsyncTask {

    static final String REQ_TAG = "NotifSubscriptionAsyncT";

    private final NotificationSubscription notificationSubscription;

    private final UserAccountActionsProvider userAccountService;

    /**
     * Usually activity, which started this Async. task, capable to process result of API call
     */
    private final NotificationSubscriptionCallListener subscriptionActivity;


    /**
     * User can be null, then Public API endpoint is used to subscribe.
     *
     * @param notificationSubscription
     * @param user
     * @param subscriptionActivity
     */
    public NotificationSubscriptionAsyncTask(NotificationSubscription notificationSubscription, UserAccountActionsProvider userAccountService, NotificationSubscriptionCallListener subscriptionActivity) {
        this.notificationSubscription = notificationSubscription;
        this.subscriptionActivity = subscriptionActivity;
        this.userAccountService = userAccountService;
    }

    public void execute() {
        Log.d(REQ_TAG, "NotificationSubscriptionAsyncTask REST request initiated");
        OkHttpClient client;
        String baseUrl;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

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
            client = Utils.getOkHttpClientBuilder().addInterceptor(headerAuthorizationInterceptor)
                                               .authenticator(new TokenAuthenticator(userAccountService))
                                               .addInterceptor(logging)
                                               .build();
            baseUrl = NotificationSubscriptionRESTInterface.NOTIFICATION_SUBSCRIBE_URL;
        } else {
            client = Utils.getOkHttpClientBuilder().build();
            baseUrl = NotificationSubscriptionRESTInterface.NOTIFICATION_SUBSCRIBE_PUBLIC_URL;
        }

        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                                        .client(client)
                                        .baseUrl(baseUrl)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build();

        Call<String> call = null;
        try {
            NotificationSubscriptionRESTInterface api = retrofit.create(NotificationSubscriptionRESTInterface.class);
            call = api.notificationSubscribe(this.notificationSubscription);
        } catch (Exception ex) {
            Log.e(REQ_TAG, "Error creating API request." + ex.getMessage());
        }

        if (call != null) {
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(REQ_TAG, "onResponse() success");
                            if (subscriptionActivity != null) {
                                if (response.body().contains("accepted")) {
                                    NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(true);
                                    subscriptionActivity.onNotificationSubscriptionSuccess(result);
                                }
                            }
                        } else {
                            Log.i(REQ_TAG, "Returned empty response for notification subscription API request.");
                            NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(new Result.Error("Empty response"));
                            subscriptionActivity.onNotificationSubscriptionFailure(result);
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(new Result.Error(Utils.getRestError(errorBody)));
                            subscriptionActivity.onNotificationSubscriptionFailure(result);
                        } catch (IOException e) {
                            Log.e(REQ_TAG, "Error response for notification subscription API request." + e.getMessage());
                            NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(new Result.Error("Error notification subscription API."));
                            subscriptionActivity.onNotificationSubscriptionFailure(result);
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<String> call, Throwable t) {
                    Log.e(REQ_TAG, "Error for notification subscription API request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error for notification subscription API request", t));
                    NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(error);
                    subscriptionActivity.onNotificationSubscriptionFailure(result);
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
