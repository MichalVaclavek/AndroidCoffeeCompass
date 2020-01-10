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
    private RestError error;

    public LogoutOrDeleteResult(@Nullable RestError error) {
        this.error = error;
    }

    public LogoutOrDeleteResult(@Nullable String userName) {
        this.userName = userName;
    }

    @Nullable
    public String getSuccess() {
        return userName;
    }

    @Nullable
    public RestError getError() {
        return error;
    }
}
