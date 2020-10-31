package cz.fungisoft.coffeecompass2.entity;

/**
 * Helper class to hold info about number of downloaded data when OFFLINE mode
 * Activity performs its work.
 * To show download results on OfflineModeSelectionActivity.
 * Also to saves into Preferences.
 */
public class DownloadDataOverview {

    /**
     * Statistics atributes
     */
    public int numOfSitesDownloaded = 0;
    public int numOfCommentsDownloaded = 0;
    public int numOfImagesDownloaded = 0;

    public DownloadDataOverview(int sites, int comments, int images) {
        this.numOfSitesDownloaded = sites;
        this.numOfCommentsDownloaded = comments;
        this.numOfImagesDownloaded = images;
    }

}
