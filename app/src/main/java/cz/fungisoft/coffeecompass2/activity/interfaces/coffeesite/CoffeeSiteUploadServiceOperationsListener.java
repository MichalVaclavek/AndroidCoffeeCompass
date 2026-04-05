package cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite;

import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;

/**
 * Listener interface implemented by Activities/Services to listen Results
 * of upload CoffeeSites operation performed by {@link CoffeeSiteCUDOperationsService}
 */
public interface CoffeeSiteUploadServiceOperationsListener {

    /**
     * on upload of CoffeeSites created/updated when Offline
     *
     * @param coffeeSites - CoffeeSites uploaded and returned from server. Can be null in case of upload failure
     * @param error - empty, if upload successful, otherwise error description
     */
    default void onCoffeeSitesUploaded(List<CoffeeSite> coffeeSites, String error) {}
}
