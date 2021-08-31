package cz.fungisoft.coffeecompass2.services.interfaces;


/**
 * Interface to define start and finished of coffee sites in range or in town searching operations.
 * Can be implemented by classes needed to know when such operations stars and finishes
 * like Activity to show or hide progress bar.
 */
public interface CoffeeSitesInRangeSearchOperationListener {

    /**
     * Called, when searching of sites in range starts.
     */
    void onStartSearchingSites();

    /**
     * Called, when searching of sites in range or in town finished.
     */
    void onSearchingSitesFinished();

    /**
     *
     * @param error
     */
    void onSearchingSitesError(String error);

}
