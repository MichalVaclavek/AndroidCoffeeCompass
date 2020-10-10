package cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sort {

    @SerializedName("empty")
    @Expose
    private Boolean empty;

    @SerializedName("sorted")
    @Expose
    private Boolean sorted;

    @SerializedName("unsorted")
    @Expose
    private Boolean unsorted;

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Boolean getSorted() {
        return sorted;
    }

    public void setSorted(Boolean sorted) {
        this.sorted = sorted;
    }

    public Boolean getUnsorted() {
        return unsorted;
    }

    public void setUnsorted(Boolean unsorted) {
        this.unsorted = unsorted;
    }
}
