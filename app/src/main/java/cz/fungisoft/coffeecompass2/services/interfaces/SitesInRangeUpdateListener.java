package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Class implementing this interface is capable to react to events,
 * that inform about change of current CoffeeSites in Range.<br>
 * Also used as a listener for CoffeeSitesInRangeUpdateService binding
 * listener
 */
public interface SitesInRangeUpdateListener {

    void onNewSitesInRange(List<CoffeeSiteMovable> newSitesInRange);
    void onSitesOutOfRange(List<CoffeeSiteMovable> goneSitesOutOfRange);

    void onCoffeeSitesInRangeUpdateServiceConnected();
}
