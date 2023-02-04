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

    private Map<String, Integer> numOfCoffeeSites = new HashMap<>();

    private int searchDistance = 0;

    public void setSearchDistance(int searchDistance) {
        this.searchDistance = searchDistance;
        this.sitesInRangeUpdateService.get().setCurrentSearchRange(this.searchDistance);
        if (this.numOfCoffeeSites != null) {
            Integer numberOfSites = this.numOfCoffeeSites.get(String.valueOf(this.searchDistance));
            if (numberOfSites != null) {
                foundNumberOfCoffeeSites.setValue(this.numOfCoffeeSites.get(String.valueOf(this.searchDistance)));
            }
        }
    }

    /**
     * Main attribute returned by this Model.
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
            LiveData<Integer> foundNumberCoffeeSitesFromDB = this.sitesInRangeUpdateService.get().getNumberOfFoundSites();
            foundNumberCoffeeSitesFromDB.observe(ownerActivity.get(), new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable final Integer numOfCoffeeSitesInRange) {
                    // Process found number of CoffeeSites
                    numOfCoffeeSites.put(String.valueOf(searchDistance), numOfCoffeeSitesInRange);
                    foundNumberOfCoffeeSites.setValue(numOfCoffeeSitesInRange);
                }
            });
        }
    }

    public void setCurrentLocationAndSearchDistance(LatLng currentLocation, int searchDistance, List<Integer> allRanges, String coffeeSort) {
        this.searchDistance = searchDistance;
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
        foundNumberOfCoffeeSites.setValue(this.numOfCoffeeSites.get(String.valueOf(this.searchDistance)));
    }
}
