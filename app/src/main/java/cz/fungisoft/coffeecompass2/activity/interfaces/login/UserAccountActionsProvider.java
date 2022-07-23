package cz.fungisoft.coffeecompass2.activity.interfaces.login;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.JwtUserToken;

/**
 * Interface to define local actions to be performed after
 * remote user account actions (login, logout, register, deleteUser and refreshToken)
 * are finished within REST request.
 * It is expected, that some Service class, intended to perform
 * user account related actions, is implementing such
 * interface.
 */
public interface UserAccountActionsProvider {

    LoggedInUser getLoggedInUser();

    void evaluateLoginResult(Result result);
    void evaluateRegisterResult(Result result);
    void evaluateLogoutResult(Result result);
    void clearLoggedInUser();
    void evaluateDeleteResult(Result result);

    String getAccessToken();
    String getAccessTokenType();
    boolean isAccessTokenExpired();

    String getRefreshToken();
    JwtUserToken refreshTokenSync();
}
