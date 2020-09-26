package cz.fungisoft.coffeecompass2.entity;

/**
 * Holder of statistics info loaded from coffeecompass.cz
 */
public class Statistics {

    /**
     * Statistics atributes
     */
    public String numOfSites = "";
    public String numOfSitesLastWeek = "";
    public String numOfSitesToday = "";
    public String numOfUsers = "";

    public Statistics(String sites, String sitesLastWeek, String sitesToday, String users) {
        this.numOfSites = sites;
        this.numOfSitesLastWeek = sitesLastWeek;
        this.numOfSitesToday = sitesToday;
        this.numOfUsers = users;
    }

}
