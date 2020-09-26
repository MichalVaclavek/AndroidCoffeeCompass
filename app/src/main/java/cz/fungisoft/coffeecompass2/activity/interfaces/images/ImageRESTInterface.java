package cz.fungisoft.coffeecompass2.activity.interfaces.images;

import cz.fungisoft.coffeecompass2.BuildConfig;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
* Retrofit interface for REST requests related to CoffeeSite's image object.
* Used by {@link cz.fungisoft.coffeecompass2.asynctask.coffeesite.ImageUploadAsyncTask}
 * and {@link cz.fungisoft.coffeecompass2.asynctask.coffeesite.ImageDeleteAsyncTask}
*/
public interface ImageRESTInterface {

    String UPLOAD_IMAGE_URL = BuildConfig.IMAGES_API_SECURED_URL;

    String DELETE_IMAGE_URL = BuildConfig.IMAGES_API_SECURED_URL;

    /**
     * Calls uploading of Image file. Returns URL of the uploaded image.
     *
     * @param coffeeSiteId
     * @return saved image load URL
     */
    @Multipart
    @POST("upload")
    Call<String> uploadImage(@Part MultipartBody.Part file, @Query("coffeeSiteId") long coffeeSiteId);


    /**
     * Calls delete of CoffeeSite"s image. Input is an ID of the CoffeeSite
     * to whom the image belongs to.
     *
     * Example: https://coffeecompass.cz/rest/secured/image/delete/site/320
     *
     * @param siteId
     * @return
     */
    @DELETE("delete/site/{siteId}")
    Call<Integer> deleteImageBySiteId(@Path("siteId") int siteId);

    /**
     * Calls delete of CoffeeSite|s image. Input is an ID of the CoffeeSite
     * to whom the image belongs to.
     *
     * Example: https://coffeecompass.cz/rest/secured/image/delete/567
     *
     * @param imageId
     * @return
     */
    @DELETE("delete/{imageId}")
    Call<Integer> deleteImageByImageId(@Path("imageId") int imageId);

}
