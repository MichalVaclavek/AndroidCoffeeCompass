package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteRepository;
import io.reactivex.Maybe;

/**
 * Model for creation of CoffeeSite in {@link cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity}
 * Validation of some obligatory CoffeeSite attributes.
 *
 * Also used as model when editing CoffeeSite saved in DB
 */
public class CoffeeSiteCreateModel extends ViewModel {

    private CoffeeSiteRepository coffeeSiteRepository;

    private final int SITE_NAME_LENGTH = 35;

    public CoffeeSiteCreateModel(@NonNull Application application) {
        super();
        coffeeSiteRepository = new CoffeeSiteRepository(CoffeeSiteDatabase.getDatabase(application.getApplicationContext()));
    }

    public Maybe<CoffeeSite> getCoffeeSiteById(long siteId) {
        return coffeeSiteRepository.getCoffeeSiteByIdMaybe(siteId);
    }

    public LiveData<CoffeeSite> getCoffeeSite(long siteId) {
        return coffeeSiteRepository.getCoffeeSiteById(siteId);
    }


    private final MutableLiveData<CoffeeSiteCreateFormState> coffeeSiteFormState = new MutableLiveData<>();

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
        } else {
            coffeeSiteFormState.setValue(new CoffeeSiteCreateFormState(true));
        }
    }

    // A placeholder CoffeeSite name validation check
    private boolean isCoffeeSiteNameValid(String coffeeSiteName) {
        return coffeeSiteName != null && coffeeSiteName.trim().length() >= 4 && coffeeSiteName.trim().length() <= SITE_NAME_LENGTH;
    }

    // A placeholder longitude validation check
    private boolean isLongitudeValid(String longitude) {
        try {
            float delka = Float.parseFloat(longitude);
            return delka >= -180 && delka <= 180;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    // A placeholder latitude validation check
    private boolean isLatitudeValid(String latitude) {
        try {
            float sirka = Float.parseFloat(latitude);
            return sirka >= -180 && sirka <= 180;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

}
