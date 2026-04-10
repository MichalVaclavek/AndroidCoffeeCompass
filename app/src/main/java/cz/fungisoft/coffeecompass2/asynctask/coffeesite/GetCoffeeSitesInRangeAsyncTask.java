package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesFoundFromServerResultListener;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * REST caller to obtain all CoffeeSites within current distance range.
 * <p>
 * Replaces the former {@code AsyncTask}-based implementation.
 * Since Retrofit's {@link Call#enqueue(Callback)} is already asynchronous
 * (network I/O on a background thread, callbacks on the main thread),
 * there is no need for an additional {@code AsyncTask} wrapper.
 * <p>
 * The public API ({@link #execute()}) is kept intentionally so that callers
 * require minimal changes.
 */
public class GetCoffeeSitesInRangeAsyncTask {

    private static final String TAG = "GetSitesInRangeAsyncT";
    private static final String ACTIVE_RECORD_STATUS = "ACTIVE";

    /**
     * A Service or component which invokes this REST caller and receives results.
     */
    private final CoffeeSitesFoundFromServerResultListener callingService;

    private final double latFrom;
    private final double longFrom;
    private final int range;

    public GetCoffeeSitesInRangeAsyncTask(CoffeeSitesFoundFromServerResultListener parentService,
                                          double latFrom, double longFrom, int range, String coffeeSort) {
        this.callingService = parentService;
        this.latFrom = latFrom;
        this.longFrom = longFrom;
        this.range = range;
    }

    /**
     * Starts the asynchronous REST call. Results are delivered via the
     * {@link CoffeeSitesFoundFromServerResultListener} callback on the main thread.
     * <p>
     * This method can be called from any thread (including the main thread)
     * because {@link Call#enqueue(Callback)} handles threading internally.
     */
    public void execute() {
        Log.i(TAG, "start");

        CoffeeSiteRESTInterface api = RetrofitClientProvider.getInstance()
                .getRetrofit(CoffeeSiteRESTInterface.COFFEESITE_API_PUBLIC_SEARCH_URL)
                .create(CoffeeSiteRESTInterface.class);

        Call<List<CoffeeSite>> call = api.getCoffeeSitesInRange(
                this.latFrom,
                this.longFrom,
                this.range,
                ACTIVE_RECORD_STATUS);

        Log.i(TAG, "start call");

        call.enqueue(new Callback<>() {
            private String describeCoffeeSite(CoffeeSite coffeeSite) {
                if (coffeeSite == null) {
                    return "null";
                }

                int cupTypesCount = coffeeSite.getCupTypes() != null ? coffeeSite.getCupTypes().size() : -1;
                int coffeeSortsCount = coffeeSite.getCoffeeSorts() != null ? coffeeSite.getCoffeeSorts().size() : -1;

                return "id=" + coffeeSite.getId()
                        + ", name=" + coffeeSite.getName()
                        + ", status=" + (coffeeSite.getStatusZaznamu() != null ? coffeeSite.getStatusZaznamu().toString() : "null")
                        + ", imageUrl=" + coffeeSite.getMainImageURL()
                        + ", city=" + coffeeSite.getMesto()
                        + ", cups=" + cupTypesCount
                        + ", sorts=" + coffeeSortsCount;
            }

            @Override
            public void onResponse(Call<List<CoffeeSite>> call, Response<List<CoffeeSite>> response) {
                List<CoffeeSiteMovable> coffeeSiteMovables = new ArrayList<>();
                if (response.isSuccessful()) {
                    Log.i(TAG, "onSuccess()");
                    if (response.body() != null) {
                        List<CoffeeSite> coffeeSites = response.body();
                        Log.i(TAG, "CoffeeSites in range loaded. count=" + coffeeSites.size()
                                + (coffeeSites.isEmpty() ? "" : ", first={" + describeCoffeeSite(coffeeSites.get(0)) + "}"));
                        for (CoffeeSite cs : coffeeSites) {
                            coffeeSiteMovables.add(new CoffeeSiteMovable(cs));
                        }
                        if (callingService != null) {
                            callingService.onSitesInRangeReturnedFromServer(coffeeSiteMovables);
                        }
                    } else {
                        String error = "Returned empty response for loading CoffeeSites in range REST request.";
                        Log.i(TAG, error);
                        if (callingService != null) {
                            callingService.onSitesInRangeReturnedFromServer(coffeeSiteMovables);
                        }
                    }
                } else {
                    try {
                        Log.i(TAG, "No CoffeeSite found.");
                        if (callingService != null) {
                            String error = response.errorBody() != null
                                    ? response.errorBody().string()
                                    : "Unknown error";
                            callingService.onSitesInRangeReturnedFromServerError(error);
                        }
                    } catch (IOException e) {
                        String error = e.getMessage();
                        Log.e(TAG, error);
                        if (callingService != null) {
                            callingService.onSitesInRangeReturnedFromServerError(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CoffeeSite>> call, Throwable t) {
                String error = "Error loading CoffeeSites in range REST request." + t.getMessage();
                Log.e(TAG, error);
                if (callingService != null) {
                    callingService.onSitesInRangeReturnedFromServerError(error);
                }
            }
        });
    }

}
