package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.activity.data.Result;

/**
 * Interface to define method, which is called by CoffeeSiteEntitiesService,
 * when all CoffeeSiteEntities are loaded from server.
 * Can be implemented by Activity, which wants to be informed about
 * this event. The Entities themselves are not passed to this
 * interface/method.
 */
public interface CoffeeSiteEntitiesLoadRESTResultListener {

    void onCoffeeSiteEntitiesLoaded(Result<Boolean> result);
}
