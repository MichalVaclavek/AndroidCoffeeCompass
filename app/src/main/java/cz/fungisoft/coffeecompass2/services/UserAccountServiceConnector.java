package cz.fungisoft.coffeecompass2.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLogoutAndDeleteServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLogoutAndDeleteServiceListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserRegisterServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserRegisterServiceListener;

/**
 * Class to connect UserAccountService to calling activity
 */
public class UserAccountServiceConnector implements ServiceConnection {

    private UserLoginServiceConnectionListener callingLoginActivity;

    public UserAccountServiceConnector(UserLoginServiceConnectionListener callingActivity) {
        this.callingLoginActivity = callingActivity;
    }

    private CoffeeSiteService callingService;

    public UserAccountServiceConnector(CoffeeSiteService callingService) {
        this.callingService = callingService;
    }


    private UserRegisterServiceConnectionListener callingRegisterActivity;

    public UserAccountServiceConnector(UserRegisterServiceConnectionListener callingActivity) {
        this.callingRegisterActivity = callingActivity;
    }

    private UserLogoutAndDeleteServiceConnectionListener callingUserDataViewActivity;

    public UserAccountServiceConnector(UserLogoutAndDeleteServiceConnectionListener callingUserDataViewActivity) {
        this.callingUserDataViewActivity = callingUserDataViewActivity;
    }

    // To invoke the bound service, first make sure that this value
    // is not null.
    private UserAccountService mBoundService;

    public UserAccountService getUserLoginService() {
        return mBoundService;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
        mBoundService = ((UserAccountService.LocalBinder)service).getService();

        if (this.callingLoginActivity != null) {
            this.callingLoginActivity.onUserLoginServiceConnected();
        }
        if (this.callingRegisterActivity != null) {
            this.callingRegisterActivity.onUserRegisterServiceConnected();
        }
        if (this.callingUserDataViewActivity != null) {
            this.callingUserDataViewActivity.onLogoutAndDeleteServiceConnected();
        }

        if (this.callingService != null) {
            this.callingService.onUserLoginServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
}
