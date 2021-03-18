package cz.fungisoft.coffeecompass2.activity.ui.notification;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.fungisoft.coffeecompass2.R;

/**
 * This model is used to provide and validate list of towns entered by user for push notifications about
 * new CoffeeSites in these towns.<br>
 * Uses Google Geolocation API for validating name of the town and checks if every town in the selected list us unique.
 */
public class NotificationSubscriptionViewModel extends ViewModel {

    private static final String TAG = "NotificationViewModel";

    private final MutableLiveData<NotificationSubscriptionFormValidationState> notificationSubscriptionFormState = new MutableLiveData<>();

    /**
     * Currently being validated/selected town name
     */
    private String validatedTownName;

    /**
     * Holds info, if a user selected 'all_towns' check box
     */
    private boolean allTownsSelected = false;

    public boolean isAllTownsSelected() {
        return allTownsSelected;
    }

    /**
     * Already validated and selected town names
     */
    private final List<String> allValidatedTownNames = new ArrayList<>();

    public String getValidatedTownName() {
        return this.validatedTownName;
    }

    public List<String> getAllValidatedTownNames() {
        return allValidatedTownNames;
    }

    public LiveData<NotificationSubscriptionFormValidationState> getNotificationSubscriptionFormState() {
        return notificationSubscriptionFormState;
    }

    /**
     * Validates currently entered start of townName using GeoLocation API other already entered town names,
     * so all the names are different.
     *
     * @param context - required by  GeoLocation API to initialize
     * @param townName - town name to be validated using GeoLocation API
     */
    private boolean validateTownName(Context context, String townName) {
        this.validatedTownName = "";

        String city;
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(context, new Locale("cs"));

        try {
            Log.d(TAG, "Looking for address start ...");
            addresses = geocoder.getFromLocationName(townName, 5, 48.5, 12.0,
                    51.1,  18.9);
        } catch (IOException e) {
            Log.e(TAG, "Error looking for address: " + e.getMessage());
        }

        if (addresses != null && addresses.size() > 0) {
            Log.d(TAG, "Address found");
            for (Address address : addresses) {
                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = address.getLocality();
                city = (city == null) ? address.getSubLocality() : city;
                if (city == null) {
                    String adminArea = address.getAdminArea();
                    if (adminArea != null) {
                        String[] adminAreaSplit = adminArea.split(" ");
                        city = adminAreaSplit[adminAreaSplit.length - 1]; // get last part of Admin Area. probably relevant only for "Hlavni mesto Praha"
                    }
                }
                if (city == null || city.isEmpty()) {
                    city = address.getFeatureName();
                }
                if (!city.isEmpty() && city.contains(townName)
                    || (address.getFeatureName() != null && address.getFeatureName().contains(townName))) {
                    validatedTownName = townName;
                    return true;
                }
            }
        }
        return false;
   }

    /**
     * Validates currently selected townName or 'all_towns' flag. Previously selected town name are taken
     * int account during validation.
     *
     * @param context - required by Geolocation API to be created
     * @param townName - name of town to be validated. Can be empty, then allTownsSelected parameter is evaluated
     * @param allTownsSelected - flag to indicate, if a user selected 'all_towns' check box or not
     */
    public void townDataChanged(Context context, String townName, boolean allTownsSelected) {
        if (!townName.isEmpty()) { // we are checking town name only here
            if (!isTownNameValid(context, townName)) {
                notificationSubscriptionFormState.setValue(new NotificationSubscriptionFormValidationState(R.string.invalid_townname, null));
            } else if (isTownNameAlreadyUsed(townName)) {
                notificationSubscriptionFormState.setValue(new NotificationSubscriptionFormValidationState(null, R.string.town_name_already_used));
                } else {
                    notificationSubscriptionFormState.setValue(new NotificationSubscriptionFormValidationState(true));
                }
            return; // other parameters are not to be evaluated
        }

        // we are checking allTownsSelected flag only
        this.allTownsSelected = false;
        validatedTownName = "";

        if (allTownsSelected) {
            this.allTownsSelected = true;
            notificationSubscriptionFormState.setValue(new NotificationSubscriptionFormValidationState(true));
        } else if (allValidatedTownNames.size() > 0) {
            notificationSubscriptionFormState.setValue(new NotificationSubscriptionFormValidationState(true));
        } else {
            notificationSubscriptionFormState.setValue(new NotificationSubscriptionFormValidationState(false));
        }
    }

    /**
     * Process removing of the townName from the list of already validated town names
     *
     * @param townName - town name to be removed from list of already validated town names
     */
    public void townDataRemoved(String townName) {
        validatedTownName = "";
        allValidatedTownNames.remove(townName);
        if (allValidatedTownNames.size() > 0) {
            notificationSubscriptionFormState.setValue(new NotificationSubscriptionFormValidationState(true));
        } else {
            notificationSubscriptionFormState.setValue(new NotificationSubscriptionFormValidationState(false));
        }
    }

    /**
     * A placeholder for validation of the town name using Geolocation API to validate townName
    */
    private boolean isTownNameValid(Context context, String townName) {
        if (townName == null || townName.isEmpty()) {
            return false;
        }
        return validateTownName(context, townName);
    }

    /**
     * A placeholder for validation of town name against other already selected town names.
     */
    private boolean isTownNameAlreadyUsed(String townName) {
        if (townName == null || townName.isEmpty()) {
            return false;
        }
        if (allValidatedTownNames.contains(townName)) {
            return true;
        } else {
            allValidatedTownNames.add(townName);
            return false;
        }
    }
}
