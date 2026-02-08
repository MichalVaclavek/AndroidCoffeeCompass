package cz.fungisoft.coffeecompass2.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holder of statistics info loaded from coffeecompass.cz
 */
public class Statistics {

    /**
     * Statistics attributes
     */
    @Expose
    @SerializedName("numOfAllSites")
    public String numOfSites = "";

    @Expose
    @SerializedName("numOfNewSitesLast7Days")
    public String numOfSitesLastWeek = "";

    @Expose
    @SerializedName("numOfNewSitesToday")
    public String numOfSitesToday = "";

    @Expose
    @SerializedName("numOfAllUsers")
    public String numOfUsers = "";

    public Statistics() {}

    public Statistics(String sites, String sitesLastWeek, String sitesToday, String users) {
        this.numOfSites = sites;
        this.numOfSitesLastWeek = sitesLastWeek;
        this.numOfSitesToday = sitesToday;
        this.numOfUsers = users;
    }

}
