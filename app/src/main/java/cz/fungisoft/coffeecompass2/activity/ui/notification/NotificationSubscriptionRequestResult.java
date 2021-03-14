package cz.fungisoft.coffeecompass2.activity.ui.notification;

import androidx.annotation.Nullable;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoggedInUserView;

/**
 * Push Notification subscription result : success (accepted) or error message.
 */
public class NotificationSubscriptionRequestResult {

    @Nullable
    private boolean accepted;

    @Nullable
    private Result.Error error;

    public NotificationSubscriptionRequestResult(@Nullable Result.Error error) {
        this.error = error;
    }

    public NotificationSubscriptionRequestResult(@Nullable boolean success) {
        this.accepted = success;
    }

    @Nullable
    public boolean getSuccess() {
        return accepted;
    }

    @Nullable
    public Result.Error getError() {
        return error;
    }
}
