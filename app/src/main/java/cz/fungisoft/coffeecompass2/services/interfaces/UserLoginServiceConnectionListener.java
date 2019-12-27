package cz.fungisoft.coffeecompass2.services.interfaces;

/**
 * To allow Activity to perform an action when the UserAccountService
 * is connected within Activity.
 */
public interface UserLoginServiceConnectionListener {

    void onUserLoginServiceConnected();
}
