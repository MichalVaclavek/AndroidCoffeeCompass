package cz.fungisoft.coffeecompass2.activity.interfaces.images;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.entity.ImageObject;

/**
 * Listener interface for image management operations (load, upload, delete)
 * used by the CoffeeSiteImagesActivity.
 */
public interface CoffeeSiteImageManageListener {

    /**
     * Called when ImageObject with all image files has been loaded successfully.
     *
     * @param imageObject the loaded ImageObject containing all image file entries
     */
    void onImageObjectLoaded(ImageObject imageObject);

    /**
     * Called when loading of ImageObject fails.
     *
     * @param error error details
     */
    void onImageObjectLoadFailed(Result.Error error);

    /**
     * Called when a new image has been uploaded successfully.
     *
     * @param imageExtId the external ID of the newly uploaded image
     */
    void onImageUploaded(String imageExtId);

    /**
     * Called when image upload fails.
     *
     * @param error error details
     */
    void onImageUploadFailed(Result.Error error);

    /**
     * Called when an image has been deleted successfully.
     *
     * @param imageExtId the external ID of the deleted image
     */
    void onImageDeleted(String imageExtId);

    /**
     * Called when image deletion fails.
     *
     * @param error error details
     */
    void onImageDeleteFailed(Result.Error error);
}
