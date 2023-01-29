package cz.fungisoft.coffeecompass2.services.interfaces;


/**
 * Interface to define start and finish of coffee sites in range operations.
 * Can be implemented by classes needed to know when such operations stars and finishes
 * like Activity to show or hide progress bar.
 */
public interface CoffeeSitesInRangeSearchOperationListener {

    /**
     * Called, when searching of sites in range starts.
     */
    void onStartSearchingSites();

    /**
     * Called, when searching of number of coffee sites in different range is finished.
     */
    void onSearchingSitesFinished(int numOfSitesInRanges);

    /**
     *
     * @param error
     */
    void onSearchingSitesError(String error);
}
