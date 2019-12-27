package cz.fungisoft.coffeecompass2.services.interfaces;


/**
 * Listener Interface to define events, which are fired by UserAccountService
 * after new user registration.
 */
public interface UserLogoutAndDeleteServiceListener {

    void onUserLogoutSuccess();
    void onUserLogoutFailure(String errorMessage);

    void onUserDeleteSuccess();
    void onUserDeleteFailure(String errorMessage);
}
