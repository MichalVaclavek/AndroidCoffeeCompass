package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;

/**
 * An observable interface to indicate new result
 * of the REST operation, whose return value is Integer (usually number of CoffeeSites)
 * It is used by respective CoffeeSiteService, which for example, loads
 * number of CoffeeSites created by current user.
 * <br>
 * Usually called by AsyncTasks with Retrofit call with Integer
 * as return value of REST call.
 */
public interface CoffeeSiteNumbersRESTResultListener {

    void onNumberOfCoffeeSitesReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<Integer> result);
}
