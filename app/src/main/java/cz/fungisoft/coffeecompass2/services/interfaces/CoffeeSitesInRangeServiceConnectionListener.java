package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeFoundService;

/**
 * Interface to indicate finished binding of the {@link CoffeeSitesInRangeFoundService}
 * ancestors.
 */
public interface CoffeeSitesInRangeServiceConnectionListener {

    void onCoffeeSitesInRangeUpdateServiceConnected();
}
