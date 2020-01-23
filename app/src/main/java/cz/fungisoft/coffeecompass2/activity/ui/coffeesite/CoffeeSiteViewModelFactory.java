package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cz.fungisoft.coffeecompass2.activity.ui.login.LoginRegisterViewModel;

public class CoffeeSiteViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CoffeeSiteCreateModel.class)) {
            CoffeeSiteCreateModel createCoffeeSiteViewModel = new CoffeeSiteCreateModel();
            return (T) createCoffeeSiteViewModel;
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
