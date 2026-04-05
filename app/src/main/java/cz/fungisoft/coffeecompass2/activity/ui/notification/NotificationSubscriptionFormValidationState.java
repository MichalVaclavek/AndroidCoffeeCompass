package cz.fungisoft.coffeecompass2.activity.ui.notification;

import androidx.annotation.Nullable;

/**
 * Data validation state of the Notification subscription town entering form.
 *
 * @see NewsSubscriptionActivity
 * @see NotificationSubscriptionViewModel
 */
class NotificationSubscriptionFormValidationState {

    @Nullable
    private final Integer townNameError;

    @Nullable
    private final Integer townNameAlreadyUsedError;

    private final boolean isDataValid;


    NotificationSubscriptionFormValidationState(@Nullable Integer townNameError, @Nullable Integer townNameAlreadyUsedError) {
        this.townNameError = townNameError;
        this.townNameAlreadyUsedError = townNameAlreadyUsedError;
        this.isDataValid = false;
    }


    NotificationSubscriptionFormValidationState(boolean isDataValid) {
        this.townNameError = null;
        this.townNameAlreadyUsedError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getTownNameError() {
        return townNameError;
    }

    @Nullable
    Integer getNameAlreadyUsedError() {
        return townNameAlreadyUsedError;
    }


    boolean isDataValid() {
        return isDataValid;
    }
}
