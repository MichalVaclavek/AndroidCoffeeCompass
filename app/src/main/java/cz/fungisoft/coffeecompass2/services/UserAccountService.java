package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.UserAccountDataSource;
import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import cz.fungisoft.coffeecompass2.activity.data.model.UserPreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoggedInUserView;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;
import cz.fungisoft.coffeecompass2.activity.ui.login.LogoutOrDeleteResult;
import cz.fungisoft.coffeecompass2.asynctask.DeleteUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.LoginUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.LogoutUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.RegisterUserRESTAsyncTask;

/**
 * Service to run user login and register Async tasks and to
 * provide info about currently logged-in user to other Activities, if requested.
 */
public class UserAccountService extends Service {

    private String TAG = "UserAccount service";

    // Repository to check if a user is logen in
    private UserAccountRepository userLoginAndRegisterRepository;

    private UserPreferenceHelper preferenceHelper;

    private MutableLiveData<LoginOrRegisterResult> loginResult = new MutableLiveData<>();
    private MutableLiveData<LoginOrRegisterResult> registerResult = new MutableLiveData<>();
    private MutableLiveData<LogoutOrDeleteResult> logoutResult = new MutableLiveData<LogoutOrDeleteResult>();
    private MutableLiveData<LogoutOrDeleteResult> deleteResult = new MutableLiveData<>();

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

    private List<UserLoginServiceListener> userLoginServiceListeners = new ArrayList<>();

    public void addUserLoginServiceListener(UserLoginServiceListener userLogindServiceListener) {
        userLoginServiceListeners.add(userLogindServiceListener);
    }

    public void removeUserLoginServiceListener(UserLoginServiceListener userLogindServiceListener) {
        userLoginServiceListeners.remove(userLogindServiceListener);
    }

    private List<UserRegisterServiceListener> userRegisterServiceListeners = new ArrayList<>();

    public void addUserRegisterServiceListener(UserRegisterServiceListener userRegisterServiceListener) {
        userRegisterServiceListeners.add(userRegisterServiceListener);
    }

    public void removeUserRegisterServiceListener(UserRegisterServiceListener userRegisterServiceListener) {
        userRegisterServiceListeners.remove(userRegisterServiceListener);
    }


    private List<UserLogoutAndDeleteServiceListener> userLogoutAndDeleteServiceListeners = new ArrayList<>();

    public void addLogoutAndDeleteServiceListener(UserLogoutAndDeleteServiceListener logoutAndDeleteServiceListener) {
        userLogoutAndDeleteServiceListeners.add(logoutAndDeleteServiceListener);
    }

    public void removeLogoutAndDeleteServiceListener(UserLogoutAndDeleteServiceListener logoutAndDeleteServiceListener) {
        userLogoutAndDeleteServiceListeners.remove(logoutAndDeleteServiceListener);
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
        preferenceHelper = new UserPreferenceHelper(this);
        userLoginAndRegisterRepository = UserAccountRepository.getInstance(new UserAccountDataSource(this), preferenceHelper);
    }

    public void login(String username, String password,  String deviceID) {
        new LoginUserRESTAsyncTask(username, password, deviceID, userLoginAndRegisterRepository).execute();
    }

    public void register(String username, String password, String email, String deviceID) {
        new RegisterUserRESTAsyncTask(username, password, email, deviceID, userLoginAndRegisterRepository).execute();
    }

    public void logout() {
        new LogoutUserRESTAsyncTask(userLoginAndRegisterRepository).execute();
    }

//    public void delete(String userName) {
//        new DeleteUserRESTAsyncTask(userName, userLoginAndRegisterRepository).execute();
//    }

    public void delete() {
        new DeleteUserRESTAsyncTask(userLoginAndRegisterRepository).execute();
    }

    /**
     * Method called within login REST response evaluation
     *
     * @param result
     */
    public void evaluateLoginResult(Result result) {
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginOrRegisterResult(new LoggedInUserView(data.getUserName())));
            userLoginAndRegisterRepository.setLoggedInUser(data);
            onUserLoggedInSuccess();
        } else {
            RestError error = ((Result.Error) result).getError();
            loginResult.setValue(new LoginOrRegisterResult(error));
            onUserLoggedInFailure();
        }
    }

    /**
     * Method called within new user register REST response evaluation
     *
     * @param result
     */
    public void evaluateRegisterResult(Result result) {
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            registerResult.setValue(new LoginOrRegisterResult(new LoggedInUserView(data.getUserName())));
            userLoginAndRegisterRepository.setLoggedInUser(data);
            onUserRegisterSuccess();
        } else {
            RestError error = ((Result.Error) result).getError();
            registerResult.setValue(new LoginOrRegisterResult(error));
            onUserRegisterFailure();
        }
    }

    /**
     * Method called within logout REST response evaluation
     *
     * @param result
     */
    public void evaluateLogoutResult(Result result) {
        if (result instanceof Result.Success) {
            String data = ((Result.Success<String>) result).getData();
            logoutResult.setValue(new LogoutOrDeleteResult(data));
            userLoginAndRegisterRepository.setLoggedInUser(null);
            onUserLogoutSuccess();
        } else {
            RestError error = ((Result.Error) result).getError();
            logoutResult.setValue(new LogoutOrDeleteResult(error));
            onUserLogoutFailure();
        }
    }

    /**
     * Method called within delete user REST response evaluation
     *
     * @param result
     */
    public void evaluateDeleteResult(Result result) {
        if (result instanceof Result.Success) {
            String data = ((Result.Success<String>) result).getData();
            deleteResult.setValue(new LogoutOrDeleteResult(data));
            userLoginAndRegisterRepository.setLoggedInUser(null);
            onUserDeleteSuccess();
        } else {
            RestError error = ((Result.Error) result).getError();
            deleteResult.setValue(new LogoutOrDeleteResult(error));
            onUserDeleteFailure();
        }
    }

    /**
     * Returns currently loged in user or null, if user none is registered
     */
    public LoggedInUser getLoggedInUser() {
        return userLoginAndRegisterRepository.getLoggedInUser();
    }

    public boolean isUserLoggedIn() {
        return userLoginAndRegisterRepository.isLoggedIn();
    }


    // Fire methods for login/register/logout events

    // FireUp methods
    public void onUserRegisterSuccess() {
        for (UserRegisterServiceListener listener : userRegisterServiceListeners) {
            listener.onUserRegisterSuccess(registerResult.getValue());
        }
    }
    public void onUserRegisterFailure() {
        for (UserRegisterServiceListener listener : userRegisterServiceListeners) {
            listener.onUserRegisterFailure(registerResult.getValue());
        }
    }

    public void onUserLoggedInSuccess() {
        for (UserLoginServiceListener listener : userLoginServiceListeners) {
            listener.onUserLoggedInSuccess(loginResult.getValue());
        }
    }
    public void onUserLoggedInFailure() {
        for (UserLoginServiceListener listener : userLoginServiceListeners) {
            listener.onUserLoggedInFailure(loginResult.getValue());
        }
    }

    public void onUserLogoutSuccess() {
        for (UserLogoutAndDeleteServiceListener listener : userLogoutAndDeleteServiceListeners) {
            listener.onUserLogoutSuccess();
        }
    }

    public void onUserLogoutFailure() {
        for (UserLogoutAndDeleteServiceListener listener : userLogoutAndDeleteServiceListeners) {
            String errorDetail = (logoutResult.getValue().getError() != null)
                                 ? logoutResult.getValue().getError().getDetail()
                                 : getString(R.string.logout_failure);
            listener.onUserLogoutFailure(errorDetail);
        }
    }

    public void onUserDeleteSuccess() {
        for (UserLogoutAndDeleteServiceListener listener : userLogoutAndDeleteServiceListeners) {
            listener.onUserDeleteSuccess();
        }
    }

    public void onUserDeleteFailure() {
        for (UserLogoutAndDeleteServiceListener listener : userLogoutAndDeleteServiceListeners) {
            String errorDetail = (logoutResult.getValue().getError() != null)
                                 ? logoutResult.getValue().getError().getDetail()
                                 : getString(R.string.delete_user_failure);
            listener.onUserDeleteFailure(errorDetail);
        }
    }

}
