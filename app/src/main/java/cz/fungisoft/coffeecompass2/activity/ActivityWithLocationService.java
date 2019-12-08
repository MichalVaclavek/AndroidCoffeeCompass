package cz.fungisoft.coffeecompass2.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import cz.fungisoft.coffeecompass2.services.LocationService;
import cz.fungisoft.coffeecompass2.services.LocationServiceConnector;

/**
 * Common parent of Activities, which invokes start and connection (Binding - calling bindService()
 * method) to LocationService on Activity's creation.
 * It calls doUnbindLocationService() when the activity is destroyed.
 *
 * The {@code LocationServiceConnector} is used for running and binding to LocationService.
 *
 * When the LocationService is really started and connected, it provides reference to that LocationService.
 */
public abstract class ActivityWithLocationService extends AppCompatActivity {

    private static final String TAG = "ActivityWithLocationSrv";

    protected static final double CHECK_DISTANCE_TIMER_DELAY = 0.5d;
    protected static final double CHECK_DISTANCE_TIMER_PERIOD = 1.0d;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbind;

    protected LocationService locationService;
    private LocationServiceConnector locationServiceConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindLocationService();
    }

    private void doBindLocationService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        locationServiceConnector = new LocationServiceConnector(this);
        if (bindService(new Intent(this, LocationService.class),
                locationServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.e(TAG, "Error: The requested 'LocationService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindLocationService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            unbindService(locationServiceConnector);
            mShouldUnbind = false;
        }
    }

    /**
     * To be called in {@code LocationServiceConnector} class when the {@code LocationService}
     * is really connected.
     */
    public void onLocationServiceConnected() {
        locationService = locationServiceConnector.getLocationService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindLocationService();
    }

}
