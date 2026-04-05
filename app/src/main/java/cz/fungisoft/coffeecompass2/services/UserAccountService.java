package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.UserAccountDataSource;
import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;
import cz.fungisoft.coffeecompass2.activity.data.UserPreferencesHelper;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.JwtUserToken;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoggedInUserView;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;
import cz.fungisoft.coffeecompass2.activity.ui.login.LogoutOrDeleteResult;
import cz.fungisoft.coffeecompass2.asynctask.user.DeleteUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.user.LoginUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.user.LogoutUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.user.RegisterUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLogoutAndDeleteServiceListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserRegisterServiceListener;

/**
 * Service to run user login and logout, and new user register and deleteUser. Also handles refresh token requests/respones.
 * Async tasks and to provide info about currently logged-in user to other Activities, if requested.
 */
public class UserAccountService extends Service implements UserAccountActionsProvider {

    private final String TAG = "UserAccountService";

    // Repository to check if a user is logged-in
    private static UserAccountRepository userLoginAndRegisterRepository;

    private static final MutableLiveData<LoginOrRegisterResult> loginResult = new MutableLiveData<>();
    private static final MutableLiveData<LoginOrRegisterResult> registerResult = new MutableLiveData<>();
    private static final MutableLiveData<LogoutOrDeleteResult> logoutResult = new MutableLiveData<>();
    private static final MutableLiveData<LogoutOrDeleteResult> deleteResult = new MutableLiveData<>();

    public LiveData<LoginOrRegisterResult> getLoginResult() {
        return loginResult;
    }
    public LiveData<LoginOrRegisterResult> getRegisterResult() {
        return registerResult;
    }
    public LiveData<LogoutOrDeleteResult> getLogoutResult() {
        return logoutResult;
    }
    public LiveData<LogoutOrDeleteResult> getDeleteResult() {
        return deleteResult;
    }

    /**
     * List of listeneres for user login events.
     */
    private static final List<UserLoginServiceListener> userLoginServiceListeners = new ArrayList<>();

    public void addUserLoginServiceListener(UserLoginServiceListener userLogindServiceListener) {
        if (!userLoginServiceListeners.contains(userLogindServiceListener)) {
            userLoginServiceListeners.add(userLogindServiceListener);
        }
        Log.i(TAG, "Počet posluchačů User login: " + userLoginServiceListeners.size());
        Log.i(TAG, userLogindServiceListener.toString());
    }

    public void removeUserLoginServiceListener(UserLoginServiceListener userLogindServiceListener) {
        userLoginServiceListeners.remove(userLogindServiceListener);
        Log.i(TAG, "Počet posluchačů User login: " + userLoginServiceListeners.size());
        Log.i(TAG, userLogindServiceListener.toString());
    }

    /**
     * List of listeneres for new user register events.
     */
    private static final List<UserRegisterServiceListener> userRegisterServiceListeners = new ArrayList<>();

    /**
     * Adds new listener, if it is not already in a list
     * @param userRegisterServiceListener
     */
    public void addUserRegisterServiceListener(UserRegisterServiceListener userRegisterServiceListener) {
        if (!userRegisterServiceListeners.contains(userRegisterServiceListener)) {
            userRegisterServiceListeners.add(userRegisterServiceListener);
        }
        Log.i(TAG, "Počet posluchačů User register: " + userRegisterServiceListeners.size());
        Log.i(TAG, userRegisterServiceListener.toString());
    }

    public void removeUserRegisterServiceListener(UserRegisterServiceListener userRegisterServiceListener) {
        userRegisterServiceListeners.remove(userRegisterServiceListener);
        Log.i(TAG, "Počet posluchačů User register: " + userRegisterServiceListeners.size());
        Log.i(TAG, userRegisterServiceListener.toString());
    }

    /**
     * List of listeneres for logout and deleteUser user account events.
     */
    private static final List<UserLogoutAndDeleteServiceListener> userLogoutAndDeleteServiceListeners = new ArrayList<>();

    public void addLogoutAndDeleteServiceListener(UserLogoutAndDeleteServiceListener logoutAndDeleteServiceListener) {
        if (!userLogoutAndDeleteServiceListeners.contains(logoutAndDeleteServiceListener)) {
            userLogoutAndDeleteServiceListeners.add(logoutAndDeleteServiceListener);
        }
        Log.i(TAG, "Počet posluchačů User logout and deleteUser: " + userLogoutAndDeleteServiceListeners.size());
        Log.i(TAG, logoutAndDeleteServiceListener.toString());
    }

    public void removeLogoutAndDeleteServiceListener(UserLogoutAndDeleteServiceListener logoutAndDeleteServiceListener) {
        userLogoutAndDeleteServiceListeners.remove(logoutAndDeleteServiceListener);
        Log.i(TAG, "Počet posluchačů User logout and deleteUser: " + userLogoutAndDeleteServiceListeners.size());
        Log.i(TAG, logoutAndDeleteServiceListener.toString());
    }

    @Override
    public String getAccessToken() {
        return userLoginAndRegisterRepository.getLoggedInUser() != null ? userLoginAndRegisterRepository.getLoggedInUser().getAccessToken() : null;
    }

    @Override
    public String getAccessTokenType() {
        return userLoginAndRegisterRepository.getLoggedInUser() != null ? userLoginAndRegisterRepository.getLoggedInUser().getAccessTokenType() : null;
    }

    @Override
    public boolean isAccessTokenExpired() {
        return userLoginAndRegisterRepository.getLoggedInUser() == null || userLoginAndRegisterRepository.getLoggedInUser().isAccessTokenExpired();
    }

    @Override
    public String getRefreshToken() {
        return userLoginAndRegisterRepository.getLoggedInUser() != null ? userLoginAndRegisterRepository.getLoggedInUser().getRefreshToken() : null;
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        UserAccountService getService() {
            return UserAccountService.this;
        }
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new UserAccountService.LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        UserPreferencesHelper userPreferenceHelper = new UserPreferencesHelper(this);
        userLoginAndRegisterRepository = UserAccountRepository.getInstance(new UserAccountDataSource(this), userPreferenceHelper);
        Log.d(TAG, "Service started.");
    }

    public void login(String username, String password,  String deviceID) {
        new LoginUserRESTAsyncTask(username, password, deviceID, userLoginAndRegisterRepository).execute();
    }

    public void register(String username, String password, String email, String deviceID) {
        new RegisterUserRESTAsyncTask(username, password, email, deviceID, userLoginAndRegisterRepository).execute();
    }

    /**
     * Starts user logout Async task.
     * The Async task invokes REST call to perform logout, then.
     * When the REST call is finished, it calls evaluation methods of
     * {@link UserAccountActionsProvider} interface, which is implemented by
     * this service (UserAccountService) to process further actions
     * (update user account repository and UI of the calling Activity)
     * within this service.
     *
     * Same flow happens during login, register and deleteUser account actions.
     */
    public void logout() {
        new LogoutUserRESTAsyncTask(userLoginAndRegisterRepository).execute();
    }

    /**
     * Starts user deleteUser Async task
     */
    public void delete() {
        new DeleteUserRESTAsyncTask(userLoginAndRegisterRepository).execute();
    }

    /**
     * Starts refresh token Async task
     */
    public JwtUserToken refreshTokenSync() {
        return userLoginAndRegisterRepository.refreshToken();
    }

    /**
     * Method called within login REST response evaluation
     *
     * @param result
     */
    @Override
    public void evaluateLoginResult(Result result) {
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginOrRegisterResult(new LoggedInUserView(data.getUserName())));
            userLoginAndRegisterRepository.setLoggedInUser(data);
            onUserLoggedInSuccess();
        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                Log.e(TAG, "Error when returning user login data. " + error.getDetail());
                loginResult.setValue(new LoginOrRegisterResult(error));
            }
            onUserLoggedInFailure();
        }
    }

    /**
     * Method called within new user register REST response evaluation
     *
     * @param result
     */
    @Override
    public void evaluateRegisterResult(Result result) {
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            registerResult.setValue(new LoginOrRegisterResult(new LoggedInUserView(data.getUserName())));
            userLoginAndRegisterRepository.setLoggedInUser(data);
            onUserRegisterSuccess();
        } else {
            Result.Error error = (Result.Error) result;
            if (error != null) {
                Log.e(TAG, "Error when returning user register data. " + error.getDetail());
                registerResult.setValue(new LoginOrRegisterResult(error));
            }
            onUserRegisterFailure();
        }
    }

    /**
     * Method called within logout REST response evaluation
     *
     * @param result
     */
    @Override
    public void evaluateLogoutResult(Result result) {
        if (result instanceof Result.Success) {
            String data = ((Result.Success<String>) result).getData();
            logoutResult.setValue(new LogoutOrDeleteResult(data, ""));
            clearLoggedInUser();
            onUserLogoutSuccess();
        } else {
            String error = ((Result.Error) result).getDetail();
            logoutResult.setValue(new LogoutOrDeleteResult("", error));
            onUserLogoutFailure();
        }
    }

    @Override
    public void clearLoggedInUser() {
        userLoginAndRegisterRepository.setLoggedInUser(null);
    }

    /**
     * Method called within deleteUser user REST response evaluation
     *
     * @param result
     */
    @Override
    public void evaluateDeleteResult(Result result) {
        if (result instanceof Result.Success) {
            String data = ((Result.Success<String>) result).getData();
            deleteResult.setValue(new LogoutOrDeleteResult(data, ""));
            userLoginAndRegisterRepository.setLoggedInUser(null);
            onUserDeleteSuccess();
        } else {
            String error = ((Result.Error) result).getDetail();
            deleteResult.setValue(new LogoutOrDeleteResult("", error));
            onUserDeleteFailure();
        }
    }

    /**
     * Returns currently loged in user or null, if user none is registered
     */
    @Override
    public LoggedInUser getLoggedInUser() {
        return userLoginAndRegisterRepository.getLoggedInUser();
    }

    public boolean isUserLoggedIn() {
        return userLoginAndRegisterRepository.isLoggedIn();
    }

    // Fire-up methods for login/register/logout events to be processed by listeners

    /* ---- User Register ----  */
    private void onUserRegisterSuccess() {
        for (UserRegisterServiceListener listener : userRegisterServiceListeners) {
            listener.onUserRegisterSuccess(registerResult.getValue());
        }
    }

    private void onUserRegisterFailure() {
        for (UserRegisterServiceListener listener : userRegisterServiceListeners) {
            listener.onUserRegisterFailure(registerResult.getValue());
        }
    }

    /* ---- User Log-in ----  */
    private void onUserLoggedInSuccess() {
        for (UserLoginServiceListener listener : userLoginServiceListeners) {
            listener.onUserLoggedInSuccess(loginResult.getValue());
        }
    }
    private void onUserLoggedInFailure() {
        for (UserLoginServiceListener listener : userLoginServiceListeners) {
            listener.onUserLoggedInFailure(loginResult.getValue());
        }
    }

    /* ---- User Log-out ----  */
    private void onUserLogoutSuccess() {
        for (UserLogoutAndDeleteServiceListener listener : userLogoutAndDeleteServiceListeners) {
            listener.onUserLogoutSuccess();
        }
    }

    private void onUserLogoutFailure() {
        for (UserLogoutAndDeleteServiceListener listener : userLogoutAndDeleteServiceListeners) {
            String errorDetail = (logoutResult.getValue().getError() != null)
                                 ? logoutResult.getValue().getError()
                                 : getString(R.string.logout_failure);
            if (errorDetail.contains(getString(R.string.user_no_value_present_error))) {
                // logout failure, because account does not exist (usually deleted from another device)
                // follow normal logout finish procedure
                userLoginAndRegisterRepository.setLoggedInUser(null);
                listener.onUserLogoutSuccess();
            } else {
                listener.onUserLogoutFailure(errorDetail);
            }
        }
    }

    /* ---- User Delete ----  */
    private void onUserDeleteSuccess() {
        for (UserLogoutAndDeleteServiceListener listener : userLogoutAndDeleteServiceListeners) {
            listener.onUserDeleteSuccess();
        }
    }

    private void onUserDeleteFailure() {
        for (UserLogoutAndDeleteServiceListener listener : userLogoutAndDeleteServiceListeners) {
            String errorDetail = (deleteResult.getValue().getError() != null)
                                 ? deleteResult.getValue().getError()
                                 : getString(R.string.delete_user_failure);

            if (errorDetail.contains(getString(R.string.user_no_value_present_error))) {
                // Attempt to deleteUser already deleted user
                userLoginAndRegisterRepository.setLoggedInUser(null);
                listener.onUserDeleteSuccess();
            } else {
                listener.onUserDeleteFailure(errorDetail);
            }
        }
    }

    /* ---- Refresh access token  ----  */
    private void onRefreshTokenSuccess() {
        for (UserLoginServiceListener listener : userLoginServiceListeners) {
            listener.onRefreshTokenSuccess(userLoginAndRegisterRepository.getLoggedInUser());
        }
    }

    private void onRefreshTokenFailure() {
        for (UserLoginServiceListener listener : userLoginServiceListeners) {
            listener.onRefreshTokenFailure(userLoginAndRegisterRepository.getLoggedInUser());
        }
    }

}
