package cz.fungisoft.coffeecompass2.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;

/**
 * Common service connector for all services extended from CoffeeSiteWithUserAccountService
 */
public class CoffeeSiteServicesConnector<T extends CoffeeSiteWithUserAccountService> implements ServiceConnection {

    private List<CoffeeSiteServicesConnectionListener> connectionListenerList = new ArrayList<>();

    public void addCoffeeSiteServiceConnectionListener(CoffeeSiteServicesConnectionListener listener) {
        this.connectionListenerList.add(listener);
    }
    public void removeCoffeeSiteServiceConnectionListener(CoffeeSiteServicesConnectionListener listener) {
        this.connectionListenerList.remove(listener);
    }

    // To invoke the bound service, first make sure that this value
    // is not null.
    private T mBoundService;

    public T getCoffeeSiteService() {
        return mBoundService;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
        mBoundService = (T) ((T.LocalBinder)service).getService();

        for (CoffeeSiteServicesConnectionListener listener : connectionListenerList) {
            listener.onCoffeeSiteServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
}
