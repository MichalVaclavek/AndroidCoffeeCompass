package cz.fungisoft.coffeecompass2.services.interfaces;

/**
 * Interface to indicate finished binding of the CoffeeSiteEntitiesService.
 * Used by CoffeeSiteEntitiesServiceConnector and and implemented
 * by respective Activity, which needs to know that the service is
 * already binded/connected.
 */
public interface CoffeeSiteEntitiesServiceConnectionListener {

    void onCoffeeSiteEntitiesServiceConnected();
}
