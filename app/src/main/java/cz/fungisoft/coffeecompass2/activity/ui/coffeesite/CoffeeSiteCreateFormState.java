package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import androidx.annotation.Nullable;

/**
 * Data validation state of the create CoffeeSite form.
 * Attributes hold either null or R.string.id of the
 * error message.
 *
 * Contains validation for CoffeeSiteName, longitude
 * and latitude. Other fields within CoffeeSite create
 * form are selected from lists or other fixed
 * values option elememnts/View (like Chips etc.)
 */
public class CoffeeSiteCreateFormState {

    @Nullable
    private Integer coffeeSiteNameError;
    @Nullable
    private Integer longitudeError;
    @Nullable
    private Integer latitudeError;

    private boolean isDataValid;

    CoffeeSiteCreateFormState(@Nullable Integer coffeeSiteNameError, @Nullable Integer longitudeError, @Nullable Integer latitudeError) {
        this.coffeeSiteNameError = coffeeSiteNameError;
        this.longitudeError = longitudeError;
        this.latitudeError = latitudeError;
        this.isDataValid = false;
    }

    CoffeeSiteCreateFormState(boolean isDataValid) {
        this.coffeeSiteNameError = null;
        this.longitudeError = null;
        this.latitudeError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getCoffeeSiteNameError() {
        return coffeeSiteNameError;
    }

    @Nullable
    public Integer getLongitudeError() {
        return longitudeError;
    }

    @Nullable
    public Integer getLatitudeError() {
        return latitudeError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}
