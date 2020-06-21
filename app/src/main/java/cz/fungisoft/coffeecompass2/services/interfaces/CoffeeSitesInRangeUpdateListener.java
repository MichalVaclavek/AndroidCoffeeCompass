package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Class implementing this interface is capable to react to events,
 * that inform about change of current CoffeeSites in Range.<br>
 * Also used as a listener for CoffeeSitesInRangeUpdateService binding
 * listener
 */
public interface CoffeeSitesInRangeUpdateListener {

    /**
     * Called, when searching of sites in range starts.
     *
     * @param newSitesInRange
     */
    void onStartSearchingSitesInRange();

    /**
     * Called, when searching of sites in range finished.
     *
     * @param newSitesInRange
     */
    void onSearchingSitesInRangeFinished();

    /**
     * Called, when one new CoffeeSite in current searching within range is read.
     *
     * @param numberOfSitesAlreadyRead number of CoffeeSites already read within reading list of CoffeeSites in range
     */
    void onNextSiteInRangeRead(int numberOfSitesAlreadyRead);

    /**
     * Called, when there are new sites in range detected.
     *
     * @param newSitesInRange
     */
    void onNewSitesInRange(List<CoffeeSiteMovable> newSitesInRange);

    /**
     * Called, when there are new sites in range detected.
     *
     * @param newSitesInRange
     */
    void onNewSitesInRangeError(String error);

    /**
     * Called, when there are sites out of current range detected.
     *
     * @param newSitesInRange
     */
    void onSitesOutOfRange(List<CoffeeSiteMovable> goneSitesOutOfRange);

    void onCoffeeSitesInRangeUpdateServiceConnected();
}
