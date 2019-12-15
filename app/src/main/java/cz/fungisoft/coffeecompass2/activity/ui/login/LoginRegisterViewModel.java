package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Patterns;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.LoginAndRegisterRepository;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.ui.register.RegisterFormState;
import cz.fungisoft.coffeecompass2.asynctask.LoginUserRESTAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.RegisterUserRESTAsyncTask;

public class LoginRegisterViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();

    //private MutableLiveData<LoginOrRegisterResult> loginResult = new MutableLiveData<>();
    //private MutableLiveData<LoginOrRegisterResult> registerResult = new MutableLiveData<>();
   // private LoginAndRegisterRepository loginRepository;

//    LoginRegisterViewModel(LoginAndRegisterRepository loginRepository) {
//        this.loginRepository = loginRepository;
//    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

//    public LiveData<LoginOrRegisterResult> getLoginResult() {
//        return loginResult;
//    }

//    public LiveData<LoginOrRegisterResult> getRegisterResult() {
//        return registerResult;
//    }

//    public void login(String username, String password,  String deviceID) {
//        new LoginUserRESTAsyncTask(username, password, deviceID, loginRepository).execute();
//    }
//
//    public void register(String username, String password, String email, String deviceID) {
//        new RegisterUserRESTAsyncTask(username, password, email, deviceID, loginRepository).execute();
//    }

//    public void evaluateLoginResult(Result<LoggedInUser> result) {
//        if (result instanceof Result.Success) {
//            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
//            loginResult.setValue(new LoginOrRegisterResult(new LoggedInUserView(data.getUserName())));
//            loginRepository.setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
//        } else {
//            loginResult.setValue(new LoginOrRegisterResult(R.string.login_failed));
//        }
//    }

//    public void evaluateRegisterResult(Result<LoggedInUser> result) {
//        if (result instanceof Result.Success) {
//            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
//            registerResult.setValue(new LoginOrRegisterResult(new LoggedInUserView(data.getUserName())));
//            loginRepository.setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
//        } else {
//            registerResult.setValue(new LoginOrRegisterResult(R.string.login_failed));
//        }
//    }

    public void loginDataChanged(String username, String password) {

        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    public void registerDataChanged(String username, String email, String password) {

        if (!isUserNameValid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null, null));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password, null));
        } else if (!isEmailValid(email)) {
            registerFormState.setValue(new RegisterFormState(null, null, R.string.invalid_email));
        } else {
            registerFormState.setValue(new RegisterFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() >= 4;
    }

    // A placeholder email validation check
    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
