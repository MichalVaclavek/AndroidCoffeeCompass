package cz.fungisoft.coffeecompass2.services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import cz.fungisoft.coffeecompass2.activity.data.NotificationSubscriptionPreferencesHelper;

/**
 * Service Class to process incoming Firebase notification messages.
 */
public class FirebaseMessageService extends FirebaseMessagingService {

    public FirebaseMessageService() {
    }

    private static final String TAG = "FirebaseService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Nazev: " + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "Zprava: " + remoteMessage.getNotification().getBody());

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }

            /**
             * Notifies those observing events about new Notification message data (usually MainActivity)
             */
            Notification.getInstance()
                        .addData(remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * There are two scenarios when onNewToken is called:
     * <p>
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        //sendRegistrationToServer(token);
        NotificationSubscriptionPreferencesHelper notificationSubscriptionPreferencesHelper = new NotificationSubscriptionPreferencesHelper(this);
        notificationSubscriptionPreferencesHelper.putFirebaseToken(token);
    }

    /**
     * Pomocna inner class to Notify MainActivity, that notification push message was received
     * using LiveData
     */
    public static class Notification {

        private static Notification instance;
        private final MutableLiveData<Map<String, String>> notificationMessageData;

        private Notification() {
            notificationMessageData = new MutableLiveData<>();
        }

        public static Notification getInstance() {
            if (instance == null) {
                instance = new Notification();
            }
            return instance;
        }

        public LiveData<Map<String, String>> getMessageData() {
            return notificationMessageData;
        }

        public void addData(Map<String, String> data) {
            notificationMessageData.postValue(data);
        }
    }
}
