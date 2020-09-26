package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.JwtUserToken;

/**
 * Saves logged-in user data into "Preferences" to use it
 * after application is closed and opened again.
 */
public class UserPreferencesHelper {

    private final String INTRO = "intro";

    private final String USER_ID = "userid";
    private final String DISPLAY_NAME = "displayName";

    private final String USER_NAME = "userName";

    private final String EMAIL = "email";

    private final String NUM_OF_CREATED_SITES = "numOfCreatedSites";
    private final String NUM_OF_UPDATED_SITES = "numOfUpdatedSites";
    private final String NUM_OF_DELETED_SITES = "numOfDeletedSites";
    // Number of created sites minus number of canceled sites, i.e. Active or Inactive sites
    private final String NUM_OF_NOT_CANCELED_SITES = "numOfNotCanceledSites";


    private final String FIRST_NAME = "firstName";
    private final String LAST_NAME = "lastName";

    private final String CREATED_ON = "createdOn";

    private final String USER_ROLES = "userRole";

    private final String DEVICE_ID = "deviceID";

    private final String LOGIN_TOKEN = "loginToken";
    private final String LOGIN_TOKEN_TYPE = "loginTokenType";
    private final String LOGIN_TOKEN_EXPIRY = "loginTokenExpiry";

    private SharedPreferences app_prefs;
    private Context context;

    private final String nameOfSharedPreferences = "sharedUser2";

    public UserPreferencesHelper(Context context) {
        app_prefs = context.getSharedPreferences(nameOfSharedPreferences,
                Context.MODE_PRIVATE);
        this.context = context;
    }

    private void putIsLogin(boolean loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putBoolean(INTRO, loginorout);
        edit.apply();
    }
    public boolean getIsLogin() {
        return app_prefs.getBoolean(INTRO, false);
    }

    private void putUserId(long userId) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putLong(USER_ID, userId);
        edit.apply();
    }
    private long getUserId() {
        return app_prefs.getLong(USER_ID, 0);
    }

    private void putDisplayName(String displayName) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(DISPLAY_NAME, displayName);
        edit.apply();
    }
    public String getUserName() {
        return app_prefs.getString(USER_NAME, null);
    }

    private void putUserName(String userName) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(USER_NAME, userName);
        edit.apply();
    }
    private String getDisplayName() {
        return app_prefs.getString(DISPLAY_NAME, "");
    }

    private void putEmail(String email) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(EMAIL, email);
        edit.apply();
    }
    public String getEmail() {
        return app_prefs.getString(EMAIL, "");
    }

    private void putNumOfCreatedSites(int numOfCreatedSites) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(NUM_OF_CREATED_SITES, numOfCreatedSites);
        edit.apply();
    }
    private int getNumOfCreatedSites() {
        return app_prefs.getInt(NUM_OF_CREATED_SITES, 0);
    }

    public void putNumOfNotCanceledSites(int numOfNotCanceledSites) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(NUM_OF_NOT_CANCELED_SITES, numOfNotCanceledSites);
        edit.apply();
    }
    public int getNumOfNotCanceledSites() {
        return app_prefs.getInt(NUM_OF_NOT_CANCELED_SITES, 0);
    }


    private void putNumOfUpdatedSites(int numOfUpdatedSites) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(NUM_OF_UPDATED_SITES, numOfUpdatedSites);
        edit.apply();
    }
    private int getNumOfUpdatedSites() {
        return app_prefs.getInt(NUM_OF_UPDATED_SITES, 0);
    }

    private void putNumOfDeletedSites(int numOfDeletedSites) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(NUM_OF_DELETED_SITES, numOfDeletedSites);
        edit.apply();
    }
    private int getNumOfDeletedSites() {
        return app_prefs.getInt(NUM_OF_DELETED_SITES, 0);
    }

    private void putFirstName(String firstName) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(FIRST_NAME, firstName);
        edit.apply();
    }
    private String getFirstName() {
        return app_prefs.getString(FIRST_NAME, "");
    }

    private void putLastName(String lastName) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(LAST_NAME, lastName);
        edit.apply();
    }
    private String getLastName() {
        return app_prefs.getString(LAST_NAME, "");
    }

    private void putCreatedOn(String createdOn) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(CREATED_ON, createdOn);
        edit.apply();
    }
    private String getCreatedOn() {
        return app_prefs.getString(CREATED_ON, "");
    }

    private void putUserRoles(List<String> userRoles) {
        String[] rolesArray = new String[userRoles.size()];
        rolesArray = userRoles.toArray(rolesArray);
        SharedPreferences.Editor edit = app_prefs.edit();
        for (int i=0; i < rolesArray.length; i++) {
            edit.putString(USER_ROLES + String.valueOf(i), rolesArray[i]);
        }
        edit.apply();
    }

    private List<String> getUserRoles() {
        List<String> retVal = new ArrayList<>();
        int rolesCounter = 0;
        while (!app_prefs.getString(USER_ROLES + String.valueOf(rolesCounter), "").isEmpty()) {
            retVal.add(app_prefs.getString(USER_ROLES  + String.valueOf(rolesCounter), "USER"));
            rolesCounter++;
        }
        return retVal;
    }

    private void putDeviceId(String deviceId) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(DEVICE_ID, deviceId);
        edit.apply();
    }
    private String getDeviceId() {
        return app_prefs.getString(DEVICE_ID, "");
    }

    private void putLoginToken(JwtUserToken loginToken) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(LOGIN_TOKEN, loginToken.getAccessToken());
        edit.putString(LOGIN_TOKEN_EXPIRY, loginToken.getExpiryDateFormated());
        edit.putString(LOGIN_TOKEN_TYPE, loginToken.getTokenType());

        edit.apply();
    }

    public JwtUserToken getLoginToken() {

        JwtUserToken userToken = new JwtUserToken(app_prefs.getString(LOGIN_TOKEN, ""),
                                                  getLoginTokenExpiry(),
                                                  getLoginTokenType());

        return userToken;
    }

    private String getLoginTokenExpiry() {
        return app_prefs.getString(LOGIN_TOKEN_EXPIRY, "");
    }

    private String getLoginTokenType() {
        return app_prefs.getString(LOGIN_TOKEN_TYPE, "Bearer");
    }


    public void saveUserData(LoggedInUser user) {

        putIsLogin(true);
        putUserId(user.getUserId());
        putUserName(user.getUserName());
        putDeviceId(user.getDeviceID());
        putDisplayName(user.getDisplayName());
        putEmail(user.getEmail());
        putFirstName(user.getFirstName());
        putLastName(user.getLastName());
        putLoginToken(user.getLoginToken());
        putNumOfCreatedSites(user.getNumOfCreatedSites());
        putNumOfUpdatedSites(user.getNumOfUpdatedSites());
        putNumOfDeletedSites(user.getNumOfDeletedSites());
        putCreatedOn(user.getCreatedOnFormated());
        putUserRoles(user.getUserRoles());
    }

    public LoggedInUser getSavedUserData() {

        LoggedInUser user = null;
        // Check if data are saved
        if (getUserName() != null) {

            user = new LoggedInUser();

            user.setUserName(getUserName());
            user.setFirstName(getFirstName());
            user.setLastName(getLastName());

            user.setCreatedOn(getCreatedOn());
            user.setLoginToken(getLoginToken());
            user.setEmail(getEmail());
            user.setDisplayName(getDisplayName());
            user.setDeviceID(getDeviceId());
            user.setUserId(getUserId());
            user.setUserRoles(getUserRoles());
            user.setNumOfDeletedSites(getNumOfDeletedSites());
            user.setNumOfCreatedSites(getNumOfCreatedSites());
            user.setNumOfUpdatedSites(getNumOfUpdatedSites());
        }
        return user;
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
