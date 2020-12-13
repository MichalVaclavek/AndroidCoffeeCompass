package cz.fungisoft.coffeecompass2.widgets;

import android.content.Intent;
import android.content.SharedPreferences;
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
import cz.fungisoft.coffeecompass2.activity.data.SearchDistancePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.WidgetSettingsPreferenceHelper;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class WidgetConfigurationActivity extends AppCompatActivity {

    private LinearLayout container;
    private SeekBar seekBar;
    private RadioGroup rg;

    private TextView siteName = findViewById(R.id.widget_nearest_site_name);

    // Saves selected search distance range
    private WidgetSettingsPreferenceHelper sharedPref;

    private static int searchRange = 500; // range in meters for searching from current position - 500 m default value
    private static  String searchRangeString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        container = findViewById(R.id.widget_container);
        seekBar = findViewById(R.id.widgetBackground);
        rg = findViewById(R.id.widgetColor);

        // Get current serachDistance from Preferences
        sharedPref = new WidgetSettingsPreferenceHelper(this);
        searchRange = sharedPref.getSearchDistance();

        //populate setting-UI elements with the selected values or default values
        seekBar.setMax(100);
        seekBar.setProgress(sharedPref.getBackround());

        rg.check(sharedPref.getSelectedRadio());

        //apply settings to the widget
        //helps in viewing how widget looks with current settings.
        applySettings();

        //handle widget background settings and store in shared preferences
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //save the selected background
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putInt(getString(R.string.widget_background), i);
//                editor.commit();
                sharedPref.putBackround(i);
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

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    //handle text color setting and store in shared preferences
    public void onChooseColor(View view) {
        if (!((RadioButton) view).isChecked()) {
            return;
        }

        int color = android.R.color.black;
        int selectedRadio = R.id.black;

        switch (view.getId()) {
            case R.id.red:
                color = android.R.color.holo_red_dark;
                selectedRadio = R.id.red;
                break;
            case R.id.blue:
                color = android.R.color.holo_blue_dark;
                selectedRadio = R.id.blue;
                break;
            case R.id.green:
                color = android.R.color.holo_green_dark;
                selectedRadio = R.id.green;
                break;
            case R.id.black:
                color = android.R.color.black;
                selectedRadio = R.id.black;
                break;
        }

//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putInt(getString(R.string.widget_color), color);
//        editor.putInt(getString(R.string.widget_color_button), selectedRadio);
//        editor.commit();
        sharedPref.putTextColor(color);
        sharedPref.putSelectedRadio(selectedRadio);

        applySettings();
    }

    //apply selected settings to the widget to see how widget
    //looks with the selected settings.
    private void applySettings() {

        int widgetBackground = sharedPref.getBackround();
        //0 means transparent , 255 opaque
        //convert value to percentage
        int transparentBackground = widgetBackground * 100 / 255;

        int textcolor = sharedPref.getTextColor();

        container.getBackground().setAlpha(transparentBackground);
        siteName.setTextColor(ContextCompat.getColor(getApplicationContext(), textcolor));
    }

    //close settings screen
    public void closeSettings(View v) {
        //update all widgets to apply the slected settings.
        //TODO
        //MainAppWidgetProvider.updateAppWidget(getApplicationContext(), );

        Intent widgetIntent = new Intent();
        setResult(RESULT_OK, widgetIntent);
        finish();
        //System.exit(0);
    }

}