package cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Class describing one component of the Page class returning from server
 * when requesting CoffeeSite or Comment by Page.
 * Used for JSON serialization/deserialization.
 *
 * @see CoffeeSitePageEnvelope
 * @see Pageable
 */
public class Sort {

    @SerializedName("direction")
    @Expose
    private String direction;

    @SerializedName("property")
    @Expose
    private String property;

    @SerializedName("ignoreCase")
    @Expose
    private Boolean ignoreCase;

    @SerializedName("nullHandling")
    @Expose
    private String nullHandling;

    @SerializedName("ascending")
    @Expose
    private Boolean ascending;

    @SerializedName("descending")
    @Expose
    private Boolean descending;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Boolean getIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(Boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public String getNullHandling() {
        return nullHandling;
    }

    public void setNullHandling(String nullHandling) {
        this.nullHandling = nullHandling;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
    }

    public Boolean getDescending() {
        return descending;
    }

    public void setDescending(Boolean descending) {
        this.descending = descending;
    }
}
