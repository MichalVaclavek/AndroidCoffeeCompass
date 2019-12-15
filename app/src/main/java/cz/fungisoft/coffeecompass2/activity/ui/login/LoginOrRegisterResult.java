package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.support.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
public class LoginOrRegisterResult {

    @Nullable
    private LoggedInUserView success;
    @Nullable
    private Integer error;

    public LoginOrRegisterResult(@Nullable Integer error) {
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
    public Integer getError() {
        return error;
    }
}
