package cz.fungisoft.coffeecompass2.activity.ui.notification;

/**
 * To define methods to be fired after successful or failed
 * subscription API call for a new CoffeeSites notification or cancel such notifications.
 */
public interface NotificationSubscriptionCallListener {

    void onNotificationSubscriptionSuccess(NotificationSubscriptionRequestResult subscriptionRequestResult);
    void onNotificationSubscriptionFailure(NotificationSubscriptionRequestResult subscriptionRequestResult);

    void onCancelNotificationSubscriptionSuccess(NotificationSubscriptionRequestResult subscriptionRequestResult);
    void onCancelNotificationSubscriptionFailure(NotificationSubscriptionRequestResult subscriptionRequestResult);
}
