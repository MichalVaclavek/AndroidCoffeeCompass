package cz.fungisoft.coffeecompass2.services.interfaces;

import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;

/**
 * An observable interface to indicate new result of the CoffeeSites upload REST operation,
 * whose return value is list of CoffeeSites uploaded and returned by server.
 * It is used by {@link cz.fungisoft.coffeecompass2.services.CoffeeSiteCUDOperationsService} as the listener.
 * <p>
 * Usually called by {@link cz.fungisoft.coffeecompass2.asynctask.coffeesite.UploadCoffeeSitesAsyncTask} with Retrofit call with {@code List<CoffeeSite>}
 * as return value of REST call.
 */
public interface CoffeeSitesUploadRESTResultListener {

    /**
     *
     * @param oper identifier of REST operation which lead to call this method
     * @param result - success or error result of the operation. If success, then List<CoffeeSite> is returned in result = new Result.Success<>(coffeeSites);
     */
    void onCoffeeSitesUploadedAndReturned(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper oper, Result<List<CoffeeSite>> result);
}
