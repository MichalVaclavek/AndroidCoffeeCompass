package cz.fungisoft.coffeecompass2.activity.data;

import android.util.Log;

import cz.fungisoft.coffeecompass2.activity.data.model.rest.UserLoginOrRegisterRESTRequest;
import cz.fungisoft.coffeecompass2.services.UserLoginAndRegisterService;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 *
 * Contacts server via REST to perform login
 */
public class LoginAndRegisterDataSource {

    private UserLoginAndRegisterService userLoginAndRegisterService;

    private final String TAG = "LoginAndRegisterSource";

    public LoginAndRegisterDataSource(UserLoginAndRegisterService userLoginAndRegisterService) {
        this.userLoginAndRegisterService = userLoginAndRegisterService;
    }

    public void login(String username, String password, String deviceID) {

        try {
            UserLoginOrRegisterRESTRequest userLoginRESTRequest = new UserLoginOrRegisterRESTRequest(deviceID,null, username, password, userLoginAndRegisterService);
            userLoginRESTRequest.performLoginRequest();
        } catch (Exception e) {
           // return new Result.Error(new IOException("Error reading current user: " +  "", e));
            Log.e(TAG, "Current user response failure. " + e.getMessage());
        }
    }

    public void register(String username, String password, String email, String deviceID) {

        try {
            UserLoginOrRegisterRESTRequest registerUserRESTRequest = new UserLoginOrRegisterRESTRequest(deviceID, email, username, password, userLoginAndRegisterService);
            registerUserRESTRequest.performRegisterRequest();
        } catch (Exception e) {
            // return new Result.Error(new IOException("Error reading current user: " +  "", e));
            Log.e(TAG, "Register new user failure. " + e.getMessage());
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
