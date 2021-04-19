package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.CoffeeSiteCreateModel;

public class CoffeeSiteViewModelFactory implements ViewModelProvider.Factory {

    private Application application;

    public CoffeeSiteViewModelFactory(@NonNull Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CoffeeSiteCreateModel.class)) {
            CoffeeSiteCreateModel createCoffeeSiteViewModel = new CoffeeSiteCreateModel(application);
            return (T) createCoffeeSiteViewModel;
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
