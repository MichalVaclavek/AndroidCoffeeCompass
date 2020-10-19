package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Saves if data for OFFLINE mode were downloaded and the Date of download
 */
public class DataForOfflineModeDownloadPreferenceHelper {

    private final String DATA_DOWNLOADED = "downloaded";

    private final String DOWNLOAD_DATE = "download_date";

    private SharedPreferences app_prefs;
    private Context context;

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

}
