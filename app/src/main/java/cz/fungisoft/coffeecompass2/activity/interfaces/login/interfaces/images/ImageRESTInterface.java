package cz.fungisoft.coffeecompass2.activity.interfaces.login.interfaces.images;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ImageRESTInterface {

    String UPLOAD_IMAGE_URL = "https://coffeecompass.cz/rest/secured/image/";

    String DELETE_IMAGE_URL = "https://coffeecompass.cz/rest/secured/image/";


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
     * Calls delete of CoffeeSite|s image. Input is an ID of the CoffeeSite
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
