package cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;

/**
* Listener interface implemented by Activities to listen async results
* of operations performed by CoffeeSiteStatusChangeService
*/
public interface CoffeeSiteServiceStatusOperationsListener {

    default void onCoffeeSiteActivated(CoffeeSite activeCoffeeSite, String error) {}
    default void onCoffeeSiteDeactivated(CoffeeSite inactiveCoffeeSite, String error) {}
    default void onCoffeeSiteCanceled(CoffeeSite canceledCoffeeSite, String error) {}
}
