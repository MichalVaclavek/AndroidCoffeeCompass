package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import cz.fungisoft.coffeecompass2.R;

public class CoffeeSiteCreateModel extends ViewModel {

    private MutableLiveData<CoffeeSiteCreateFormState> coffeeSiteFormState = new MutableLiveData<>();

    public LiveData<CoffeeSiteCreateFormState> getCoffeeSiteFormState() {
        return coffeeSiteFormState;
    }

    public void coffeeSiteDataChanged(String coffeeSiteName, String longitude, String latitude) {

        if (!isCoffeeSiteNameValid(coffeeSiteName)) {
            coffeeSiteFormState.setValue(new CoffeeSiteCreateFormState(R.string.invalid_coffeesitename, null, null));
        } else if (!isLongitudeValid(longitude)) {
            coffeeSiteFormState.setValue(new CoffeeSiteCreateFormState(null, R.string.invalid_longitude, null));
        } else if (!isLatitudeValid(latitude)) {
            coffeeSiteFormState.setValue(new CoffeeSiteCreateFormState(null, null, R.string.invalid_latitude));
        } else{
            coffeeSiteFormState.setValue(new CoffeeSiteCreateFormState(true));
        }
    }

    // A placeholder CoffeeSite name validation check
    private boolean isCoffeeSiteNameValid(String coffeeSiteName) {
        return coffeeSiteName != null && coffeeSiteName.trim().length() >= 4;
    }

    // A placeholder longitude validation check
    private boolean isLongitudeValid(String longitude) {
        try {
            float delka = Float.valueOf(longitude);
            return delka >= -180 && delka <= 180;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    // A placeholder latitude validation check
    private boolean isLatitudeValid(String latitude) {
        try {
            float sirka = Float.valueOf(latitude);
            return sirka >= -180 && sirka <= 180;
        } catch (NumberFormatException ex) {
            return false;
        }
    }


}
