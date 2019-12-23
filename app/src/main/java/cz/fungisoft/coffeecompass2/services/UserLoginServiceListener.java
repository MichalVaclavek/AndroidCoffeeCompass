package cz.fungisoft.coffeecompass2.services;


import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;

/**
 * Listener Interface to define events, which are fired by UserAccountService
 * after user login and when the UserAccountService is connected to
 * calling activity.
 */
public interface UserLoginServiceListener {

    void onUserLoggedInSuccess(LoginOrRegisterResult loginResult);

    void onUserLoggedInFailure(LoginOrRegisterResult loginResult);

    void onUserLoginServiceConnected();
}
