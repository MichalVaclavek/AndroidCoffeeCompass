package cz.fungisoft.coffeecompass2.activity.ui.notification;

/**
 * To define methods to be fired after successful or failed
 * subscription for new CoffeeSites notification.
 */
public interface NotificationSubscriptionCallListener {

    void onNotificationSubscriptionSuccess(NotificationSubscriptionRequestResult subscriptionRequestResult);
    void onNotificationSubscriptionFailure(NotificationSubscriptionRequestResult subscriptionRequestResult);
}
