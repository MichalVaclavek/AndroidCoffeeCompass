package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

import cz.fungisoft.coffeecompass2.entity.DownloadDataOverview;

/**
 * Saves if data (CoffeeSites, Comments, Images, Entities) for OFFLINE mode were downloaded and the Date of download.
 * Also keeps info, that there are CoffeeSites or Comments created in Offline mode and saved to local DB.
 */
public class DataForOfflineModePreferenceHelper {

    private final String DATA_DOWNLOADED = "downloaded";
    private final String DOWNLOAD_DATE = "download_date";

    private final String DOWNLOADED_SITES = "downloaded_sites";
    private final String DOWNLOADED_COMMENTS = "downloaded_comments";
    private final String DOWNLOADED_IMAGES = "downloaded_images";

    // Location types, cup types, and so on ...
    private final String DOWNLOADED_CS_ENTITIES = "downloaded_entities";

    // to keep info, that some data (CoffeeSites or Comments) were saved into DB in Offline mode
    // and are not saved on server yet
    private final String DATA_SAVED_OFFLINE = "data_saved_offline_available";

    private final SharedPreferences app_prefs;
    private final Context context;

    public DataForOfflineModePreferenceHelper(Context context) {
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


    public void putDataSavedOfflineAvailable(boolean savedOffline) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putBoolean(DATA_SAVED_OFFLINE, savedOffline);
        edit.apply();
    }

    public boolean getDataSavedOfflineAvailable() {
        return app_prefs.getBoolean(DATA_SAVED_OFFLINE, false);
    }

    public void putCSEntitiesDownloaded(boolean csEntitiesDownloaded) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putBoolean(DOWNLOADED_CS_ENTITIES, csEntitiesDownloaded);
        edit.apply();
    }

    public boolean getCSEntitiesDownloaded() {
        return app_prefs.getBoolean(DOWNLOADED_CS_ENTITIES, false);
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
