package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeFoundService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeFoundListener;

/**
 * Data Model to be hold by {@link FoundCoffeeSitesListActivity}.
 * Represents all found coffee sites to be displayed by the activity's RecyclerViewAdapter
 */
public class FoundCoffeeSitesViewModel extends AndroidViewModel implements CoffeeSitesInRangeFoundListener {

    private static final String TAG = "FoundCoffeeSitesModel";

    /**
     * Actual list of CoffeeSites in the search range from current position of the equipment as
     * found in DB.
     */
    private final LiveData<List<CoffeeSiteMovable>> foundCoffeeSitesInDB;

    public LiveData<List<CoffeeSiteMovable>> getFoundCoffeeSitesInDb() {
        return foundCoffeeSitesInDB;
    }

    /**
     * Actual list of CoffeeSites in the search range from current position of the equipment
     * as returned from server (or from DB after conversion from List<CoffeeSite> to List<CoffeeSiteMovable>)
     */
    private List<CoffeeSiteMovable> currentSitesInRange = new ArrayList<>();

    /**
     * CoffeeSites, which have come into current Range.
     * (i.e. the sites which were not included in the previous actual list of Sites in Range, but
     * are included in the current sites in Range. i.e. "plus" difference between current
     * and old sites in Range)
     */
    private final List<CoffeeSiteMovable> newSitesInRange = new ArrayList<>();

    /**
     * CoffeeSites, which have left the current Range.
     * (i.e. the sites which were included in the previous actual list of Sites in Range, but
     * not included in the current sites in Range. i.e. "minus" difference between current
     * and old sites in Range)
     */
    private final List<CoffeeSiteMovable> goneSitesOutOfRange = new ArrayList<>();

    /**
     * CoffeeSites, which have come into current Range.
     * (i.e. the sites which were not included in the previous actual list of Sites in Range, but
     * are included in the current sites in Range. i.e. "plus" difference between current
     * and old sites in Range)
     */
    private final MutableLiveData<List<CoffeeSiteMovable>> newSitesInRangeMutable = new MutableLiveData<>();
    public MutableLiveData<List<CoffeeSiteMovable>> getNewSitesInRange() {
        return newSitesInRangeMutable;
    }


    /**
     * CoffeeSites, which have left the current Range.
     * (i.e. the sites which were included in the previous actual list of Sites in Range, but
     * not included in the current sites in Range. i.e. "minus" difference between current
     * and old sites in Range)
     */
    private final MutableLiveData<List<CoffeeSiteMovable>> goneSitesOutOfRangeMutable = new MutableLiveData<>();
    public MutableLiveData<List<CoffeeSiteMovable>> getGoneSitesOutOfRange() {
        return goneSitesOutOfRangeMutable;
    }


    public FoundCoffeeSitesViewModel(@NonNull Application application,
                                     @NonNull CoffeeSitesInRangeFoundService sitesInRangeUpdateService) {
        super(application);
        /**
         * Service to return coffee sites in range
         */
        WeakReference<CoffeeSitesInRangeFoundService> sitesInRangeUpdateService1 = new WeakReference<>(sitesInRangeUpdateService);
        sitesInRangeUpdateService1.get().addSitesInRangeFoundListener(this);
        foundCoffeeSitesInDB = sitesInRangeUpdateService1.get().getFoundSites();
    }


    /**
     * Receives found coffeeSites in range and compares it with current list. Founds new and old coffeeSites.
     *
     * @param coffeeSites
     * @return
     */
    public FoundCoffeeSitesViewModel processFoundCoffeeSites(List<CoffeeSiteMovable> coffeeSites) {

        newSitesInRange.clear();
        newSitesInRange.addAll(coffeeSites);
        newSitesInRange.removeAll(currentSitesInRange);

        // 1. Find oldSites, i.e. sites not included in the coffeeSites
        goneSitesOutOfRange.clear();
        goneSitesOutOfRange.addAll(currentSitesInRange);
        goneSitesOutOfRange.removeAll(coffeeSites);

        currentSitesInRange = coffeeSites;

        newSitesInRangeMutable.setValue(newSitesInRange);
        goneSitesOutOfRangeMutable.setValue(goneSitesOutOfRange);

        return this;
    }

    /**
     * Found CoffeeSites returned from server.
     *
     * @param coffeeSites
     */
    @Override
    public void onSitesInRangeFound(List<CoffeeSiteMovable> coffeeSites) {
        processFoundCoffeeSites(coffeeSites);
    }

}
