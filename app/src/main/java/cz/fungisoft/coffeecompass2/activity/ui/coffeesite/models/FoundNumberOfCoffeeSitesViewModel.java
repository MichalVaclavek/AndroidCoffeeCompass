package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.fungisoft.coffeecompass2.services.CoffeeSitesFoundService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesFoundListener;

/**
 * Data Model to be hold by {@link cz.fungisoft.coffeecompass2.activity.MainActivityActivity}.<br>
 * Model returns number of coffee sites in the currently selected distance from search point.
 * <p>
 * Class is a singleton.
 */
public class FoundNumberOfCoffeeSitesViewModel extends AndroidViewModel
                                               implements CoffeeSitesFoundListener {

    private static final String TAG = "FoundNumCoffeeSitesModel";

    private final WeakReference<AppCompatActivity> ownerActivity;

    private WeakReference<CoffeeSitesFoundService> sitesInRangeUpdateService;

    private int currentSearchDistance = 0;

    public void setCurrentSearchDistance(int currentSearchDistance) {
        this.currentSearchDistance = currentSearchDistance;
        this.sitesInRangeUpdateService.get().setCurrentSearchRange(this.currentSearchDistance);
        if (this.numOfCoffeeSites != null) {
            Integer numberOfSites = this.numOfCoffeeSites.get(String.valueOf(this.currentSearchDistance));
            if (numberOfSites != null) {
                foundNumberOfCoffeeSites.setValue(this.numOfCoffeeSites.get(String.valueOf(this.currentSearchDistance)));
            }
        }
    }

    /**
     * Main internal data structure to hold info about search distance (String)
     * and number of coffee sites in that distances.
     */
    private Map<String, Integer> numOfCoffeeSites = new HashMap<>();

    /**
     * Main "LiveData" attribute returned by this Model.
     * Actual number of CoffeeSites in the current search range from current position.
     */
    private final MutableLiveData<Integer> foundNumberOfCoffeeSites = new MutableLiveData<>();

    public LiveData<Integer> getFoundNumberOfCoffeeSites() {
        return foundNumberOfCoffeeSites;
    }

    private MutableLiveData<Map<String, Integer>> numOfAllCoffeeSites = new MutableLiveData<>();

    public LiveData<Map<String, Integer>> getAllFoundNumbersOfCoffeeSites() {
        return numOfAllCoffeeSites;
    }

    /**
     * Constructor ....
     *
     * @param application
     * @param sitesInRangeUpdateService
     */
    public FoundNumberOfCoffeeSitesViewModel(@NonNull AppCompatActivity ownerActivity) {
        super(ownerActivity.getApplication());
        this.ownerActivity = new WeakReference<>(ownerActivity);
    }

    public void setCoffeeSitesInRangeFoundService(CoffeeSitesFoundService sitesInRangeUpdateService) {
        this.sitesInRangeUpdateService = new WeakReference<>(sitesInRangeUpdateService);
        if (this.sitesInRangeUpdateService.get() != null) {
            this.sitesInRangeUpdateService.get().getAllNumberOfFoundSitesInRanges().observe(ownerActivity.get(), new Observer<Map<String, LiveData<Integer>>>() {
                @Override
                public void onChanged(@Nullable final Map<String, LiveData<Integer>> numOfCoffeeSitesInRange) {
                    // Process found number of CoffeeSites in all search ranges
                    if (numOfCoffeeSitesInRange != null) {
                        for (Map.Entry<String, LiveData<Integer>> e : numOfCoffeeSitesInRange.entrySet()) {
                            e.getValue().observe(ownerActivity.get(), new Observer<Integer>() {
                                @Override
                                public void onChanged(@Nullable final Integer numOfCoffeeSitesInRange) {
                                    // Process found number of CoffeeSites
                                    numOfCoffeeSites.put(e.getKey(), numOfCoffeeSitesInRange);
                                }
                            });
                        }
                        numOfAllCoffeeSites.setValue(numOfCoffeeSites);
                    }
                }
            });
        }
    }

    public void setCurrentLocationAndSearchDistance(LatLng currentLocation, int searchDistance, List<Integer> allRanges, String coffeeSort) {
        this.currentSearchDistance = searchDistance;
        if (this.sitesInRangeUpdateService != null) {
            this.sitesInRangeUpdateService.get().requestUpdateOfCoffeeSitesNumberInRanges(currentLocation, searchDistance, allRanges, coffeeSort);
        }
    }

    /**
     * Found CoffeeSites returned from service (and server).
     *
     * @param coffeeSites
     */
    @Override
    public void onNumbersOfSitesInRangesFound(Map<String, Integer> numOfCoffeeSites) {
        this.numOfCoffeeSites = numOfCoffeeSites;
        numOfAllCoffeeSites.setValue(numOfCoffeeSites);
        foundNumberOfCoffeeSites.setValue(this.numOfCoffeeSites.get(String.valueOf(this.currentSearchDistance)));
    }
}
