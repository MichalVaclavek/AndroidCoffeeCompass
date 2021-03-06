package cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite;

import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;

/**
 * Listener interface implemented by Activities/Services to listen async results
 * of operations performed by {@link CoffeeSiteLoadOperationsService}
 */
public interface CoffeeSiteLoadServiceOperationsListener {

    default void onCoffeeSiteLoaded(CoffeeSite coffeeSite, String error) {}
    default void onCoffeeSitesFromRangeLoaded(List<CoffeeSite> coffeeSites, String error) {}
    default void onCoffeeSiteListFromUserLoaded(List<CoffeeSite> coffeeSites, String error) {}

    default void onCoffeeSiteListFromLoggedInUserLoaded(List<CoffeeSite> coffeeSites, String error) {}
    default void onCoffeeSiteFirstPageFromLoggedInUserLoaded(CoffeeSitePageEnvelope coffeeSitesPage, String error) {}
    default void onCoffeeSiteNextPageFromLoggedInUserLoaded(CoffeeSitePageEnvelope coffeeSitesPage, String error) {}

    default void onNumberOfCoffeeSiteFromLoggedInUserLoaded(int coffeeSitesNumber, String error) {}

    default void onAllCoffeeSitesLoaded(List<CoffeeSite> coffeeSites, String error) {}
}
