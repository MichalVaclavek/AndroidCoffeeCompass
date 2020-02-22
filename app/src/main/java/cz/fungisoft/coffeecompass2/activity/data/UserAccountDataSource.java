package cz.fungisoft.coffeecompass2.activity.data;

import android.util.Log;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.UserDeleteRESTRequest;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.UserLoginOrRegisterRESTRequest;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.UserLogoutRESTRequest;
import cz.fungisoft.coffeecompass2.services.UserAccountService;

/**
 * Class that handles authentication / login credentials and retrieves logged-in user information.
 * Calls respective methods of the REST requests classes to perform register, login, logout
 * and delete of a user.
 */
public class UserAccountDataSource {

    private UserAccountService userLoginAndRegisterService;

    private final String TAG = "UserAccountDataSource";

    public UserAccountDataSource(UserAccountService userLoginAndRegisterService) {
        this.userLoginAndRegisterService = userLoginAndRegisterService;
    }

    public void login(String username, String password, String deviceID) {

        try {
            UserLoginOrRegisterRESTRequest userLoginRESTRequest = new UserLoginOrRegisterRESTRequest(deviceID,null, username, password, userLoginAndRegisterService);
            userLoginRESTRequest.performLoginRequest();
        } catch (Exception e) {
            Log.e(TAG, "Current user response failure. " + e.getMessage());
        }
    }

    public void register(String username, String password, String email, String deviceID) {

        try {
            UserLoginOrRegisterRESTRequest registerUserRESTRequest = new UserLoginOrRegisterRESTRequest(deviceID, email, username, password, userLoginAndRegisterService);
            registerUserRESTRequest.performRegisterRequest();
        } catch (Exception e) {
            Log.e(TAG, "Register new user failure. " + e.getMessage());
        }
    }

    public void logout(LoggedInUser currentUser) {
        try {
            UserLogoutRESTRequest logoutUserRESTRequest = new UserLogoutRESTRequest(currentUser, userLoginAndRegisterService);
            logoutUserRESTRequest.performLogoutRequest();
        } catch (Exception e) {
            Log.e(TAG, "User logout failure. " + e.getMessage());
        }
    }

    public void delete(LoggedInUser user) {
        try {
            UserDeleteRESTRequest deleteUserRESTRequest = new UserDeleteRESTRequest(user, userLoginAndRegisterService);
            deleteUserRESTRequest.performDeleteRequest();
        } catch (Exception e) {
            Log.e(TAG, "User delete failure. " + e.getMessage());
        }
    }
}
