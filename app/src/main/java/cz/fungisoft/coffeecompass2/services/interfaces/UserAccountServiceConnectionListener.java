package cz.fungisoft.coffeecompass2.services.interfaces;

/**
 * Interface to declare actions to be performed after connection to UserAccountService
 * is finished.
 */
public interface UserAccountServiceConnectionListener {

    void onUserAccountServiceConnected();
}
