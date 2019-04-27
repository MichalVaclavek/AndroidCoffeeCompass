package cz.fungisoft.coffeecompass2.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;

/**
 * A connector which connects to LocationService and informs calling/parrent {@code ActivityWithLocationService},
 * when the LocationService is really connected and ready to use.
 */
public class LocationServiceConnector implements ServiceConnection {

    private ActivityWithLocationService callingActivity;

    public LocationServiceConnector(ActivityWithLocationService callingActivity) {
        this.callingActivity = callingActivity;
    }

    // To invoke the bound service, first make sure that this value
    // is not null.
    private LocationService mBoundService;

    public LocationService getLocationService() {
        return mBoundService;
    }

    /**
     * Called when the LocationService is really connected.
     * Informs parent Activity about finished connection by calling it's
     * method on onLocationServiceConnected()
     *
     * @param name
     * @param service
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
        mBoundService = ((LocationService.LocalBinder)service).getService();

        this.callingActivity.onLocationServiceConnected();
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
