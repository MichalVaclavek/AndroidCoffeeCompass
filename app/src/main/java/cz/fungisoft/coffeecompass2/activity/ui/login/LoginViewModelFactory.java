package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import cz.fungisoft.coffeecompass2.activity.data.LoginAndRegisterDataSource;
import cz.fungisoft.coffeecompass2.activity.data.LoginAndRegisterRepository;
import cz.fungisoft.coffeecompass2.activity.data.model.UserPreferenceHelper;

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
            //LoginAndRegisterDataSource loginDataSource = new LoginAndRegisterDataSource();
            //LoginRegisterViewModel loginViewModel = new LoginRegisterViewModel(LoginAndRegisterRepository.getInstance(loginDataSource, preferenceHelper));
            LoginRegisterViewModel loginViewModel = new LoginRegisterViewModel();
            //loginDataSource.setLoginViewModel(loginViewModel);
            return (T) loginViewModel;
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
