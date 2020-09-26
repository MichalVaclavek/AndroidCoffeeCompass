package cz.fungisoft.coffeecompass2.entity;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import cz.fungisoft.coffeecompass2.services.LocationService;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * CoffeeSite which is able to listen locationService changes
 * and update it's distance from current location accordingly.<br>
 * Another Class can also to register listeners for 'distance'
 * change event of this CoffeeSiteMovable class
 */
public class CoffeeSiteMovable extends CoffeeSite implements PropertyChangeListener, Parcelable
{
    private static final String TAG = "CoffeeSiteMovable";
    /**
     * Support for property change, 'distance' in this case.
     */
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private static  LocationService locService;


    protected CoffeeSiteMovable(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CoffeeSiteMovable> CREATOR = new Creator<CoffeeSiteMovable>() {
        @Override
        public CoffeeSiteMovable createFromParcel(Parcel in) {
            return new CoffeeSiteMovable(in);
        }

        @Override
        public CoffeeSiteMovable[] newArray(int size) {
            return new CoffeeSiteMovable[size];
        }
    };

    public void setLocationService(LocationService locationService) {
        this.locService = locationService;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.addPropertyChangeListener(pcl);
        Log.d(TAG, "Coffee Site objID: " + this + ". Coffee Site: " + getName() + ". Pocet posluchacu zmeny vzdalenosti: " + changeSupport.getPropertyChangeListeners().length);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.removePropertyChangeListener(pcl);
        Log.d(TAG, "Coffee Site objID: " + this + ". Coffee Site: " + getName() + ". Pocet posluchacu zmeny vzdalenosti: " + changeSupport.getPropertyChangeListeners().length);
    }

    public CoffeeSiteMovable() {
        super();
    }

    /**
     * Constructor capable to create instance of CoffeeSiteMovable
     * from its CoffeeSite ancestor instance.
     * @param coffeeSite
     */
    public CoffeeSiteMovable(CoffeeSite coffeeSite) {
        super();
        this.setId(coffeeSite.getId());
        this.setName(coffeeSite.getName());
        this.setDistance(coffeeSite.getDistance());

        this.setLatitude(coffeeSite.getLatitude());
        this.setLongitude(coffeeSite.getLongitude());

        this.setMainImageURL(coffeeSite.getMainImageURL());
        this.setMainImageFileName(coffeeSite.getMainImageFileName());

        this.setCena(coffeeSite.getCena());

        this.setUliceCP(coffeeSite.getUliceCP());
        this.setMesto(coffeeSite.getMesto());
        this.setTypPodniku(coffeeSite.getTypPodniku());
        this.setTypLokality(coffeeSite.getTypLokality());
        this.setStatusZarizeni(coffeeSite.getStatusZarizeni());
        this.setHodnoceni(coffeeSite.getHodnoceni());
        this.setCreatedByUserName(coffeeSite.getCreatedByUserName());
        this.setCreatedOnString(coffeeSite.getCreatedOnString());

        this.setUvodniKoment(coffeeSite.getUvodniKoment());

        this.setOteviraciDobaDny(coffeeSite.getOteviraciDobaDny());
        this.setOteviraciDobaHod(coffeeSite.getOteviraciDobaHod());

        this.setCupTypes(coffeeSite.getCupTypes());
        this.setNextToMachineTypes(coffeeSite.getNextToMachineTypes());
        this.setCoffeeSorts(coffeeSite.getCoffeeSorts());
        this.setOtherOffers(coffeeSite.getOtherOffers());
    }

    /**
     * Constructor capable to create instance of CoffeeSiteMovable
     * from its CoffeeSite ancestor instance and counts distance from search location
     * @param coffeeSite
     */
    public CoffeeSiteMovable(CoffeeSite coffeeSite, LatLng searchLocationFrom) {

        this(coffeeSite);
        this.setDistance(Utils.countDistanceMetersFromSearchPoint(coffeeSite.getLatitude(), coffeeSite.getLongitude(), searchLocationFrom.latitude, searchLocationFrom.longitude));
    }

    public CoffeeSiteMovable(int id, String name, long dist) {
        super(id, name, dist);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (locService != null) {
            long newDistance = locService.getDistanceFromCurrentLocation(getLatitude(), getLongitude());
            long currentDistance = getDistance();
            if (Math.abs(newDistance - currentDistance) >= ((Location) evt.getNewValue()).getAccuracy() / 2) { // distance change is at least twice higher then current location accuracy
                setDistance(newDistance);
                changeSupport.firePropertyChange("distance", currentDistance, newDistance);
            }
        }
    }

    public boolean isLocationServiceAssigned() {
        return locService != null;
    }
}
