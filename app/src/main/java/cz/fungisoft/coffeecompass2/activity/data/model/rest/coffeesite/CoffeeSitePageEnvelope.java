package cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;

/**
 * Class to map JSON response for Pageable CoffeeSites,
 * for example {@link CoffeeSiteRESTInterface.getAllCoffeeSitesFromCurrentUserPaginated() }
 */
public class CoffeeSitePageEnvelope {

    @SerializedName("content")
    @Expose
    private List<CoffeeSite> content;

    @SerializedName("empty")
    @Expose
    private Boolean empty;

    @SerializedName("first")
    @Expose
    private Boolean first;

    @SerializedName("last")
    @Expose
    private Boolean last;

    @SerializedName("number")
    @Expose
    private Integer number;

    @SerializedName("numberOfElements")
    @Expose
    private Integer numberOfElements;

    @SerializedName("pageable")
    @Expose
    private Pageable pageable;

    @SerializedName("size")
    @Expose
    private Integer size;

    @SerializedName("sort")
    @Expose
    private Sort sort;

    @SerializedName("totalElements")
    @Expose
    private Integer totalElements;

    @SerializedName("totalPages")
    @Expose
    private Integer totalPages;

    public List<CoffeeSite> getContent() {
        return content;
    }

    public void setContent(List<CoffeeSite> content) {
        this.content = content;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

}
