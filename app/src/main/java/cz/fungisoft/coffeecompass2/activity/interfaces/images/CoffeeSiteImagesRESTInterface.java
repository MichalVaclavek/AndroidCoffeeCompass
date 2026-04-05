package cz.fungisoft.coffeecompass2.activity.interfaces.images;

import java.util.List;

import cz.fungisoft.coffeecompass2.BuildConfig;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit interface for CoffeeSite image URLs.
 */
public interface CoffeeSiteImagesRESTInterface {

    String COFFEESITE_API_PUBLIC_URL = BuildConfig.COFFEESITE_SITES_API_PUBLIC_URL;

    /**
     * Returns all image URLs for a CoffeeSite.
     * Example: /api/v1/sites/image/allImageUrls/{coffeeSiteId}
     *
     * @param coffeeSiteId CoffeeSite id
     * @return list of image URLs
     */
    @GET("image/allImageUrls/{coffeeSiteId}")
    Call<List<String>> getAllImageUrls(@Path("coffeeSiteId") String coffeeSiteId);
}
