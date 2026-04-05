package cz.fungisoft.coffeecompass2.activity.interfaces.images;

import cz.fungisoft.coffeecompass2.BuildConfig;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Retrofit interface for secured operations on the new Images API
 * (images-api.yaml). Provides upload, delete single and delete all endpoints.
 * <p>
 * Base URL is {@link BuildConfig#IMAGES_API_PUBLIC_URL} with Bearer JWT auth.
 */
public interface ImagesApiSecuredRESTInterface {

    String BASE_URL = BuildConfig.IMAGES_API_PUBLIC_URL;

    /**
     * Uploads a new image for the given object.
     * <p>
     * Example: POST /upload with multipart form-data containing
     * objectExtId, description, imageType and file.
     *
     * @param objectExtId the external ID of the object (CoffeeSite ID)
     * @param description optional description of the image
     * @param imageType   image type, set by user, e.g. "main"
     * @param type        image type
     * @param file        the image file binary part
     * @return the external ID (UUID) of the newly created image
     */
    @Multipart
    @POST("upload")
    Call<String> uploadImage(@Part("objectExtId") RequestBody objectExtId,
                             @Part("description") RequestBody description,
                             @Part("imageType") RequestBody imageType,
                             @Part("type") RequestBody type,
                             @Part MultipartBody.Part file);

    /**
     * Deletes a single image identified by its object and image external IDs.
     * <p>
     * Example: DELETE /object/{objectExtId}/image/{imageExtId}
     *
     * @param objectExtId the external ID of the object (CoffeeSite ID)
     * @param imageExtId  the external ID of the image to delete
     * @return void response (HTTP 200 on success)
     */
    @DELETE("object/{objectExtId}/image/{imageExtId}")
    Call<Void> deleteImage(@Path("objectExtId") String objectExtId,
                           @Path("imageExtId") String imageExtId);

    /**
     * Deletes all images associated with the given object.
     * <p>
     * Example: DELETE /object/{objectExtId}/all
     *
     * @param objectExtId the external ID of the object (CoffeeSite ID)
     * @return void response (HTTP 200 on success)
     */
    @DELETE("object/{objectExtId}/all")
    Call<Void> deleteAllImages(@Path("objectExtId") String objectExtId);
}
