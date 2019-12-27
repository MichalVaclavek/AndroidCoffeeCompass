package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;

/**
 * Listener Interface to define events, which are fired by UserAccountService
 * after new user registration.
 */
public interface UserRegisterServiceListener {

    void onUserRegisterSuccess(LoginOrRegisterResult registerResult);
    void onUserRegisterFailure(LoginOrRegisterResult registerResult);
}
