package cz.fungisoft.coffeecompass.entity;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import cz.fungisoft.coffeecompass.services.LocationService;

/**
 * CoffeeSite which is able to listen locationService changes
 * and update it's distance from current location accordingly.<br>
 * Class is also capable to register listeners for 'distance'
 * change event.
 */
public class CoffeeSiteMovable extends CoffeeSite implements PropertyChangeListener
{
    private static final String TAG = "CoffeeSiteMovable:";
    /**
     * Support for property change, 'distance' in this case.
     */
    private PropertyChangeSupport support;

    private static  LocationService locService;

    public void setLocationService(LocationService locationService) {
        this.locService = locationService;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
        Log.d(TAG, "Coffee Site objID: " + this + ". Coffee Site: " + getName() + ". Pocet posluchacu zmeny vzdalenosti: " + support.getPropertyChangeListeners().length);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
        Log.d(TAG, "Coffee Site objID: " + this + ". Coffee Site: " + getName() + ". Pocet posluchacu zmeny vzdalenosti: " + support.getPropertyChangeListeners().length);
    }

    public CoffeeSiteMovable() {
        super();
        support = new PropertyChangeSupport(this);
    }

    public CoffeeSiteMovable(int id, String name, long dist) {
        super(id, name, dist);
        support = new PropertyChangeSupport(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        long newDistance = locService.getDistanceFromCurrentLocation(getLatitude(), getLongitude());
        long currentDistance = getDistance();
        if (Math.abs(newDistance - currentDistance) >= ((Location)evt.getNewValue()).getAccuracy()/2) { // distance change is at least twice higher then current location accuracy
            setDistance(newDistance);
            support.firePropertyChange("distance", currentDistance, newDistance);
        }
    }


}
