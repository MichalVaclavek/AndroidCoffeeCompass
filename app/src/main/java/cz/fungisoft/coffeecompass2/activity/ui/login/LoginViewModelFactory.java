package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

/**
 * ViewModel provider factory to instantiate LoginRegisterViewModel.
 * Required given LoginRegisterViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    //private UserPreferenceHelper preferenceHelper;

//    public LoginViewModelFactory(UserPreferenceHelper preferenceHelper) {
//        this.preferenceHelper = preferenceHelper;
//    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginRegisterViewModel.class)) {
            //UserAccountDataSource loginDataSource = new UserAccountDataSource();
            //LoginRegisterViewModel loginViewModel = new LoginRegisterViewModel(UserAccountRepository.getInstance(loginDataSource, preferenceHelper));
            LoginRegisterViewModel loginViewModel = new LoginRegisterViewModel();
            //loginDataSource.setLoginViewModel(loginViewModel);
            return (T) loginViewModel;
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
