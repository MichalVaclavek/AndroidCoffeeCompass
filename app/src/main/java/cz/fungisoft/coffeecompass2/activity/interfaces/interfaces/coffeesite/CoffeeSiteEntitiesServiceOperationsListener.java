package cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite;


/**
 * Listener interface implemented by Activities to listen async results
 * of operations performed by {@link cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesService}
 */
public interface CoffeeSiteEntitiesServiceOperationsListener {

    // In most cases probably Not needed to implement as all the CoffeeSitesEntities
    // are saved by CoffeeSiteEntitiesRepository and Activity
    // need not to react to finis the load action
    default void onCoffeeSiteEntitiesLoaded(boolean result) {}
}
