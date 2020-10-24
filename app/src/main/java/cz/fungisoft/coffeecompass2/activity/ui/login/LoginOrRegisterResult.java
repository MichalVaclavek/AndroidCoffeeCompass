package cz.fungisoft.coffeecompass2.activity.ui.login;

import androidx.annotation.Nullable;

import cz.fungisoft.coffeecompass2.activity.data.Result;

/**
 * Authentication result : success (user details) or error message.
 */
public class LoginOrRegisterResult {

    @Nullable
    private LoggedInUserView success;
    @Nullable
    private Result.Error error;

    public LoginOrRegisterResult(@Nullable Result.Error error) {
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
    public Result.Error getError() {
        return error;
    }
}
