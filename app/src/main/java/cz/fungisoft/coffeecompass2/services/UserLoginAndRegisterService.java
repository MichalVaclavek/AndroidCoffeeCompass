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
import cz.fungisoft.coffeecompass2.activity.data.LoginAndRegisterDataSource;
import cz.fungisoft.coffeecompass2.activity.data.LoginAndRegisterRepository;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.UserPreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoggedInUserView;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;
import cz.fungisoft.coffeecompass2.asynctask.LoginUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.RegisterUserRESTAsyncTask;

/**
 * Service to run user login and register Async tasks and to
 * provide info about currently logged-in user to other Activities, if requested.
 */
public class UserLoginAndRegisterService extends Service {

    private String TAG = "UserLoginRegister service";

    // Repository to check if a user is logen in
    private LoginAndRegisterRepository userLoginAndRegisterRepository;

    private UserPreferenceHelper preferenceHelper;

    private MutableLiveData<LoginOrRegisterResult> loginResult = new MutableLiveData<>();
    private MutableLiveData<LoginOrRegisterResult> registerResult = new MutableLiveData<>();

    public LiveData<LoginOrRegisterResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<LoginOrRegisterResult> getRegisterResult() {
        return registerResult;
    }

    private List<UserLoginServiceListener> userLoginServiceListeners = new ArrayList<>();

    public void addUserLoginServiceListener(UserLoginServiceListener userLogindServiceListener) {
        userLoginServiceListeners.add(userLogindServiceListener);
        //Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + userLoginServiceListeners.size());
    }

    public void removeUserLoginServiceListener(UserLoginServiceListener userLogindServiceListener) {
        userLoginServiceListeners.remove(userLogindServiceListener);
        //Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + userLoginServiceListeners.size());
    }
    private List<UserRegisterServiceListener> userRegisterServiceListeners = new ArrayList<>();

    public void addUserRegisterServiceListener(UserRegisterServiceListener userRegisterServiceListener) {
        userRegisterServiceListeners.add(userRegisterServiceListener);
        //Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + userLoginServiceListeners.size());
    }

    public void removeUserRegisterServiceListener(UserRegisterServiceListener userRegisterServiceListener) {
        userRegisterServiceListeners.remove(userRegisterServiceListener);
        //Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + userLoginServiceListeners.size());
    }


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        UserLoginAndRegisterService getService() {
            return UserLoginAndRegisterService.this;
        }
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new UserLoginAndRegisterService.LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceHelper = new UserPreferenceHelper(this);
        userLoginAndRegisterRepository = LoginAndRegisterRepository.getInstance(new LoginAndRegisterDataSource(this), preferenceHelper);
    }

    public void login(String username, String password,  String deviceID) {
        new LoginUserRESTAsyncTask(username, password, deviceID, userLoginAndRegisterRepository).execute();
    }

    public void register(String username, String password, String email, String deviceID) {
        new RegisterUserRESTAsyncTask(username, password, email, deviceID, userLoginAndRegisterRepository).execute();
    }

    public void evaluateLoginResult(Result<LoggedInUser> result) {
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginOrRegisterResult(new LoggedInUserView(data.getUserName())));
            userLoginAndRegisterRepository.setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
            onUserLoggedInSuccess();
        } else {
            loginResult.setValue(new LoginOrRegisterResult(R.string.login_failed));
            onUserLoggedInFailure();
        }
    }

    public void evaluateRegisterResult(Result<LoggedInUser> result) {
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            registerResult.setValue(new LoginOrRegisterResult(new LoggedInUserView(data.getUserName())));
            userLoginAndRegisterRepository.setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
            onUserRegisterSuccess();
        } else {
            registerResult.setValue(new LoginOrRegisterResult(R.string.login_failed));
            onUserRegisterFailure();
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

    public void onUserLoggedOut() {
        for (UserLoginServiceListener listener : userLoginServiceListeners) {
            listener.onUserLoggedOut();
        }
    }

}
