package cz.fungisoft.coffeecompass2.services.interfaces;

/**
 * Interface to declare actions to be performed after connection to UserAccountService
 * finished
 */
public interface UserAccountServiceConnectionListener {

    void onUserAccountServiceConnected();
}
