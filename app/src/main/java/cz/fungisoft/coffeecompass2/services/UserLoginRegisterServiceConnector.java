package cz.fungisoft.coffeecompass2.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Class to connect UserLoginAndRegisterService to calling activity
 */
public class UserLoginRegisterServiceConnector implements ServiceConnection {

    private UserLoginServiceListener callingLoginActivity;

    public UserLoginRegisterServiceConnector(UserLoginServiceListener callingActivity) {
        this.callingLoginActivity = callingActivity;
    }

    private UserRegisterServiceListener callingRegisterActivity;

    public UserLoginRegisterServiceConnector(UserRegisterServiceListener callingActivity) {
        this.callingRegisterActivity = callingActivity;
    }

    // To invoke the bound service, first make sure that this value
    // is not null.
    private UserLoginAndRegisterService mBoundService;

    public UserLoginAndRegisterService getUserLoginService() {
        return mBoundService;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
        mBoundService = ((UserLoginAndRegisterService.LocalBinder)service).getService();

        if (this.callingLoginActivity != null) {
            this.callingLoginActivity.onUserLoginServiceConnected();
        }

        if (this.callingRegisterActivity != null) {
            this.callingRegisterActivity.onUserRegisterServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
}
