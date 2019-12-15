package cz.fungisoft.coffeecompass2.services;

import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;

/**
 * Listener Interface to define events, which are fired by UserLoginAndRegisterService
 * after new user registration and when the UserLoginAndRegisterService is connected to
 * calling activity.
 */
public interface UserRegisterServiceListener {

    void onUserRegisterSuccess(LoginOrRegisterResult registerResult);

    void onUserRegisterFailure(LoginOrRegisterResult registerResult);

    void onUserRegisterServiceConnected();
}
