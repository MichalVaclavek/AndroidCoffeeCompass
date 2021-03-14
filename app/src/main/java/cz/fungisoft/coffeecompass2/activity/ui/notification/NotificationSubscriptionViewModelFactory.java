package cz.fungisoft.coffeecompass2.activity.ui.notification;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.CoffeeSiteCreateModel;

public class NotificationSubscriptionViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotificationSubscriptionViewModel.class)) {
            NotificationSubscriptionViewModel notificationSubscriptionViewModel = new NotificationSubscriptionViewModel();
            return (T) notificationSubscriptionViewModel;
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
