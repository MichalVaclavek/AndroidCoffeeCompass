package cz.fungisoft.coffeecompass2.activity.interfaces.stats;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.entity.Statistics;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit interface for REST request to read basic statistics
 * about CoffeeSites from the server.
 * Used by {@link cz.fungisoft.coffeecompass2.asynctask.ReadStatsAsyncTask}.
 */
public interface StatisticsRESTInterface {

    String HOME_BASE_URL = BuildConfig.HOME_API_BASE_URL;

    /**
     * REST call for obtaining basic statistics about saved CoffeeSites.
     *
     * @return Statistics object with counts of all sites, new sites last week, today and all users.
     */
    @GET("home")
    Call<Statistics> getStatistics();
}
