package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;

/**
 * An observable interface to indicate new result
 * of the REST operation, whose return value is Long (usually CoffeeSite id)

 * Usually called by AsyncTasks with Retrofit call with Long
 * as return value of REST call.
 */
public interface CoffeeSiteIdRESTResultListener {

    void onCoffeeSitesIdReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<Long> result);
}
