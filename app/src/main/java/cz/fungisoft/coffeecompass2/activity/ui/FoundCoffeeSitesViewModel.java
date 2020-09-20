package cz.fungisoft.coffeecompass2.activity.ui;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeFoundService;
import cz.fungisoft.coffeecompass2.services.LocationService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeFoundListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeUpdateListener;

/**
 * Data Model to be hold by {@link FoundCoffeeSitesListActivity}.
 * Represents all found coffee sites to be displayd by the activity's RecyclerViewAdapter
 */
public class FoundCoffeeSitesViewModel extends AndroidViewModel implements CoffeeSitesInRangeFoundListener {


    private static final String TAG = "FoundCoffeeSitesModel";

    /**
     * Actual list of CoffeeSites in the search range from current position of the equipment as
     * found in DB.
     */
    private LiveData<List<CoffeeSite>> foundCoffeeSitesInDB;

    /**
     * Actual list of CoffeeSites in the search range from current position of the equipment
     * as returned from server (or from DB after conversion from List<CoffeeSite> to List<CoffeeSiteMovable>)
     */
    private List<CoffeeSiteMovable> currentSitesInRange = new ArrayList<>();


    public LiveData<List<CoffeeSite>> getFoundCoffeeSites() {
        return foundCoffeeSitesInDB;
    }

    /**
     * CoffeeSites, which have come into current Range.
     * (i.e. the sites which were not included in the previous actual list of Sites in Range, but
     * are included in the current sites in Range. i.e. "plus" difference between current
     * and old sites in Range)
     */
    private List<CoffeeSiteMovable> newSitesInRange = new ArrayList<>();
    public List<CoffeeSiteMovable> getNewSitesInRange() {
        return newSitesInRange;
    }


    /**
     * CoffeeSites, which have left the current Range.
     * (i.e. the sites which were included in the previous actual list of Sites in Range, but
     * not included in the current sites in Range. i.e. "minus" difference between current
     * and old sites in Range)
     */
    private List<CoffeeSiteMovable> goneSitesOutOfRange = new ArrayList<>();
    public List<CoffeeSiteMovable> getGoneSitesOutOfRange() {
        return goneSitesOutOfRange;
    }


    /**
     * Location service needed to update coffeeSitesMovable listeners
     * LocationService is provided by CoffeeSitesInRangeFoundService
     */
    private LocationService locationService;

    /**
     * Service to return coffee sites in range
     */
    private CoffeeSitesInRangeFoundService sitesInRangeUpdateService;

    private List<CoffeeSitesInRangeUpdateListener>  sitesInRangeUpdateListeners = new ArrayList<>();

    public void addSitesInRangeUpdateListener(CoffeeSitesInRangeUpdateListener sitesInRangeUpdateListener) {
        sitesInRangeUpdateListeners.add(sitesInRangeUpdateListener);
        Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + sitesInRangeUpdateListeners.size());
    }

    public void removeSitesInRangeUpdateListener(CoffeeSitesInRangeUpdateListener sitesInRangeUpdateListener) {
        sitesInRangeUpdateListeners.remove(sitesInRangeUpdateListener);
        Log.d(TAG,  ". Pocet posluchacu zmeny CoffeeSites in Range: " + sitesInRangeUpdateListeners.size());
    }

    private FoundCoffeeSitesListActivity mParentActivity;

    public FoundCoffeeSitesViewModel(@NonNull Application application,
                                     @NonNull LocationService locationService,
                                     @NonNull CoffeeSitesInRangeFoundService sitesInRangeUpdateService) {
        super(application);
        this.locationService = locationService;
        this.sitesInRangeUpdateService = sitesInRangeUpdateService;
        this.sitesInRangeUpdateService.addSitesInRangeFoundListener(this);
        foundCoffeeSitesInDB = this.sitesInRangeUpdateService.getFoundSites();
    }


    /**
     * Receives found coffeeSites in range and compares it with current list. Founds new and old coffeeSites.
     * @param coffeeSites
     * @return
     */
    public FoundCoffeeSitesViewModel processFoundCoffeeSites(List<CoffeeSiteMovable> coffeeSites) {
        newSitesInRange.clear();
        newSitesInRange.addAll(coffeeSites);
        newSitesInRange.removeAll(currentSitesInRange);

        for (CoffeeSiteMovable csm : newSitesInRange) {
            // First add new CoffeeSites as locationService listeners
            csm.setLocationService(locationService);
            locationService.addPropertyChangeListener(csm);
        }

        // 1. Find oldSites, i.e. sites not included in the coffeeSites
        goneSitesOutOfRange.clear();
        goneSitesOutOfRange.addAll(currentSitesInRange);
        goneSitesOutOfRange.removeAll(coffeeSites);

        for (CoffeeSiteMovable csm : goneSitesOutOfRange) {
            locationService.removePropertyChangeListener(csm);
        }

        currentSitesInRange = coffeeSites;

        return this;
    }


    @Override
    public void onSitesInRangeFound(List<CoffeeSiteMovable> coffeeSites) {
        // 1. Find newSites
        processFoundCoffeeSites(coffeeSites);

        for (CoffeeSitesInRangeUpdateListener listener : sitesInRangeUpdateListeners) {

            if (goneSitesOutOfRange.size() > 0) {
                listener.onSitesOutOfRange(goneSitesOutOfRange);
            }
            if (newSitesInRange.size() > 0) {
                listener.onNewSitesInRange(newSitesInRange);
            }
        }
    }

}
