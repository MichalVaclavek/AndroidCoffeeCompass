package cz.fungisoft.coffeecompass2.asynctask.notification;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.notification.NotificationSubscription;
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
 * AsyncTask to call REST methods/interface performing cancel of all previously subscribed notifications.
 * If user is not null, then it calls Secured API otherwise public API.
 */
public class CancelNotificationSubscriptionAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_TAG = "CancelSubscriptAsyncT";

    /**
     * Subscription to be canceled. Empty fields of NotificationSubscription are evaluated by server
     * as the request to cancel ALL previous subscription of the Token.
     */
    private final NotificationSubscription notificationSubscription;

    private final LoggedInUser user;

    /**
     * Usually activity, which started this Async. task, capable to process result of API call
     */
    private final NotificationSubscriptionCallListener subscriptionActivity;


    /**
     * User can be null, then Public API endpoint is used to cancel subscription.
     *
     * @param notificationSubscription subscription to be canceled, in this case empty fields (except Token) are expected as it means All subscription to be canceled
     * @param user
     * @param subscriptionActivity
     */
    public CancelNotificationSubscriptionAsyncTask(NotificationSubscription notificationSubscription, LoggedInUser user, NotificationSubscriptionCallListener subscriptionActivity) {
        this.notificationSubscription = notificationSubscription;
        this.subscriptionActivity = subscriptionActivity;
        this.user = user;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(REQ_TAG, "CancelNotificationSubscriptionAsyncTask REST request initiated");
        OkHttpClient client;
        String baseUrl;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        if (user != null) {
            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", user.getLoginToken().getTokenType() + " " + user.getLoginToken().getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };
            client = new OkHttpClient.Builder().addInterceptor(headerAuthorizationInterceptor)
                                               .addInterceptor(logging)
                                               .build();
            baseUrl = NotificationSubscriptionRESTInterface.NOTIFICATION_SUBSCRIBE_URL;
        } else {
            client = new OkHttpClient.Builder().build();
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
            call = api.notificationUnSubscribeAll(this.notificationSubscription);
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
                                    subscriptionActivity.onCancelNotificationSubscriptionSuccess(result);
                                }
                            }
                        } else {
                            Log.i(REQ_TAG, "Returned empty response for cancel notification subscription API request.");
                            NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(new Result.Error("Empty response"));
                            subscriptionActivity.onCancelNotificationSubscriptionFailure(result);
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(new Result.Error(Utils.getRestError(errorBody)));
                            subscriptionActivity.onCancelNotificationSubscriptionFailure(result);
                        } catch (IOException e) {
                            Log.e(REQ_TAG, "Error response for cancel notification subscription API request." + e.getMessage());
                            NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(new Result.Error("Error cancel notification subscription API."));
                            subscriptionActivity.onCancelNotificationSubscriptionFailure(result);
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<String> call, Throwable t) {
                    Log.e(REQ_TAG, "Error for cancel notification subscription API request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error for cancel notification subscription API request", t));
                    NotificationSubscriptionRequestResult result = new NotificationSubscriptionRequestResult(error);
                    subscriptionActivity.onCancelNotificationSubscriptionFailure(result);
                }
            });
        }
        return null;
    }

}
