package cz.fungisoft.coffeecompass2.activity.data;

//import com.android.volley.RequestQueue;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.UserLoginRESTRequest;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginViewModel;
//import cz.fungisoft.coffeecompass2.asynctask.LoginUserRESTAsyncTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 *
 * Contacts server via REST to perform login
 */
public class LoginDataSource {

    private LoginViewModel loginViewModel;

//    public LoginDataSource(LoginViewModel loginViewModel) {
//        this.loginViewModel = loginViewModel;
//    }

    public LoginDataSource() {
    }

    public void setLoginViewModel(LoginViewModel loginViewModel) {
        this.loginViewModel = loginViewModel;
    }

    //public Result<LoggedInUser> login(String username, String password, String deviceID) {
    public void login(String username, String password, String deviceID) {

        try {
            UserLoginRESTRequest userLoginRESTRequest = new UserLoginRESTRequest(deviceID,null, username, password, loginViewModel);
            //LoggedInUser currentLoggedInUser = userLoginRESTRequest.performRequest();
            userLoginRESTRequest.performRequest();
            //LoggedInUser currentLoggedInUser = new LoginUserRESTAsyncTask(username, password, deviceID).execute().get(3, TimeUnit.SECONDS);
            //LoggedInUser currentLoggedInUser = new LoginUserRESTAsyncTask(username, password, deviceID).execute().get();

            //return new Result.Success<>(currentLoggedInUser);
        } catch (Exception e) {
           // return new Result.Error(new IOException("Error reading current user: " +  "", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
