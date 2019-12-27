package cz.fungisoft.coffeecompass2.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import cz.fungisoft.coffeecompass2.services.interfaces.SitesInRangeUpdateListener;

/**
 * Connector for CoffeeSitesInRangeUpdateService
 */
public class CoffeeSitesInRangeUpdateServiceConnector implements ServiceConnection {

    private SitesInRangeUpdateListener callingActivity;

    public CoffeeSitesInRangeUpdateServiceConnector(SitesInRangeUpdateListener callingActivity) {
        this.callingActivity = callingActivity;
    }

    // To invoke the bound service, first make sure that this value
    // is not null.
    private CoffeeSitesInRangeUpdateService mBoundService;

    public CoffeeSitesInRangeUpdateService getSitesInRangeUpdateService() {
        return mBoundService;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBoundService = ((CoffeeSitesInRangeUpdateService.LocalBinder)service).getService();

        if (this.callingActivity != null) {
            this.callingActivity.onCoffeeSitesInRangeUpdateServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        // Because it is running in our same process, we should never
        // see this happen.
        mBoundService = null;
    }

}
