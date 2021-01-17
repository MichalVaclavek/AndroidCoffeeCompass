package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

import cz.fungisoft.coffeecompass2.entity.DownloadDataOverview;

/**
 * Saves if data for OFFLINE mode were downloaded and the Date of download
 */
public class DataForOfflineModeDownloadPreferenceHelper {

    private final String DATA_DOWNLOADED = "downloaded";
    private final String DOWNLOAD_DATE = "download_date";

    private final String DOWNLOADED_SITES = "downloaded_sites";
    private final String DOWNLOADED_COMMENTS = "downloaded_comments";
    private final String DOWNLOADED_IMAGES = "downloaded_images";

    private final SharedPreferences app_prefs;
    private final Context context;

    public DataForOfflineModeDownloadPreferenceHelper(Context context) {
        app_prefs = context.getSharedPreferences("sharedDataDownload",
                Context.MODE_PRIVATE);
        this.context = context;
    }

    public void putDownloaded(boolean downloaded) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putBoolean(DATA_DOWNLOADED, downloaded);
        edit.apply();
    }

    public boolean getDownloaded() {
        return app_prefs.getBoolean(DATA_DOWNLOADED, false);
    }

    public void putDownloadDate(Date downloadDate) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putLong(DOWNLOAD_DATE, downloadDate.getTime());
        edit.apply();
    }

    public Date getDownloadDate() {
        return new Date(app_prefs.getLong(DOWNLOAD_DATE, 1));
    }

    public DownloadDataOverview getDownloadOverview() {
        DownloadDataOverview overview = new DownloadDataOverview(
                                                app_prefs.getInt(DOWNLOADED_SITES, 0),
                                                app_prefs.getInt(DOWNLOADED_COMMENTS, 0),
                                                app_prefs.getInt(DOWNLOADED_IMAGES, 0));
        return overview;
    }

    public void putDownloadOverview(DownloadDataOverview overview) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(DOWNLOADED_SITES, overview.numOfSitesDownloaded);
        edit.putInt(DOWNLOADED_COMMENTS, overview.numOfCommentsDownloaded);
        edit.putInt(DOWNLOADED_IMAGES, overview.numOfImagesDownloaded);
        edit.apply();
    }
}
