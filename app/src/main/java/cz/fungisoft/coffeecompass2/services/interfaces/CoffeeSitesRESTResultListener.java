package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;

/**
 * An observable interface to indicate new result
 * of the REST operation, whose return value is list of CoffeeSites.
 * It is used by respective CoffeeSiteWithUserAccountService ancestor,
 * which for example, loads CoffeeSites created by current user.
 * <p>
 * Usually called by AsyncTasks with Retrofit call with {@code List<CoffeeSite>}
 * as return value of REST call
 */
public interface CoffeeSitesRESTResultListener {

    /**
     *
     * @param oper identifier of REST operation which lead to call this method
     * @param result - success or error result of the operation. If success, then List<CoffeeSite> is returned in result = new Result.Success<>(coffeeSites);
     */
    void onCoffeeSitesReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result);
}
