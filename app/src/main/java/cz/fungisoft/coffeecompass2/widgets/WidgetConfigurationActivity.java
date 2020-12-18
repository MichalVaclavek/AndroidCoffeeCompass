package cz.fungisoft.coffeecompass2.widgets;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.WidgetSettingsPreferenceHelper;

public class WidgetConfigurationActivity extends AppCompatActivity {

    private LinearLayout container;

    private SeekBar opacitySeekBar;
    //private RadioGroup rgTextColor;
    private RadioGroup rgBackroundColor;
    private RadioGroup rgFrameColor;
    private RadioGroup rgDistance;

    private TextView siteName;

    // Saves selected search distance range
    private WidgetSettingsPreferenceHelper sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    //.replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        siteName = findViewById(R.id.widget_nearest_site_name);

        container = findViewById(R.id.widget_settings_container);

        opacitySeekBar = findViewById(R.id.widget_background_opacity);
//        rgTextColor = findViewById(R.id.widget_text_color);
        rgBackroundColor = findViewById(R.id.widget_backround_color);
        rgFrameColor = findViewById(R.id.widget_frame_color);
        rgDistance = findViewById(R.id.widget_search_distance);

        // Get current serachDistance from Preferences
        sharedPref = new WidgetSettingsPreferenceHelper(this);

        //populate setting-UI elements with the selected values or default values
        opacitySeekBar.setMax(100);
        opacitySeekBar.setProgress(sharedPref.getBackroundOpacity());

        //rgTextColor.check(sharedPref.getSelectedTextRadio());
        rgBackroundColor.check(sharedPref.getSelectedBackroundColorRadio());
        rgFrameColor.check(sharedPref.getSelectedFrameColorRadio());
        rgDistance.check(sharedPref.getSelectedDistanceRadio());

        //apply settings to the widget
        //helps in viewing how widget looks with current settings.
        applySettings();

        //handle widget background settings and store in shared preferences
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //save the selected background
                sharedPref.putBackroundOpacity(i);
                applySettings();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //google places api requires location access peremission
        //checkPermission(ACCESS_FINE_LOCATION);

    }

//    public static class SettingsFragment extends PreferenceFragmentCompat {
//        @Override
//        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//            setPreferencesFromResource(R.xml.root_preferences, rootKey);
//        }
//    }

    // handle text color setting and store in shared preferences
//    public void onChooseTextColor(View view) {
//        if (!((RadioButton) view).isChecked()) {
//            return;
//        }
//
//        int color = android.R.color.black;
//        int selectedRadio = R.id.black_text;
//
//        switch (view.getId()) {
//            case R.id.red_text:
//                color = android.R.color.holo_red_dark;
//                selectedRadio = R.id.red_text;
//                break;
//            case R.id.blue_text:
//                color = android.R.color.holo_blue_dark;
//                selectedRadio = R.id.blue_text;
//                break;
//            case R.id.green_text:
//                color = android.R.color.holo_green_dark;
//                selectedRadio = R.id.green_text;
//                break;
//            case R.id.black_text:
//                color = android.R.color.black;
//                selectedRadio = R.id.black_text;
//                break;
//        }
//
//        sharedPref.putTextColor(color);
//        sharedPref.putSelectedTextRadio(selectedRadio);
//
//        applySettings();
//    }

    // handle widget frame color setting and store in shared preferences
    public void onChooseFrameColor(View view) {
        if (!((RadioButton) view).isChecked()) {
            return;
        }

        int color = android.R.color.black;
        int selectedRadio = R.id.black_frame;

        switch (view.getId()) {
            case R.id.red_frame:
                color = android.R.color.holo_red_dark;
                selectedRadio = R.id.red_frame;
                break;
            case R.id.blue_frame:
                color = android.R.color.holo_blue_dark;
                selectedRadio = R.id.blue_frame;
                break;
            case R.id.green_frame:
                color = android.R.color.holo_green_dark;
                selectedRadio = R.id.green_frame;
                break;
            case R.id.black_frame:
                color = android.R.color.black;
                selectedRadio = R.id.black_frame;
                break;
        }

        sharedPref.putSelectedFrameColor(color);
        sharedPref.putSelectedFrameColorRadio(selectedRadio);

        applySettings();
    }

    // handle backround color setting and store in shared preferences
    public void onChooseBackroundColor(View view) {
        if (!((RadioButton) view).isChecked()) {
            return;
        }

        int color = android.R.color.black;
        int selectedRadio = R.id.black_backround;

        switch (view.getId()) {
            case R.id.red_backround:
                color = android.R.color.holo_red_dark;
                selectedRadio = R.id.red_backround;
                break;
            case R.id.blue_backround:
                color = android.R.color.holo_blue_dark;
                selectedRadio = R.id.blue_backround;
                break;
            case R.id.green_backround:
                color = android.R.color.holo_green_dark;
                selectedRadio = R.id.green_backround;
                break;
            case R.id.black_backround:
                color = android.R.color.black;
                selectedRadio = R.id.black_backround;
                break;
        }

        sharedPref.putSelectedBackroundColor(color);
        sharedPref.putSelectedBackroundColorRadio(selectedRadio);

        applySettings();
    }

    // handle search distance setting and store in shared preferences
    public void onChooseSearchDistance(View view) {
        if (!((RadioButton) view).isChecked()) {
            return;
        }

        int distance = 500;
        int selectedRadio = R.id.widget_dist_2000;

        switch (view.getId()) {
            case R.id.widget_dist_100:
                distance = 100;
                selectedRadio = R.id.widget_dist_100;
                break;
            case R.id.widget_dist_200:
                distance = 200;
                selectedRadio = R.id.widget_dist_200;
                break;
            case R.id.widget_dist_500:
                distance = 500;
                selectedRadio = R.id.widget_dist_500;
                break;
            case R.id.widget_dist_1000:
                distance = 1000;
                selectedRadio = R.id.widget_dist_1000;
                break;
            case R.id.widget_dist_2000:
                distance = 2000;
                selectedRadio = R.id.widget_dist_2000;
                break;
        }

        sharedPref.putSearchDistance(distance);
        sharedPref.putSelectedDistanceRadio(selectedRadio);

        applySettings();
    }

    // apply selected settings to the widget to see how widget
    // looks with the selected settings.
    //TODO - set all other selected colors i.e. Frame, Backround
    private void applySettings() {

        int widgetBackground = sharedPref.getBackroundOpacity();
        //0 means transparent , 255 opaque
        //convert value to percentage
        int transparentBackground = widgetBackground * 100 / 255;

        int textColor = sharedPref.getTextColor();

        siteName.setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
        container.getBackground().setAlpha(transparentBackground);

        //TODO - set backround end color

        //TODO - set frame color

    }

    // close settings screen
    public void closeSettings(View v) {
        //update all widgets to apply the slected settings.
        MainAppWidgetProvider.updateCoffeeSiteWidget(getApplicationContext(), null);

        Intent widgetIntent = new Intent();
        setResult(RESULT_OK, widgetIntent);
        finish();
    }
}