package cz.fungisoft.coffeecompass2.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cz.fungisoft.coffeecompass2.BuildConfig;

/**
 * POJO model for a single image file entry returned by the Images API
 * endpoint {@code GET /object/{objectExtId}}.
 * <p>
 * Each {@link ImageObject} may contain multiple {@code ImageFile} entries.
 */
public class ImageFile {

    @Expose
    @SerializedName("externalId")
    private String externalId;

    @Expose
    @SerializedName("imageType")
    private String imageType;

    @Expose
    @SerializedName("description")
    private String description;

    @Expose
    @SerializedName("savedOn")
    private String savedOn;

    @Expose
    @SerializedName("baseBytesImageUrl")
    private String baseBytesImageUrl;

    @Expose
    @SerializedName("baseBase64ImageUrl")
    private String baseBase64ImageUrl;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSavedOn() {
        return savedOn;
    }

    public void setSavedOn(String savedOn) {
        this.savedOn = savedOn;
    }

    public String getBaseBytesImageUrl() {
        return baseBytesImageUrl;
    }

    public void setBaseBytesImageUrl(String baseBytesImageUrl) {
        this.baseBytesImageUrl = baseBytesImageUrl;
    }

    public String getBaseBase64ImageUrl() {
        return baseBase64ImageUrl;
    }

    public void setBaseBase64ImageUrl(String baseBase64ImageUrl) {
        this.baseBase64ImageUrl = baseBase64ImageUrl;
    }

    /**
     * Returns the URL for downloading this image in the given size.
     *
     * @param size one of "original", "hd", "large", "mid", "small"
     * @return full URL with size query parameter appended
     */
    public String getBytesUrl(String size) {
        String url = baseBytesImageUrl;

        if (url == null || url.isEmpty()) {
            if (externalId == null || externalId.isEmpty()) {
                return "";
            }
            url = BuildConfig.IMAGES_API_PUBLIC_URL + "bytes/?imageExtId=" + externalId;
        } else if (isInternalImagesHost(url) && externalId != null && !externalId.isEmpty()) {
            url = BuildConfig.IMAGES_API_PUBLIC_URL + "bytes/?imageExtId=" + externalId;
        }

        if (size != null && !size.isEmpty() && !url.contains("size=")) {
            url = appendQueryParam(url, "size", size);
        }

        return url;
    }

    private boolean isInternalImagesHost(String url) {
        return url.startsWith("https://images") || url.startsWith("http://images");
    }

    private String appendQueryParam(String url, String name, String value) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + name + "=" + value;
    }
}
