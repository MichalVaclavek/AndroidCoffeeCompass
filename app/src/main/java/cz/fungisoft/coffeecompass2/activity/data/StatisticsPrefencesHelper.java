package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.JwtUserToken;
import cz.fungisoft.coffeecompass2.entity.Statistics;

/**
 * Saves {@link Statistics} data into "Preferences" to use it
 * after application is closed and opened again.
 */
public class StatisticsPrefencesHelper {

    /**
     * Statistics atributes
     */
    private final String NUM_OF_SITES_ACTIVE = "numOfSitesActive";
    private final String NUM_OF_SITES_LAST_WEEK = "numOfSitesLastWeek";
    private final String NUM_OF_SITES_TODAY = "numOfSitesToday";
    private final String NUM_OF_USERS = "numOfUsers";

    private final String DEFAULT_VALUE = "0";

    private SharedPreferences app_prefs;
    private Context context;

    private final String nameOfSharedPreferences = "statistics";

    public StatisticsPrefencesHelper(Context context) {
        app_prefs = context.getSharedPreferences(nameOfSharedPreferences,
                Context.MODE_PRIVATE);
        this.context = context;
    }

    private void putNumOfSitesActive(String numOfSitesActive) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(NUM_OF_SITES_ACTIVE, numOfSitesActive);
        edit.apply();
    }
    private String getNumOfSitesActive() {
        return app_prefs.getString(NUM_OF_SITES_ACTIVE, DEFAULT_VALUE);
    }

    private void putNumOfSitesLastWeek(String numOfSitesLastWeek) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(NUM_OF_SITES_LAST_WEEK, numOfSitesLastWeek);
        edit.apply();
    }
    private String getNumOfSitesLastWeek() {
        return app_prefs.getString(NUM_OF_SITES_LAST_WEEK, DEFAULT_VALUE);
    }


    private void putNumOfSitesToday(String numOfSitesToday) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(NUM_OF_SITES_TODAY, numOfSitesToday);
        edit.apply();
    }
    private String getNumOfSitesToday() {
        return app_prefs.getString(NUM_OF_SITES_TODAY, DEFAULT_VALUE);
    }


    private void putNumOfUsers(String numOfUsers) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(NUM_OF_USERS, numOfUsers);
        edit.apply();
    }
    private String getNumOfUsers() {
        return app_prefs.getString(NUM_OF_USERS, DEFAULT_VALUE);
    }


    public void saveStatistics(Statistics statistics) {

        putNumOfSitesActive(statistics.numOfSites);
        putNumOfSitesLastWeek(statistics.numOfSitesLastWeek);
        putNumOfSitesToday(statistics.numOfSitesToday);
        putNumOfUsers(statistics.numOfUsers);

    }

    public Statistics getSavedStatistics() {

        Statistics statistics = new Statistics(getNumOfSitesActive(),
                getNumOfSitesLastWeek(),
                getNumOfSitesToday(),
                getNumOfUsers());
        return statistics;
    }

    /**
     * To delete/remove saved user data, if user loged-out
     */
    public void removeUserData() {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.clear();
        edit.apply();
    }

}
