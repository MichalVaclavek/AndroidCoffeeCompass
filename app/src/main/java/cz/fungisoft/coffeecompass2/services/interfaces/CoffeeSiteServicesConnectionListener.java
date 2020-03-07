package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;

/**
 * Interface to indicate finished binding of the {@link CoffeeSiteWithUserAccountService}
 * ancestors.
 * Used by {@link CoffeeSiteServicesConnector}
 * and and implemented by respective Activity, which needs to know that the service is
 * already binded/connected.
 */
public interface CoffeeSiteServicesConnectionListener {

    void onCoffeeSiteServiceConnected();
}
