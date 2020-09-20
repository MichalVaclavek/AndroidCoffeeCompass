package cz.fungisoft.coffeecompass2.services.interfaces;


/**
 * Interface to define start and finished of coffee sites in range operations.
 * Can be implemented by classes needed to know when such operations stars and finishes
 * like Activity to show or hide progress bar.
 */
public interface CoffeeSitesInRangeSearchOperationListener {

    /**
     * Called, when searching of sites in range starts.
     */
    void onStartSearchingSitesInRange();

    /**
     * Called, when searching of sites in range finished.
     */
    void onSearchingSitesInRangeFinished();

    void onSearchingSitesInRangeError(String error);

}
