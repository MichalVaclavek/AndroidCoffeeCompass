package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Saves notification subscription data into Preferences
 */
public class NotificationSubscriptionPreferencesHelper {

    private final String USER_ID = "user_id"; // can be null or 0

    private final String FIREBASE_TOKEN = "firebase_token";

    private final String TOWNS_SET = "towns_set"; // used as subTopics

    private final String TOPIC = "topic"; // subscription main topic

    private final String ALL_TOWNS_TOPIC = "all_towns_topic";

    /**
     * To save info, about latest new CoffeeSite URL received upon Firebase notification.
     * Used in MainActivity.
     */
    private final String LATEST_RECEIVED_NOTIFICATION_URL = "latest_received_url";

    private final SharedPreferences app_prefs;

    private final String nameOfSharedPreferences = "shared_notifications_subscription";

    public NotificationSubscriptionPreferencesHelper(Context context) {
        app_prefs = context.getSharedPreferences(nameOfSharedPreferences,
                Context.MODE_PRIVATE);
    }

    // If 0, means no user for the subscription, only Firebase token is the identity
    public void putUserId(long userId) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putLong(USER_ID, userId);
        edit.apply();
    }
    public long getUserId() {
        return app_prefs.getLong(USER_ID, 0);
    }


    public void putFirebaseToken(String firebaseToken) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(FIREBASE_TOKEN, firebaseToken);
        edit.apply();
    }

    public String getFirebaseToken() {
        return app_prefs.getString(FIREBASE_TOKEN, "");
    }

    public void putTopic(String topic) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(TOPIC, topic);
        edit.apply();
    }

    public String getTopic() {
        return app_prefs.getString(TOPIC, "new_coffeeSite");
    }

    public void putTowns(List<String> towns) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putStringSet(TOWNS_SET, new HashSet<>(towns));
        edit.apply();
    }

    public List<String> getTowns() {
        return new ArrayList<>(app_prefs.getStringSet(TOWNS_SET, new HashSet<>()));
    }

    public void putAllTownsTopicSelected(boolean allTowns) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putBoolean(ALL_TOWNS_TOPIC, allTowns);
        edit.apply();
    }

    public boolean getAllTownsTopicSelected() {
        return app_prefs.getBoolean(ALL_TOWNS_TOPIC, false);
    }


    public void putLatestReceivedUrl(String latestURL) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(LATEST_RECEIVED_NOTIFICATION_URL, latestURL);
        edit.apply();
    }

    public String getLatestReceivedUrl() {
        return app_prefs.getString(LATEST_RECEIVED_NOTIFICATION_URL, "");
    }
}
