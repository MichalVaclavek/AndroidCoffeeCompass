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
 * This model is used to keep and validate list of towns entered by user for push notifications about
 * new CoffeeSites in these towns in respective Form of {@link NewsSubscriptionActivity}<br>
 * Uses Google Geolocation API for validating name of the individual town and checks if every town
 * in the selected list is unique.
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
    private List<String> allValidatedTownNames = new ArrayList<>();

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
     * Validates currently entered start of townName using GeoLocation API.
     * Not exactly needed as the Places API used to search valid town names
     * as user enters characters in {@link TownNamesArrayAdapter}, is enough.<br>
     * Moreover, it could happen, that town name confirmed by Places API is not validated by
     * this Geolocation API validation (for example town 'Prace' is not validated).<br>
     * So, in currrent implementation, let the method return true still.
     *
     * @param context - required by  GeoLocation API to initialize
     * @param townName - town name to be validated using GeoLocation API
     */
    private boolean validateTownName(Context context, String townName) {
        return true;

//        this.validatedTownName = "";
//
//        String city;
//        Geocoder geocoder;
//        List<Address> addresses = null;
//        geocoder = new Geocoder(context, new Locale("cs"));
//
//        try {
//            Log.d(TAG, "Looking for address start ...");
//            addresses = geocoder.getFromLocationName(townName, 5, 48.5, 12.0,
//                    51.1,  18.9);
//        } catch (IOException e) {
//            Log.e(TAG, "Error looking for address: " + e.getMessage());
//        }
//
//        if (addresses != null && addresses.size() > 0) {
//            Log.d(TAG, "Address found");
//            for (Address address : addresses) {
//                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                city = address.getLocality();
//                city = (city == null) ? address.getSubLocality() : city;
//                if (city == null) {
//                    String adminArea = address.getAdminArea();
//                    if (adminArea != null) {
//                        String[] adminAreaSplit = adminArea.split(" ");
//                        city = adminAreaSplit[adminAreaSplit.length - 1]; // get last part of Admin Area. probably relevant only for "Hlavni mesto Praha"
//                    }
//                }
//                if (city == null || city.isEmpty()) {
//                    city = address.getFeatureName();
//                }
//                if (!city.isEmpty() && city.contains(townName)
//                    || (address.getFeatureName() != null && address.getFeatureName().contains(townName))) {
//                    validatedTownName = townName;
//                    return true;
//                }
//            }
//        }
//        return false;
   }

    /**
     * Validates currently selected townName or 'all_towns' flag or list of towns.
     * Previously entered town names are also taken into account during validation.
     *
     * @param context - required by Geolocation API to be created
     * @param townName - name of town to be validated. Can be empty, then allTownsSelected parameter is evaluated
     * @param allTownsSelected - flag to indicate, if a user selected 'all_towns' check box or not
     * @param validatedTownNames - list of already validated town names, usually from preference helper (not needed to validate again)
     */
    public void townDataChanged(Context context, String townName, boolean allTownsSelected, List<String> validatedTownNames) {
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

        // Validating list of town
        if (validatedTownNames != null) {
            this.allValidatedTownNames = validatedTownNames;
        }

        // we are checking allTownsSelected flag and this.allValidatedTownNames
        this.allTownsSelected = false;
        this.validatedTownName = "";

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
     * Clears data of the model before new usage
     */
    public void clearData() {
        this.allTownsSelected = false;
        this.validatedTownName = "";
        this.allValidatedTownNames.clear();
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
     * A placeholder for validation of town name against other already entered town names.
     */
    private boolean isTownNameAlreadyUsed(String townName) {
        if (townName == null || townName.isEmpty()) {
            return false;
        }
        if (allValidatedTownNames.contains(townName)) {
            return true;
        } else {
            allTownsSelected = false;
            allValidatedTownNames.add(townName);
            return false;
        }
    }
}
