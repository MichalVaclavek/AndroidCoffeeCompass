package cz.fungisoft.coffeecompass2.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteImageServiceConnectionListener;

/**
 * Service Connector for CoffeeSiteImageService
 */
public class CoffeeSiteImageServiceConnector implements ServiceConnection {


    public CoffeeSiteImageServiceConnector() {}

    private List<CoffeeSiteImageServiceConnectionListener> connectionListenerList = new ArrayList<>();

    public void addCoffeeSiteImageServiceConnectionListener( CoffeeSiteImageServiceConnectionListener listener) {
        this.connectionListenerList.add(listener);
    }
    public void removeCoffeeSiteImageServiceConnectionListener(CoffeeSiteImageServiceConnectionListener listener) {
        this.connectionListenerList.remove(listener);
    }

    // To invoke the bound service, first make sure that this value
    // is not null.
    private CoffeeSiteImageService mBoundService;

    public CoffeeSiteImageService getCoffeeSiteImageService() {
        return mBoundService;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
        mBoundService = ((CoffeeSiteImageService.LocalBinder)service).getService();

        for (CoffeeSiteImageServiceConnectionListener listener : connectionListenerList) {
            listener.onCoffeeSiteImageServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

}
