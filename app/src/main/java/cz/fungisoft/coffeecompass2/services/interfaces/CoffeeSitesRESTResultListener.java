package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;

/**
 * An observable interface to indicate new result
 * of the REST operation, whose return value is list of CoffeeSites.
 * It is used by respective CoffeeSiteService, which for example, loads
 * CoffeeSites created by current user.

 * Usualy called by AsyncTasks with Retrofit call with {@code List<CoffeeSite>}
 * as return value of REST call
 */
public interface CoffeeSitesRESTResultListener {

    /**
     *
     * @param oper
     * @param result
     */
    void onCoffeeSitesReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result);
}
