package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;
import java.util.Map;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Interface to define methods called from process reading CoffeeSites in range
 */
public interface CoffeeSitesFoundFromServerResultListener {

    void onSitesInRangeReturnedFromServer(List<CoffeeSiteMovable> coffeeSiteMovables);
    default void onNumberOfSitesInRangesReturnedFromServer(Map<String, Integer> numOfCoffeeSitesInDistances) {}
    void onSitesInRangeReturnedFromServerError(String error);
}
