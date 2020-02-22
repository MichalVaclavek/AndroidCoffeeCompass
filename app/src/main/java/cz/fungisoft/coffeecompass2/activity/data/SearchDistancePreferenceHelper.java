package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves current selected search distance into Preference repository
 */
public class SearchDistancePreferenceHelper {

    private final String SEARCH_DISTANCE = "searchDistance";

    private SharedPreferences app_prefs;
    private Context context;

    public SearchDistancePreferenceHelper(Context context) {
        app_prefs = context.getSharedPreferences("sharedDistance",
                Context.MODE_PRIVATE);
        this.context = context;
    }

    public void putSearchDistance(int searchDistance) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(SEARCH_DISTANCE, searchDistance);
        edit.commit();
    }

    public int getSearchDistanc() {
        return app_prefs.getInt(SEARCH_DISTANCE, 500);
    }

}
