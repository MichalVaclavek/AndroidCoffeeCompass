package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Class implementing this interface is capable to react to events,
 * that inform about change of current CoffeeSites in Range.<br>
 * Also used as a listener for CoffeeSitesInRangeUpdateService binding
 * listener.
 */
public interface CoffeeSitesInRangeUpdateListener {

    /**
     * Called, when there are new sites in range detected.
     *
     * @param newSitesInRange
     */
    void onNewSitesInRange(List<CoffeeSiteMovable> newSitesInRange);

    /**
     * Called, when there are new sites in range detected.
     *
     * @param error
     */
    void onNewSitesInRangeError(String error);

    /**
     * Called, when there are sites out of current range detected.
     *
     * @param goneSitesOutOfRange
     */
    void onSitesOutOfRange(List<CoffeeSiteMovable> goneSitesOutOfRange);

}
