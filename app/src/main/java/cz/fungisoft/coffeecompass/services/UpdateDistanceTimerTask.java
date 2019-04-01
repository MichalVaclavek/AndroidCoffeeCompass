package cz.fungisoft.coffeecompass.services;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;

import cz.fungisoft.coffeecompass.activity.ActivityWithLocationService;

/**
 * TimerTask and Timer to invoke regular update of the TextViews for showing
 * distance attribute, which needs to be updated according current location.
 *
 * So, it uses LocationService and ActivityWithLocationService which contains
 * "distance" TextView to be updated.
 */
public class UpdateDistanceTimerTask extends TimerTask {

    private static final String TAG = "Timer task";

    private ActivityWithLocationService locationServiceActivity;

    // location whose distance to current location is to be updated
    private LatLng locationToCountDistanceTo;

    private LocationService locService;

    private Timer timer;
    private int delay, period;

    public boolean isRunning() {
        return running;
    }

    private boolean running = false;

    private int viewPosition;

    /**
     * Constructor for TimerTask.
     *
     * @param lsa ActivityWithLocationService which contain "distance" TextView to be updated.
     * @param viewPosition id or position of the "distance" TextView within 'lsa' (ActivityWithLocationService)
     * @param locationToCount coordinates whose distance from current position is to be counted and shown in a "distance" TextView
     * @param locService LocationService capable to count distance between 'locationToCount' and current location
     */
    public UpdateDistanceTimerTask(ActivityWithLocationService lsa, int viewPosition, LatLng locationToCount, LocationService locService) {

        this.locationServiceActivity = lsa;
        this.viewPosition = viewPosition;
        this.locationToCountDistanceTo = locationToCount;
        this.locService = locService;
//        this.timer = timer;
        timer = new Timer("Distance_Timer_for_position_" + viewPosition);

    }

    public void startTimerTask( int delay, int period) {
        this.delay = delay;
        this.period = period;

        if (!running) {
            timer.schedule(this, this.delay, this.period);
            running = true;
        }
        Log.d("Timer task", "Timer task started: " + timer.toString());
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        this.cancel();
    }

    public void run() {
        Log.d(TAG, timer.toString() + " run(). Location service: " + this.locService.toString() );
        try {
            updateActivityDistanceView();
//            Log.d(TAG, timer.toString() + " run(). Location service distance: " + this.locService.getDistanceFromCurrentLocation(locationToCountDistance.latitude, locationToCountDistance.longitude) );
        } catch (Exception e)
        {
            Log.e(TAG, "Error running timer task " + timer.toString() + ". Exception " + e.getMessage());
            stopTimerTask();
        }
    }

    private void updateActivityDistanceView() {
        locationServiceActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Stuff that updates the UI
                locationServiceActivity.updateDistanceTextViewAndOrModel(viewPosition, locService.getDistanceFromCurrentLocation(locationToCountDistanceTo.latitude, locationToCountDistanceTo.longitude));
            }
        });
    }

}
