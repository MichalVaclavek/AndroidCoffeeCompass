package cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pageable {

    @SerializedName("offset")
    @Expose
    private Integer offset;

    @SerializedName("pageNumber")
    @Expose
    private Integer pageNumber;

    @SerializedName("pageSize")
    @Expose
    private Integer pageSize;

    @SerializedName("paged")
    @Expose
    private Boolean paged;

    @SerializedName("sort")
    @Expose
    private Sort sort;

    @SerializedName("unpaged")
    @Expose
    private Boolean unpaged;

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getPaged() {
        return paged;
    }

    public void setPaged(Boolean paged) {
        this.paged = paged;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Boolean getUnpaged() {
        return unpaged;
    }

    public void setUnpaged(Boolean unpaged) {
        this.unpaged = unpaged;
    }
}
