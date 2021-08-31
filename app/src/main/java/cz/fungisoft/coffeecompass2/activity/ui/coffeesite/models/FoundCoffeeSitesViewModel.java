package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesFoundService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesFoundListener;

/**
 * Data Model to be hold by {@link FoundCoffeeSitesListActivity}.<br>
 * <p>
 * Collects following information:
 *
 * - all currently found Coffee sites on current location within given range
 * - new CoffeeSites
 * - old CoffeeSites to be displayed by the activity's RecyclerViewAdapter
 * <p>
 * Class is a singleton.
 */
public class FoundCoffeeSitesViewModel extends AndroidViewModel
                                       implements CoffeeSitesFoundListener {

    private static final String TAG = "FoundCoffeeSitesModel";

    private final WeakReference<AppCompatActivity> ownerActivity;

    /**
     * Actual list of CoffeeSites in the search range from current position of the equipment as
     * found in DB.
     */
    private LiveData<List<CoffeeSiteMovable>> foundCoffeeSites;

    /**
     * Private constructor, class is singleton. Only instance is returned by getInstance() method.
     *
     * @param ownerActivity owner Activity
     * @param sitesInRangeUpdateService service to return coffee sites in range
     */
    public FoundCoffeeSitesViewModel(@NonNull AppCompatActivity ownerActivity,
                                     @NonNull CoffeeSitesFoundService sitesInRangeUpdateService) {
        this(ownerActivity);
        foundCoffeeSites = sitesInRangeUpdateService.getFoundSites();
    }

    /**
     * Constructor ....
     *
     * @param application
     * @param sitesInRangeUpdateService
     */
    public FoundCoffeeSitesViewModel(@NonNull AppCompatActivity ownerActivity) {
        super(ownerActivity.getApplication());
        this.ownerActivity = new WeakReference<>(ownerActivity);
    }

    public void setCoffeeSitesInRangeFoundService(CoffeeSitesFoundService sitesInRangeUpdateService) {
        foundCoffeeSites = sitesInRangeUpdateService.getFoundSites();
        foundCoffeeSites.observe(ownerActivity.get(), new Observer<List<CoffeeSiteMovable>>() {
            @Override
            public void onChanged(@Nullable final List<CoffeeSiteMovable> coffeeSitesInRange) {
                // Process found CoffeeSites - leads to update list of new and gone CoffeeSites, see below
                processFoundCoffeeSites(coffeeSitesInRange);
            }
        });
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

        // Find oldSites, i.e. sites not included in the coffeeSites
        goneSitesOutOfRange.clear();
        goneSitesOutOfRange.addAll(currentSitesInRange);
        goneSitesOutOfRange.removeAll(coffeeSites);

        currentSitesInRange = coffeeSites;

        //Gets newSites as MutableLiveData, sorted
        Collections.sort(newSitesInRange);
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
