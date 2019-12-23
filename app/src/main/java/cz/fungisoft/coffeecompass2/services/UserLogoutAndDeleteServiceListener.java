package cz.fungisoft.coffeecompass2.services;

import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;

/**
 * Listener Interface to define events, which are fired by UserAccountService
 * after new user registration and when the UserAccountService is connected to
 * calling activity.
 */
public interface UserLogoutAndDeleteServiceListener {

    void onUserLogoutSuccess();
    void onUserLogoutFailure(String errorMessage);

    void onUserDeleteSuccess();
    void onUserDeleteFailure(String errorMessage);

    void onLogoutAndDeleteServiceConnected();
}
