
package cz.fungisoft.coffeecompass2.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.WidgetSettingsPreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeWidgetService;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.squareup.picasso.Picasso;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Implementation of App Widget functionality.
 * Shows the current nearest CoffeeSite found.
 * Uses mainly CoffeeSitesInRangeWidgetService to get current CoffeeSites in the search range
 * and location.
 */
public class MainAppWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "Widget";

    /**
     * Format of the last widget update time.
     */
    private static final SimpleDateFormat dateFormater = new SimpleDateFormat("HH:mm");

    public static final String REFRESH_CLICK = "cz.fungisoft.coffeecompass2.widgets.REFRESH";
    public static final String WIDGET_CLICK = "cz.fungisoft.coffeecompass2.widgets.CLICK";
    public static final String SETTINGS_CLICK = "cz.fungisoft.coffeecompass2.widgets.SETTINGS";

    public static String picturePath = "";
    public static int coffeeSiteId = 0;

    public MainAppWidgetProvider() {
    }

    private int[] appWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        this.appWidgetIds = appWidgetIds;

        // First setup default values
        RemoteViews rViews = getRemoteViewsOfWidget(context, appWidgetIds, null);
        // Set update time
        rViews.setTextViewText(R.id.widget_update_time, dateFormater.format(new Date()));

        // Then Update via service
        updateViaService(context, appWidgetIds, false);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private static Intent sitesInRangeServiceIntent;
    private static boolean serviceStarted = false;

    /**
     * Widget is always updated by calling of the service {@link CoffeeSitesInRangeWidgetService}
     *
     * @param context
     * @param appWidgetIds
     */
    private void updateViaService(Context context, int[] appWidgetIds, boolean invokedByUser) {
        WidgetSettingsPreferenceHelper sharedPref = new WidgetSettingsPreferenceHelper(context);

        sitesInRangeServiceIntent = new Intent(context, CoffeeSitesInRangeWidgetService.class);
        sitesInRangeServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sitesInRangeServiceIntent.putExtra("searchRange", sharedPref.getSearchDistance());
        sitesInRangeServiceIntent.putExtra("coffeeSort", "");
        sitesInRangeServiceIntent.putExtra("serviceInvokedByUser", invokedByUser);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                if (!serviceStarted) {
                    context.startForegroundService(sitesInRangeServiceIntent);
                    serviceStarted = true;
                    Log.i(TAG, "CoffeeSitesInRangeWidgetService started.");
                }
            } else {
                CoffeeSitesInRangeWidgetService.enqueueWork(context, sitesInRangeServiceIntent);
                Log.i(TAG, "CoffeeSitesInRangeWidgetService started - enqueueWork()");
            }

        }
        catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /**
    *  Called by {@link CoffeeSitesInRangeWidgetService}, after finishing its job
    *  or by {@link WidgetConfigurationActivity} after closing (with coffeeSites param. set to null).
     *
     * @param coffeeSites list of nearest CoffeeSites as found by CoffeeSitesInRangeWidgetService
     * @param searchingFinished to indicate if the service, calling this method, already finished searching - then the service can be stopped as it was called by this MainAppWidgetprovider
    */
    public static void updateCoffeeSiteWidget(Context context, List<? extends CoffeeSite> coffeeSites, boolean searchingFinished) {
        if (coffeeSites != null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager
                    .getAppWidgetIds(new ComponentName(context, MainAppWidgetProvider.class));

            RemoteViews rViews = getRemoteViewsOfWidget(context, appWidgetIds, coffeeSites);
            for (int refreshedWidgetId : appWidgetIds) {
                setRefreshPendingIntent(context, refreshedWidgetId, rViews);
            }
            // Set update time
            rViews.setTextViewText(R.id.widget_update_time, dateFormater.format(new Date()));
            appWidgetManager.updateAppWidget(appWidgetIds, rViews);
        }

        if (searchingFinished && serviceStarted) {
            context.stopService(sitesInRangeServiceIntent);
            serviceStarted = false;
        }
    }

    @Override
    public void onEnabled(Context context) {
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(SETTINGS_CLICK));
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(WIDGET_CLICK));

        // location access permission is required
        if (!(ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Intent mainActivity = new Intent(context, MainActivity.class);
            context.startActivity(mainActivity);
        }
    }

    /**
     * Handles requests based on user action. I.e. Refresh, Settings and open {@link FoundCoffeeSitesListActivity}.
     *
     * @param ctx
     * @param intent
     */
    @Override
    public void onReceive(Context ctx, Intent intent) {
        WidgetSettingsPreferenceHelper sharedPref = new WidgetSettingsPreferenceHelper(ctx);

        Log.i(TAG, "onReceive() started.");
        final int[] widgetIds = this.appWidgetIds;
        final Context context = ctx;
        final String action = intent.getAction();

        if (action.equals(REFRESH_CLICK)) {  // start refresh using CoffeeSitesInRangeFoundService
            try {
                updateViaService(context, widgetIds, true);
            }
            catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        }

        if (action.equals(SETTINGS_CLICK)) { // start settings activity
            Intent settings = new Intent(context, WidgetConfigurationActivity.class);
            settings.setAction(SETTINGS_CLICK);
            settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            settings.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
            context.startActivity(settings);
        }

        if (action.equals(WIDGET_CLICK)) { // start FoundCoffeeSitesListActivity
            if (Utils.isOnline(ctx) || Utils.offlineDataAvailable(ctx)) {
                Intent searching = new Intent(context, FoundCoffeeSitesListActivity.class);
                searching.setAction(WIDGET_CLICK);
                searching.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                searching.putExtra("searchRange", sharedPref.getSearchDistance());
                searching.putExtra("coffeeSort", "");
                context.startActivity(searching);
                Log.i(TAG, "Started FoundCoffeeSitesListActivity");
            } else {
                Utils.showNoInternetNoOfflineDataToast(ctx);
            }
        }

        Log.i(TAG, "onReceive() finished.");
        super.onReceive(ctx, intent);
    }

    /**
     * Prepares RemoteViews object of the Widget, applies current widget VIew settings and
     * inserts current data to be displayed by the Widget
     *
     * @param context
     * @param appWidgetIds
     * @param coffeeSites
     * @return
     */
    private static RemoteViews getRemoteViewsOfWidget(Context context, int[] appWidgetIds, List<? extends CoffeeSite> coffeeSites) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);

        // add pending intents to handle widget click events
        setWidgetClickPendingIntent(context, appWidgetIds, remoteViews);
        setSettingsPendingIntent(context, appWidgetIds, remoteViews);

        // apply current settings
        //applySettings(context, remoteViews);

        // display current coffeeSite
        populateData(context, remoteViews, appWidgetIds, coffeeSites);

        return remoteViews;
    }

    /**
     * Sets pending intent which gets fired on clicking widget body. Processed by onReceive() then.
     */
    private static void setWidgetClickPendingIntent(Context context, int[] appWidgetIds, RemoteViews rViews){
        Intent intent = new Intent(context, MainAppWidgetProvider.class);
        intent.setAction(WIDGET_CLICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        rViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
    }

    /**
     * Sets pending intent which gets fired on settings button of the Widget. Processed by onReceive() then.
     */
    private static void setSettingsPendingIntent(Context context, int[] appWidgetIds, RemoteViews rViews){
        Intent intent = new Intent(context, MainAppWidgetProvider.class);
        intent.setAction(SETTINGS_CLICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        rViews.setOnClickPendingIntent(R.id.widget_settings_button, pendingIntent);
    }

    /**
     * Sets pending intent which gets fired on refresh button of the Widget. Processed by onReceive() then.
     */
    private static void setRefreshPendingIntent(Context context, int appWidgetId, RemoteViews rViews) {
        // Create an Intent with the AppWidgetManager.ACTION_APPWIDGET_UPDATE action
        Intent intentUpdate = new Intent(context, MainAppWidgetProvider.class);
        intentUpdate.setAction(REFRESH_CLICK);

        // Update the current widget instance only, by creating an array that contains the widget’s unique ID//
        int[] idArray = new int[] { appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);

        // Wrap the intent as a PendingIntent, using PendingIntent.getBroadcast()//
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Send the pending intent in response to the user tapping the ‘Update’ button
        rViews.setOnClickPendingIntent(R.id.widget_refresh_button, pendingUpdate);
    }

    /**
     * Applies settings to widgets ... Not yet used as the Widget's view is difficult to change ...
     */
    private static void applySettings(Context context, RemoteViews rViews) {
        WidgetSettingsPreferenceHelper sharedPref = new WidgetSettingsPreferenceHelper(context);

        int backroundOpacity = sharedPref.getBackroundOpacity();
        //TODO - nastaveni barev a backround opacity z sharedPref
//        Color bg = Color.valueOf(context.getResources().g
//        int bg = context.getResources().getColor(R.color.colorPrimaryDark, null);
//        backroundOpacity = Color.argb( backroundOpacity, bg.toArgb(), bg.toArgb(), bg.toArgb());

        int color = sharedPref.getSelectedBackroundColor();

        int backroundColor = 255 - sharedPref.getBackroundOpacity();
        //Color bg = Color.valueOf(context.getResources().getColor(color, null));
        //backroundColor = Color.argb((float)backroundColor, bg.red(), bg.green(), bg.blue() );

        //rViews.setInt(R.id.widget_container, "setBackgroundColor", backroundColor);
        int frameColor = sharedPref.getSelectedFrameColor();
        rViews.setImageViewBitmap(R.id.widget_container, getBackground(color, backroundOpacity, frameColor, 180, 120, context));

        // Not settable in WidgetConfigurationActivity, yet
        int textColor = sharedPref.getTextColor();
        rViews.setTextColor(R.id.widget_nearest_site_name, ContextCompat.getColor(context, textColor));
    }

    public static Bitmap getBackground(int bgColor, int alpha, int frameColor, int width, int height, Context context) {
        try {
            // convert to HSV to lighten and darken
            float[] hsv = new float[3];
            Color.colorToHSV(bgColor, hsv);
            hsv[2] -= .1;
            int darker = Color.HSVToColor(alpha, hsv);
            hsv[2] += .3;
            int lighter = Color.HSVToColor(alpha, hsv);

            // create gradient useng lighter and darker colors
            GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] { darker, lighter} );
            shape.setShape(GradientDrawable.RECTANGLE);
            // set corner size
            shape.setCornerRadii(new float[] {4,4,4,4,4,4,4,4});

            shape.setStroke(10, frameColor);

            // get density to scale bitmap for device
            float dp = context.getResources().getDisplayMetrics().density;

            // create bitmap based on width and height of widget
            Bitmap bitmap = Bitmap.createBitmap(Math.round(width * dp), Math.round(height * dp),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas =  new Canvas(bitmap);
            shape.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            shape.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Populates widget's view elements with current CoffeeSite data.
     */
    private static void populateData(Context context, RemoteViews remoteViews, int[] appWidgetIds,
                                     List<? extends CoffeeSite> coffeeSites) {

        // Default data, if no CoffeeSites found
        remoteViews.setTextViewText(R.id.widget_nearest_site_name, "");
        remoteViews.setTextViewText(R.id.widget_nearest_site_distance, "");
        remoteViews.setTextViewText(R.id.widget_locAndTypeTextView, context.getString(R.string.widget_no_coffee));
        remoteViews.setTextViewText(R.id.widget_coffee_sort_and_price, "");
        remoteViews.setTextViewText(R.id.widget_number_of_other_sites, "");
        // default image - used when there was CoffeeSite with image nad there is no CoffeeSite
        // or CoffeeSite without image, now
        Picasso.get().load(R.drawable.kafe_backround_120x160)
                     .into(remoteViews, R.id.widget_nearest_site_image, appWidgetIds);

        if (coffeeSites != null && coffeeSites.size() > 0) {
            remoteViews.setTextViewText(R.id.widget_nearest_site_name, coffeeSites.get(0).getName());
            remoteViews.setTextViewText(R.id.widget_nearest_site_distance, coffeeSites.get(0).getDistance() + " m");
            remoteViews.setTextViewText(R.id.widget_locAndTypeTextView, coffeeSites.get(0).getTypPodniku() + ", " +  coffeeSites.get(0).getTypLokality());
            //remoteViews.setTextViewText(R.id.widget_locAndTypeTextView, String.valueOf(coffeeSites.get(0).getTypLokality()));
            //remoteViews.setTextViewText(R.id.widget_coffee_sort_and_price, coffeeSites.get(0).getCoffeeSortsOneString() + ", " +  coffeeSites.get(0).getCena());
            if (coffeeSites.get(0).getCena() != null) { // not obligatory, can be null
                remoteViews.setTextViewText(R.id.widget_coffee_sort_and_price, String.valueOf(coffeeSites.get(0).getCena()));
            }
            remoteViews.setTextViewText(R.id.widget_number_of_other_sites, coffeeSites.size() > 1 ? "+" + (coffeeSites.size() - 1) : "" );

            picturePath = Utils.isOfflineModeOn(context) ? coffeeSites.get(0).getMainImageFilePath()
                                                         : coffeeSites.get(0).getMainImageURL();
            if (!picturePath.isEmpty()) {
                if (!Utils.isOfflineModeOn(context)) {
                    Picasso.get().load(picturePath)
                            .resize(270, 360)
                            .into(remoteViews, R.id.widget_nearest_site_image, appWidgetIds);
                } else {
                    Picasso.get().load(ImageUtil.getImageFile(context, ImageUtil.COFFEESITE_IMAGE_DIR, picturePath))
                            .resize(270, 360)
                            .into(remoteViews, R.id.widget_nearest_site_image, appWidgetIds);
                }
            }
        }
    }

}