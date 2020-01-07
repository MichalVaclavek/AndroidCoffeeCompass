package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

/**
 * ViewModel provider factory to instantiate LoginRegisterViewModel.
 * Required given LoginRegisterViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginRegisterViewModel.class)) {
            LoginRegisterViewModel loginViewModel = new LoginRegisterViewModel();
            return (T) loginViewModel;
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
