package cz.fungisoft.coffeecompass2.activity.interfaces.images;

import cz.fungisoft.coffeecompass2.BuildConfig;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for the new Images API (images-api.yaml).<br>
 * Provides endpoints for downloading images in various sizes.
 * <p>
 * Base URL is configured via {@link BuildConfig#IMAGES_API_PUBLIC_URL}
 * which already includes the API gateway prefix "/api/v1/images/".
 * <p>
 * Used for downloading CoffeeSite images for offline mode.
 */
public interface ImagesApiRESTInterface {

    String IMAGES_API_BASE_URL = BuildConfig.IMAGES_API_PUBLIC_URL;

    /**
     * Gets the latest image of the given type related to the Object ext-ID as bytes in the given size.
     * <p>
     * Example call: GET /bytes/object/?objectExtId={coffeeSiteExtId}&type=main&size=mid
     *
     * @param objectExtId the external ID of the CoffeeSite (object)
     * @param type        image type, e.g. "main"
     * @param size        image size: "original", "hd", "large", "mid", "small"
     * @return binary image data as ResponseBody
     */
    @GET("bytes/object/")
    Call<ResponseBody> getImageOfTypeAsBytes(@Query("objectExtId") String objectExtId,
                                            @Query("type") String type,
                                            @Query("size") String size);

    /**
     * Gets the image file bytes by image ext-ID and size.
     * <p>
     * Example call: GET /bytes/?imageExtId={imageExtId}&size=mid
     *
     * @param imageExtId the external ID of the image
     * @param size       image size: "original", "hd", "large", "mid", "small"
     * @return binary image data as ResponseBody
     */
    @GET("bytes/")
    Call<ResponseBody> getImageBytes(@Query("imageExtId") String imageExtId,
                                    @Query("size") String size);

    /**
     * Gets the ImageObject description with all ImageFiles for the given object ext-ID.
     * <p>
     * Example call: GET /object/{objectExtId}
     *
     * @param objectExtId the external ID of the CoffeeSite (object)
     * @return ImageObject JSON with list of image files
     */
    @GET("object/{objectExtId}")
    Call<ResponseBody> getImageObject(@retrofit2.http.Path("objectExtId") String objectExtId);

    /**
     * Gets the total size in kB of all images for all objects in the given size category.
     * <p>
     * Example call: GET /all/sizeKB?imageSize=mid
     *
     * @param imageSize image size: "original", "hd", "large", "mid", "small"
     * @return size in kB
     */
    @GET("all/sizeKB")
    Call<Integer> getSizeOfAllImagesToDownload(@Query("imageSize") String imageSize);

    /**
     * Gets the number of all images for all objects in the given size category.
     * <p>
     * Example call: GET /all/number?imageSize=mid
     *
     * @param imageSize image size: "original", "hd", "large", "mid", "small"
     * @return number of images
     */
    @GET("all/number")
    Call<Integer> getNumberOfAllImagesToDownload(@Query("imageSize") String imageSize);
}
