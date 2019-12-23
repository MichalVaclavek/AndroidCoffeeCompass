package cz.fungisoft.coffeecompass2.activity.data.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.rest.JwtUserToken;

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

    private final String USER_ROLES = "userRole";

    private final String DEVICE_ID = "deviceID";

    private final String LOGIN_TOKEN = "loginToken";
    private final String LOGIN_TOKEN_TYPE = "loginTokenType";
    private final String LOGIN_TOKEN_EXPIRY = "loginTokenExpiry";


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

    public void putUserId(String userId) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(USER_ID, userId);
        edit.commit();
    }
    public String getUserId() {
        return app_prefs.getString(USER_ID, "");
    }

    public void putDisplayName(String displayName) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(DISPLAY_NAME, displayName);
        edit.commit();
    }
    public String getUserName() {
        return app_prefs.getString(USER_NAME, null);
    }

    public void putUserName(String userName) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(USER_NAME, userName);
        edit.commit();
    }
    public String getDisplayName() {
        return app_prefs.getString(DISPLAY_NAME, "");
    }

    public void putEmail(String email) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(EMAIL, email);
        edit.commit();
    }
    public String getEmail() {
        return app_prefs.getString(EMAIL, "");
    }

    public void putNumOfCreatedSites(int numOfCreatedSites) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(NUM_OF_CREATED_SITES, numOfCreatedSites);
        edit.commit();
    }
    public int getNumOfCreatedSites() {
        return app_prefs.getInt(NUM_OF_CREATED_SITES, 0);
    }

    public void putNumOfUpdatedSites(int numOfUpdatedSites) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(NUM_OF_UPDATED_SITES, numOfUpdatedSites);
        edit.commit();
    }
    public int getNumOfUpdatedSites() {
        return app_prefs.getInt(NUM_OF_UPDATED_SITES, 0);
    }

    public void putNumOfDeletedSites(int numOfDeletedSites) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(NUM_OF_DELETED_SITES, numOfDeletedSites);
        edit.commit();
    }
    public int getNumOfDeletedSites() {
        return app_prefs.getInt(NUM_OF_DELETED_SITES, 0);
    }

    public void putFirstName(String firstName) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(FIRST_NAME, firstName);
        edit.commit();
    }
    public String getFirstName() {
        return app_prefs.getString(FIRST_NAME, "");
    }

    public void putLastName(String lastName) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(LAST_NAME, lastName);
        edit.commit();
    }
    public String getLastName() {
        return app_prefs.getString(LAST_NAME, "");
    }

    public void putCreatedOn(String createdOn) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(CREATED_ON, createdOn);
        edit.commit();
    }
    public String getCreatedOn() {
        return app_prefs.getString(CREATED_ON, "");
    }

    public void putUserRoles(List<String> userRoles) {
        String[] rolesArray = new String[userRoles.size()];
        rolesArray = userRoles.toArray(rolesArray);
        SharedPreferences.Editor edit = app_prefs.edit();
        for (int i=0; i < rolesArray.length; i++) {
            edit.putString(USER_ROLES + String.valueOf(i), rolesArray[i]);
        }
        edit.commit();
    }
    //TODO
    public List<String> getUserRoles() {
        List<String> retVal = new ArrayList<>();
        int rolesCounter = 0;
        while (!app_prefs.getString(USER_ROLES + String.valueOf(rolesCounter), "").isEmpty()) {
            retVal.add(app_prefs.getString(USER_ROLES  + String.valueOf(rolesCounter), "USER"));
            rolesCounter++;
        }
        return retVal;
    }

    public void putDeviceId(String deviceId) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(DEVICE_ID, deviceId);
        edit.commit();
    }
    public String getDeviceId() {
        return app_prefs.getString(DEVICE_ID, "");
    }

    public void putLoginToken(JwtUserToken loginToken) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(LOGIN_TOKEN, loginToken.getAccessToken());
        edit.putString(LOGIN_TOKEN_EXPIRY, loginToken.getExpiryDateFormated());
        edit.putString(LOGIN_TOKEN_TYPE, loginToken.getTokenType());

        edit.commit();
    }

    public JwtUserToken getLoginToken() {

        JwtUserToken userToken = new JwtUserToken(app_prefs.getString(LOGIN_TOKEN, ""),
                                                  getLoginTokenExpiry(),
                                                  getLoginTokenType());

        return userToken;
    }

    public String getLoginTokenExpiry() {
        return app_prefs.getString(LOGIN_TOKEN_EXPIRY, "");
    }

    public String getLoginTokenType() {
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
        edit.commit();
    }
}
