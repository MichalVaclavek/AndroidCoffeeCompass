package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * Interface to define methods called from process reading CoffeeSites in range
 */
public interface CoffeeSitesInRangeFoundListener {

    void onSitesInRangeFound(List<CoffeeSiteMovable> coffeeSiteMovables);
}
