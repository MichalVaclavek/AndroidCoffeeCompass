package cz.fungisoft.coffeecompass2.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;

/**
 * Class to connect UserAccountService to calling activity or service
 */
public class UserAccountServiceConnector implements ServiceConnection {

    private CoffeeSiteService callingCoffeeSiteService;

    public UserAccountServiceConnector(CoffeeSiteService callingService) {
        this.callingCoffeeSiteService = callingService;
    }

    /**
     * Another service listeniong to connect to UserAccountService
     * //TODO should be done using Listener ...
     */
    private CoffeeSiteImageService callingCoffeeSiteImageService;

    public UserAccountServiceConnector(CoffeeSiteImageService callingCoffeeSiteImageService) {
        this.callingCoffeeSiteImageService = callingCoffeeSiteImageService;
    }

    private List<UserAccountServiceConnectionListener> connectionListenerList = new ArrayList<>();

    public void addUserAccountServiceConnectionListener( UserAccountServiceConnectionListener listener) {
        this.connectionListenerList.add(listener);
    }
    public void removeUserAccountServiceConnectionListener(UserAccountServiceConnectionListener listener) {
        this.connectionListenerList.remove(listener);
    }

    public UserAccountServiceConnector() {
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

        for (UserAccountServiceConnectionListener listener : connectionListenerList) {
            listener.onUserAccountServiceConnected();
        }

        if (this.callingCoffeeSiteService != null) {
            this.callingCoffeeSiteService.onUserAccountServiceConnected();
        }
        if (this.callingCoffeeSiteImageService != null) {
            this.callingCoffeeSiteImageService.onUserAccountServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

}
