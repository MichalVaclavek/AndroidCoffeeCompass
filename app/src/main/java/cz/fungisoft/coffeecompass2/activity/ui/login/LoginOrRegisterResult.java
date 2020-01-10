package cz.fungisoft.coffeecompass2.activity.ui.login;

import androidx.annotation.Nullable;

import cz.fungisoft.coffeecompass2.activity.data.model.RestError;

/**
 * Authentication result : success (user details) or error message.
 */
public class LoginOrRegisterResult {

    @Nullable
    private LoggedInUserView success;
    @Nullable
    private RestError error;

    public LoginOrRegisterResult(@Nullable RestError error) {
        this.error = error;
    }

    public LoginOrRegisterResult(@Nullable LoggedInUserView success) {
        this.success = success;
    }

    @Nullable
    public LoggedInUserView getSuccess() {
        return success;
    }

    @Nullable
    public RestError getError() {
        return error;
    }
}
