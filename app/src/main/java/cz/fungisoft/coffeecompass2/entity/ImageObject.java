package cz.fungisoft.coffeecompass2.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO model for the ImageObject returned by the Images API
 * endpoint {@code GET /object/{objectExtId}}.
 * <p>
 * Contains the external object ID and a list of all {@link ImageFile}
 * entries associated with it.
 */
public class ImageObject {

    @Expose
    @SerializedName("externalObjectId")
    private String externalObjectId;

    @Expose
    @SerializedName("baseBytesObjectUrl")
    private String baseBytesObjectUrl;

    @Expose
    @SerializedName("baseBase64ObjectUrl")
    private String baseBase64ObjectUrl;

    @Expose
    @SerializedName("objectImages")
    private List<ImageFile> objectImages;

    public String getExternalObjectId() {
        return externalObjectId;
    }

    public void setExternalObjectId(String externalObjectId) {
        this.externalObjectId = externalObjectId;
    }

    public String getBaseBytesObjectUrl() {
        return baseBytesObjectUrl;
    }

    public void setBaseBytesObjectUrl(String baseBytesObjectUrl) {
        this.baseBytesObjectUrl = baseBytesObjectUrl;
    }

    public String getBaseBase64ObjectUrl() {
        return baseBase64ObjectUrl;
    }

    public void setBaseBase64ObjectUrl(String baseBase64ObjectUrl) {
        this.baseBase64ObjectUrl = baseBase64ObjectUrl;
    }

    public List<ImageFile> getObjectImages() {
        if (objectImages == null) {
            objectImages = new ArrayList<>();
        }
        return objectImages;
    }

    public void setObjectImages(List<ImageFile> objectImages) {
        this.objectImages = objectImages;
    }
}
