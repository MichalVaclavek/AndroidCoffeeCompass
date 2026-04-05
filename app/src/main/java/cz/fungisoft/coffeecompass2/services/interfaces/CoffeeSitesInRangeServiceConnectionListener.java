package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.services.CoffeeSitesFoundService;

/**
 * Interface to indicate finished binding of the {@link CoffeeSitesFoundService}
 * ancestors.
 */
public interface CoffeeSitesInRangeServiceConnectionListener {

    void onCoffeeSitesInRangeUpdateServiceConnected();
}
