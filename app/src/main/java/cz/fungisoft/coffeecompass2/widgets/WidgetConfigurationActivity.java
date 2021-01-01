package cz.fungisoft.coffeecompass2.widgets;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
    //private RadioGroup rgBackroundColor;
    //private RadioGroup rgFrameColor;
    private RadioGroup rgDistance;

    //private TextView siteName;

    // Saves selected search distance range
    private WidgetSettingsPreferenceHelper sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //siteName = findViewById(R.id.widget_nearest_site_name);

        //container = findViewById(R.id.widget_settings_container);

        //opacitySeekBar = findViewById(R.id.widget_background_opacity);
        //rgBackroundColor = findViewById(R.id.widget_backround_color);
        //rgFrameColor = findViewById(R.id.widget_frame_color);
        rgDistance = findViewById(R.id.widget_search_distance);

        // Get current searchDistance from Preferences
        sharedPref = new WidgetSettingsPreferenceHelper(this);

        rgDistance.check(sharedPref.getSelectedDistanceRadio());

        // populate setting-UI elements with the selected values or default values
        //opacitySeekBar.setMax(100); // opacity as a percentage of the max. value 255
        //opacitySeekBar.setProgress(sharedPref.getBackroundOpacity());

        //rgBackroundColor.check(sharedPref.getSelectedBackroundColorRadio());
        //rgFrameColor.check(sharedPref.getSelectedFrameColorRadio());

        //apply settings to the widget
        //helps in viewing how widget looks with current settings.
        //applySettings();

        //handle widget background settings and store in shared preferences
        /*
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
*/
        //google places api requires location access peremission
        //checkPermission(ACCESS_FINE_LOCATION);

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

        //applySettings();
    }


    // handle widget frame color setting and store in shared preferences

    // handle backround color setting and store in shared preferences
//    public void onChooseBackroundColor(View view) {
//        if (!((RadioButton) view).isChecked()) {
//            return;
//        }
//
//        int color = android.R.color.black;
//        int selectedRadio = R.id.black_backround;
//
//        switch (view.getId()) {
//            case R.id.red_backround:
//                color = android.R.color.holo_red_dark;
//                selectedRadio = R.id.red_backround;
//                break;
//            case R.id.blue_backround:
//                color = android.R.color.holo_blue_dark;
//                selectedRadio = R.id.blue_backround;
//                break;
//            case R.id.green_backround:
//                color = android.R.color.holo_green_dark;
//                selectedRadio = R.id.green_backround;
//                break;
//            case R.id.black_backround:
//                color = R.color.colorPrimary2;
//                selectedRadio = R.id.black_backround;
//                break;
//        }
//
//        sharedPref.putSelectedBackroundColor(ContextCompat.getColor(this, color));
//        sharedPref.putSelectedBackroundColorRadio(selectedRadio);
//
//        applySettings();
//    }

    // apply selected settings to the widget to see how widget
    // looks with the selected settings.
    private void applySettings() {
        int backgroundAlphaPerc = sharedPref.getBackroundOpacity();
        // 0 means transparent , 255 opaque
        // convert percentage to value
        int transparentBackground = (int) Math.round(2.55 * backgroundAlphaPerc);

        int backroundEndColor = sharedPref.getSelectedBackroundColor();
        int frameColor = sharedPref.getSelectedFrameColor();
        configureWidgetShape(container, backroundEndColor, frameColor, transparentBackground);
    }

    private void configureWidgetShape(View v, int backgroundColor, int frameColor, int alpha) {
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] { backgroundColor, ContextCompat.getColor(this, R.color.activityBackround) } );
        shape.setShape(GradientDrawable.RECTANGLE);
        //shape.setColors(new int[] {R.color.activityBackround, backgroundColor});
        //shape.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shape.setCornerRadius(20);
        shape.setStroke(10, frameColor);
        shape.setAlpha(alpha);
        v.setBackground(shape);
    }

    private int convertDpToPixels(int dp) {
        return getResources().getDimensionPixelSize(dp);
    }

    // close settings screen
    public void closeSettings(View v) {
        //update all widgets to apply the slected settings.
        //MainAppWidgetProvider.updateCoffeeSiteWidget(getApplicationContext(), null);

        Intent widgetIntent = new Intent();
        setResult(RESULT_OK, widgetIntent);
        finish();
    }
}