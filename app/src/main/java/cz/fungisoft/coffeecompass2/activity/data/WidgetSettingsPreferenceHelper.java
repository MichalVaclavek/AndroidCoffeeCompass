package cz.fungisoft.coffeecompass2.activity.data;

import android.content.Context;
import android.content.SharedPreferences;

import cz.fungisoft.coffeecompass2.R;

/**
 * Saves current selected search distance into Preference repository
 */
public class WidgetSettingsPreferenceHelper {

    private final String SEARCH_DISTANCE = "searchDistanceWidget";
    private final String SELECTED_DISTANCE_RADIO = "selectedDistanceRadioWidget";

    private final String TEXT_COLOR = "textColorWidget";
    private final String SELECTED_TEXT_COLOR_RADIO = "selectedTextColorRadioWidget";

    private final String BACKROUND_COLOR = "backroundColorWidget";
    private final String SELECTED_BACKROUND_COLOR_RADIO = "selectedBackroundColorRadioWidget";

    /**
     * Barva ramecku widgetu
     */
    private final String FRAME_COLOR = "frameColorWidget";
    private final String SELECTED_FRAME_COLOR_RADIO = "selectedFrameColorRadioWidget";

    private final String BACKROUND_OPACITY = "backroundOpacityWidget";

    private final String LAST_SERVICE_CALL = "lastServiceCallWidget";

    private final SharedPreferences app_prefs;
    private Context context;


    public WidgetSettingsPreferenceHelper(Context context) {
        app_prefs = context.getSharedPreferences("sharedWidgetPrefs", Context.MODE_PRIVATE);
        this.context = context;
    }

    /* **** TEXT **** */

    public void putTextColor(int textColor) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(TEXT_COLOR, textColor);
        edit.apply();
    }

    public int getTextColor() {
        return app_prefs.getInt(TEXT_COLOR, R.color.colorPrimaryDark);
    }

    public void putSelectedTextRadio(int radio) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(SELECTED_TEXT_COLOR_RADIO, radio);
        edit.apply();
    }

    public int getSelectedTextRadio() {
        return app_prefs.getInt(SELECTED_TEXT_COLOR_RADIO, 1);
    }

   /* **** FRAME **** */

    public void putSelectedFrameColor(int backroundColor) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(FRAME_COLOR, backroundColor);
        edit.apply();
    }

    public int getSelectedFrameColor() {
        return app_prefs.getInt(FRAME_COLOR, R.color.colorPrimary2);
    }


    public void putSelectedFrameColorRadio(int radio) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(SELECTED_FRAME_COLOR_RADIO, radio);
        edit.apply();
    }

    public int getSelectedFrameColorRadio() {
        return app_prefs.getInt(SELECTED_FRAME_COLOR_RADIO, 1);
    }

    /* *** BACKROUND **** */

    public void putSelectedBackroundColor(int backroundColor) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(BACKROUND_COLOR, backroundColor);
        edit.apply();
    }

    public int getSelectedBackroundColor() {
        return app_prefs.getInt(BACKROUND_COLOR, R.color.colorPrimary2);
    }


    public void putSelectedBackroundColorRadio(int radio) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(SELECTED_BACKROUND_COLOR_RADIO, radio);
        edit.apply();
    }

    public int getSelectedBackroundColorRadio() {
        return app_prefs.getInt(SELECTED_BACKROUND_COLOR_RADIO, 1);
    }

    public void putBackroundOpacity(int backround) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(BACKROUND_OPACITY, backround);
        edit.apply();
    }

    public int getBackroundOpacity() {
        return app_prefs.getInt(BACKROUND_OPACITY, 255);
    }

    /* *** DISTANCE **** */

    public void putSelectedDistanceRadio(int radio) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(SELECTED_DISTANCE_RADIO, radio);
        edit.apply();
    }

    public int getSelectedDistanceRadio() {
        return app_prefs.getInt(SELECTED_DISTANCE_RADIO, 4);
    }

    public void putSearchDistance(int searchDistance) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putInt(SEARCH_DISTANCE, searchDistance);
        edit.apply();
    }

    public int getSearchDistance() {
        return app_prefs.getInt(SEARCH_DISTANCE, 1000);
    }

    public void putLastServiceCall(long lastServiceCall) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putLong(LAST_SERVICE_CALL, lastServiceCall);
        edit.apply();
    }

    public long getLastServiceCall() {
        return app_prefs.getLong(LAST_SERVICE_CALL, 0);
    }
}
