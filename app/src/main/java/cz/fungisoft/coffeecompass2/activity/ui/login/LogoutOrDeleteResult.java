package cz.fungisoft.coffeecompass2.activity.ui.login;

import androidx.annotation.Nullable;

/**
 * Logout or Delete result : success (user name) or error message.
 */
public class LogoutOrDeleteResult {

    @Nullable
    private final String userName;
    @Nullable
    private final String error;

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
