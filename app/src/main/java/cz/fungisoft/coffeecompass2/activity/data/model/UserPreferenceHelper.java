package cz.fungisoft.coffeecompass2.activity.data.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves loggged-in user data into "preferences" to use it
 * after application is closed and opened again
 */
public class UserPreferenceHelper {

    private final String INTRO = "intro";

    private final String USER_ID = "userid";
    private final String DISPLAY_NAME = "displayName";

    private final String USER_NAME = "userName";

    private final String EMAIL = "email";

    private final String NUM_OF_CREATED_SITES = "numOfCreatedSites";
    private final String NUM_OF_UPDATED_SITES = "numOfUpdatedSites";
    private final String NUM_OF_DELETED_SITES = "numOfDeletedSites";

    private final String FIRST_NAME = "firstName";
    private final String LAST_NAME = "lastName";

    private final String CREATED_ON = "createdOn";

    private final String USER_ROLES = "userRoles";

    private final String DEVICE_ID = "deviceID";

    private final String LOGIN_TOKEN = "loginToken";


    private SharedPreferences app_prefs;
    private Context context;

    public UserPreferenceHelper(Context context) {
        app_prefs = context.getSharedPreferences("shared",
                Context.MODE_PRIVATE);
        this.context = context;
    }

    public void putIsLogin(boolean loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putBoolean(INTRO, loginorout);
        edit.commit();
    }
    public boolean getIsLogin() {
        return app_prefs.getBoolean(INTRO, false);
    }

    public void putName(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(USER_NAME, loginorout);
        edit.commit();
    }
    public String getName() {
        return app_prefs.getString(USER_NAME, "");
    }

    public void putUserId(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(USER_ID, loginorout);
        edit.commit();
    }
    public String getUserId() {
        return app_prefs.getString(USER_ID, "");
    }

    public void putDisplayName(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(DISPLAY_NAME, loginorout);
        edit.commit();
    }
    public String getUserName() {
        return app_prefs.getString(USER_NAME, "");
    }

    public void putUserName(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(USER_NAME, loginorout);
        edit.commit();
    }
    public String getDisplayName() {
        return app_prefs.getString(DISPLAY_NAME, "");
    }

    public void putEmail(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(EMAIL, loginorout);
        edit.commit();
    }
    public String getEmail() {
        return app_prefs.getString(EMAIL, "");
    }

    public void putNumOfCreatedSites(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(NUM_OF_CREATED_SITES, loginorout);
        edit.commit();
    }
    public String getNumOfCreatedSites() {
        return app_prefs.getString(NUM_OF_CREATED_SITES, "");
    }

    public void putNumOfUpdatedSites(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(NUM_OF_UPDATED_SITES, loginorout);
        edit.commit();
    }
    public String getNumOfUpdatedSites() {
        return app_prefs.getString(NUM_OF_UPDATED_SITES, "");
    }

    public void putNumOfDeletedSites(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(NUM_OF_DELETED_SITES, loginorout);
        edit.commit();
    }
    public String getNumOfDeletedSites() {
        return app_prefs.getString(NUM_OF_DELETED_SITES, "");
    }

    public void putFirstName(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(FIRST_NAME, loginorout);
        edit.commit();
    }
    public String getFirstName() {
        return app_prefs.getString(FIRST_NAME, "");
    }

    public void putLastName(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(LAST_NAME, loginorout);
        edit.commit();
    }
    public String getLastName() {
        return app_prefs.getString(LAST_NAME, "");
    }

    public void putCreatedOn(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(CREATED_ON, loginorout);
        edit.commit();
    }
    public String getCreatedOn() {
        return app_prefs.getString(CREATED_ON, "");
    }

    public void putUserRoles(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(USER_ROLES, loginorout);
        edit.commit();
    }
    public String getUserRoles() {
        return app_prefs.getString(USER_ROLES, "");
    }

    public void putDeviceId(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(DEVICE_ID, loginorout);
        edit.commit();
    }
    public String getDeviceId() {
        return app_prefs.getString(DEVICE_ID, "");
    }

    public void putLoginToken(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(LOGIN_TOKEN, loginorout);
        edit.commit();
    }

    public String getLoginToken() {
        return app_prefs.getString(LOGIN_TOKEN, "");
    }

    public void saveUserData(LoggedInUser user) {

        //TODO

    }

    public LoggedInUser getSavedUserData() {
        LoggedInUser user = new LoggedInUser();

        user.setUserName(getUserName());
        user.setFirstName(getFirstName());
        user.setLastName(getLastName());

        user.setCreatedOn(getCreatedOn());
        //user.setLoginToken(getLoginToken());
        user.setEmail(getEmail());
        user.setDisplayName(getDisplayName());
        user.setDeviceID(getDeviceId());
        user.setUserId(getUserId());
        //user.setUserRoles(getUserRoles());
        user.setNumOfDeletedSites(Integer.parseInt(getNumOfDeletedSites()));
        user.setNumOfCreatedSites(Integer.parseInt(getNumOfCreatedSites()));
        user.setNumOfUpdatedSites(Integer.parseInt(getNumOfUpdatedSites()));

        return user;
    }
}
