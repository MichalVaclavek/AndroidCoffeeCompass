package cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteCUDOperationsService;

/**
 * Listener interface implemented by Activities to listen async results
 * of operations performed by {@link CoffeeSiteCUDOperationsService}
 * If everything is fine with operation, than CoffeeSite param
 * of every method is not null and error is empty.
 * In case of problem, CoffeeSite param is null and error
 * contains description of error.
 */
public interface CoffeeSiteServiceCUDOperationsListener {

    default void onCoffeeSiteSaved(CoffeeSite savedCoffeeSite, String error) {}
    default void onCoffeeSiteUpdated(CoffeeSite updatedCoffeeSite, String error) {}
    default void onCoffeeSiteDeleted(long deletedCoffeeSiteId, String error) {}
}
