package cz.fungisoft.coffeecompass2.services.interfaces;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;

/**
 * An observable interface to indicate new result
 * of the REST operation, whose return value is one CoffeeSite.
 * It is used by CoffeeSiteWithUserAccountService ancestor, which changes
 * status of one CoffeeSite
 * or which performs operations save, update, delete on one CoffeeSite.
 * Usually called by AsyncTasks with Retrofit call with CoffeeSite
 * as return value of REST call.
 */
public interface CoffeeSiteRESTResultListener {

    void onCoffeeSiteReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<CoffeeSite> result);
}
