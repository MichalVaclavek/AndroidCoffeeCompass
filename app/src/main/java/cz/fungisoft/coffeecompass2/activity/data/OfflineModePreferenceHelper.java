package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves if OFFLINE mode is switched on or off into Preference repository
 */
public class OfflineModePreferenceHelper {

    private final String OFFLINE_MODE = "offlineMode";

    private SharedPreferences app_prefs;
    private Context context;

    public OfflineModePreferenceHelper(Context context) {
        app_prefs = context.getSharedPreferences("sharedDistance",
                Context.MODE_PRIVATE);
        this.context = context;
    }

    public void putOfflineMode(boolean offlinemodeon) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putBoolean(OFFLINE_MODE, offlinemodeon);
        edit.apply();
    }

    public boolean getOfflineMode() {
        return app_prefs.getBoolean(OFFLINE_MODE, false);
    }

}
