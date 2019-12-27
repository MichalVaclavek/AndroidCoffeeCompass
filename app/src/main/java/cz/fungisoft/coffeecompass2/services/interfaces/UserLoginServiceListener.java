package cz.fungisoft.coffeecompass2.services.interfaces;


import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;

/**
 * Listener Interface to define events, which are fired by UserAccountService
 * after user login is finished.
 */
public interface UserLoginServiceListener {

    /**
     * Called by UserAccountService after the login action was evaluated as successful.
     * Listenrs, usualy Activity, can take action on the successful
     * login event.
     *
     * @param loginResult
     */
    void onUserLoggedInSuccess(LoginOrRegisterResult loginResult);

    void onUserLoggedInFailure(LoginOrRegisterResult loginResult);
}
