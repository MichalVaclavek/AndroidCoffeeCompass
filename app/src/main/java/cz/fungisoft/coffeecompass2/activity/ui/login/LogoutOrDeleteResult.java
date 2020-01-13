package cz.fungisoft.coffeecompass2.activity.ui.login;

import androidx.annotation.Nullable;

import cz.fungisoft.coffeecompass2.activity.data.model.RestError;

/**
 * Logout or Delete result : success (user name) or error message.
 */
public class LogoutOrDeleteResult {

    @Nullable
    private String userName;
    @Nullable
    private String error;

    public LogoutOrDeleteResult(@Nullable String userName, @Nullable String error) {
        this.error = error;
        this.userName = userName;
    }

    @Nullable
    public String getSuccess() {
        return userName;
    }

    @Nullable
    public String getError() {
        return error;
    }
}
