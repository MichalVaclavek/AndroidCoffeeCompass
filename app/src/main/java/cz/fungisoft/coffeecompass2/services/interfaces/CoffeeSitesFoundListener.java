package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;
import java.util.Map;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Interface to define methods called from process reading CoffeeSites in range or in the town.
 */
public interface CoffeeSitesFoundListener {

    default void onSitesInRangeFound(List<CoffeeSiteMovable> coffeeSiteMovables) {}
    default void onNumbersOfSitesInRangesFound(Map<String, Integer> numOfCoffeeSites) {}
}
