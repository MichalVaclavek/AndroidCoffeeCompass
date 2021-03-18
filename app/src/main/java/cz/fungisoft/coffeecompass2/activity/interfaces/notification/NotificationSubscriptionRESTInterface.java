package cz.fungisoft.coffeecompass2.activity.interfaces.notification;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.notification.NotificationSubscription;
import cz.fungisoft.coffeecompass2.asynctask.comment.SaveCommentAndStarsAsyncTask;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit interface for REST requests related to push notification subscription
 * Used by {@link SaveCommentAndStarsAsyncTask} and {@link cz.fungisoft.coffeecompass2.asynctask.comment.DeleteCommentAsyncTask}.
 */
public interface NotificationSubscriptionRESTInterface {

    String NOTIFICATION_SUBSCRIBE_URL = BuildConfig.NOTIFICATION_SUBSCRIPTION_API_SECURED_URL;
    String NOTIFICATION_SUBSCRIBE_PUBLIC_URL = BuildConfig.NOTIFICATION_SUBSCRIPTION_API_PUBLIC_URL;


    /**
     * Calls notification subscription on server - can be used with NOTIFICATION_SUBSCRIBE_PUBLIC_URL to public URL (no Authorization needed)
     * or with NOTIFICATION_SUBSCRIBE_URL as secured version i.e. with Authorization header required
     *
     * @param notificationSubscription - data/towns for Topic notification subscription
     * @return - server response, expected something like "200 subscription accepted"
     */
    @POST("subscribe")
    Call<String> notificationSubscribe(@Body NotificationSubscription notificationSubscription);

    /**
     * Calls notification unsubscribe on server - can be used with NOTIFICATION_SUBSCRIBE_PUBLIC_URL to public URL (no Authorization needed)
     * or with NOTIFICATION_SUBSCRIBE_URL as secured version i.e. with Authorization header required
     *
     * @param notificationSubscription - data/towns for Topic notification to unsubscribe
     * @return - server response, expected something like "200 subscription accepted"
     */
    @POST("unsubscribe")
    Call<String> notificationUnSubscribe(@Body NotificationSubscription notificationToUnSubscribe);

}
