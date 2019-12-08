package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import cz.fungisoft.coffeecompass2.activity.data.LoginDataSource;
import cz.fungisoft.coffeecompass2.activity.data.LoginRepository;
import cz.fungisoft.coffeecompass2.activity.data.model.UserPreferenceHelper;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private UserPreferenceHelper preferenceHelper;

    public LoginViewModelFactory(UserPreferenceHelper preferenceHelper) {
        this.preferenceHelper = preferenceHelper;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            LoginDataSource loginDataSource = new LoginDataSource();
            LoginViewModel loginViewModel = new LoginViewModel(LoginRepository.getInstance(loginDataSource, preferenceHelper));
            loginDataSource.setLoginViewModel(loginViewModel);
            return (T) loginViewModel;
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
