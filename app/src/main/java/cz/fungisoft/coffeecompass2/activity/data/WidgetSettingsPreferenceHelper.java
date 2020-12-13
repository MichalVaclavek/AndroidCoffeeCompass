package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

import cz.fungisoft.coffeecompass2.R;

/**
 * Saves current selected search distance into Preference repository
 */
public class WidgetSettingsPreferenceHelper {

    private final String SEARCH_DISTANCE = "searchDistanceWidget";
    private final String TEXT_COLOR = "textColorWidget";
    private final String SELECTED_RADIO = "selectedRadioWidget";

    private final String BACKROUND = "backroundWidget";

    private SharedPreferences app_prefs;
    private Context context;

    public WidgetSettingsPreferenceHelper(Context context) {
        app_prefs = context.getSharedPreferences("sharedDistance",
                Context.MODE_PRIVATE);
        this.context = context;
    }

    public void putSearchDistance(int searchDistance) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(SEARCH_DISTANCE, searchDistance);
        edit.apply();
    }

    public int getSearchDistance() {
        return app_prefs.getInt(SEARCH_DISTANCE, 500);
    }

    public void putTextColor(int textColor) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(TEXT_COLOR, textColor);
        edit.apply();
    }

    public int getTextColor() {
        return app_prefs.getInt(TEXT_COLOR, R.color.colorPrimaryDark);
    }

    public void putBackround(int backround) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(BACKROUND, backround);
        edit.apply();
    }

    public int getBackround() {
        return app_prefs.getInt(BACKROUND, 0);
    }

    public void putSelectedRadio(int radio) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(SELECTED_RADIO, radio);
        edit.apply();
    }

    public int getSelectedRadio() {
        return app_prefs.getInt(SELECTED_RADIO, 1);
    }

}
