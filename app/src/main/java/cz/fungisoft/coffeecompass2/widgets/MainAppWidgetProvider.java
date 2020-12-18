package cz.fungisoft.coffeecompass2.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeFoundService;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.squareup.picasso.Picasso;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Implementation of App Widget functionality.
 */
public class MainAppWidgetProvider extends AppWidgetProvider {

    private static final SimpleDateFormat dateFormater = new SimpleDateFormat("HH:mm");

    public static final String WIDGET_CLICK = "cz.fungisoft.coffeecompass2.widgets.CLICK";
    public static final String SETTINGS_CLICK = "cz.fungisoft.coffeecompass2.widgets.SETTINGS";


    public MainAppWidgetProvider() {
    }


    private static boolean imagesLoaded = false;

    private int[] appWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        imagesLoaded = false;
        this.appWidgetIds = appWidgetIds;

        // First setup default values
        RemoteViews rViews = getRemoteViewsOfWidget(context, appWidgetIds, null);
        // Set update time
        rViews.setTextViewText(R.id.widget_update_time, dateFormater.format(new Date()));
        appWidgetManager.updateAppWidget(appWidgetIds, rViews);

        // Then Update via service
        updateViaService(context, appWidgetIds);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Widget is always updated calling service CoffeeSitesInRangeFoundService
     *
     * @param context
     * @param appWidgetIds
     */
    private void updateViaService(Context context, int[] appWidgetIds) {
        WidgetSettingsPreferenceHelper sharedPref = new WidgetSettingsPreferenceHelper(context);

        Intent intent = new Intent(context, CoffeeSitesInRangeFoundService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.putExtra("searchRange", sharedPref.getSearchDistance());
        intent.putExtra("coffeeSort", "");

        try {
            //context.startService(intent);
            CoffeeSitesInRangeFoundService.enqueueWork(context, intent);
            Log.i("Widget", "CoffeeSitesInRangeFoundService started.");
        }
        catch (Exception ex) {
            Log.e("Widget", ex.getMessage());
            //CoffeeSitesInRangeFoundService.enqueueWork(context, intent);
        }
    }

    // Called by service after finishing its job
    public static void updateCoffeeSiteWidget(Context context, List<? extends CoffeeSite> coffeeSites) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager
                .getAppWidgetIds(new ComponentName(context, MainAppWidgetProvider.class));

        RemoteViews rviews = getRemoteViewsOfWidget(context, appWidgetIds, coffeeSites);
        for (int refreshedWidgetId : appWidgetIds) {
            setRefreshPendingIntent(context, refreshedWidgetId, rviews);
        }

        appWidgetManager.updateAppWidget(appWidgetIds, rviews);
    }

    @Override
    public void onEnabled(Context context) {
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(SETTINGS_CLICK));
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(WIDGET_CLICK));

        // location access permission is required
        if (!(ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            Intent mainActivity = new Intent(context, MainActivity.class);
            context.startActivity(mainActivity);
        }
    }

    /**
     * Handles requests based on user action. I.e. Refresh, Settings and FoundCoffeeSitesListActivity
     * @param ctx
     * @param intent
     */
    @Override
    public void onReceive(Context ctx, Intent intent) {
        WidgetSettingsPreferenceHelper sharedPref = new WidgetSettingsPreferenceHelper(ctx);

        Log.i("Widget", "onReceive() started.");
        final int[] widgetIds = this.appWidgetIds;
        final Context context = ctx;
        final String action = intent.getAction();

        if (action.equals(SETTINGS_CLICK)) {
            // start settings activity
            Intent settings = new Intent(context, WidgetConfigurationActivity.class);
            settings.setAction(SETTINGS_CLICK);
            settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            settings.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
            context.startActivity(settings);
        }

        if (action.equals(WIDGET_CLICK)) {
            // start FoundCoffeeSitesListActivity
            if (Utils.isOnline() || Utils.isOfflineModeOn(ctx)) {
                Intent searching = new Intent(context, FoundCoffeeSitesListActivity.class);
                searching.setAction(WIDGET_CLICK);
                searching.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                searching.putExtra("searchRange", sharedPref.getSearchDistance());
                searching.putExtra("coffeeSort", "");
                context.startActivity(searching);
            } else {
                Toast.makeText(context, context.getResources().getText(R.string.toast_no_internet_no_offline_data), Toast.LENGTH_SHORT).show();
            }
        }

        Log.i("Widget", "onReceive() finished.");
        super.onReceive(ctx, intent);
    }

    private static RemoteViews getRemoteViewsOfWidget(Context context, int[] appWidgetIds, List<? extends CoffeeSite> coffeeSites) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);

        // add pending intents to handle widget click events
        setWidgetClickPendingIntent(context, appWidgetIds, remoteViews);
        setSettingsPendingIntent(context, appWidgetIds, remoteViews);

        // apply current settings
        applySettings(context, remoteViews);

        // display current coffeeSite
        populateData(context, remoteViews, appWidgetIds, coffeeSites);

        return remoteViews;
    }

    //sets pending intent which gets fired on clicking widget
    private static void setWidgetClickPendingIntent(Context context, int[] appWidgetIds, RemoteViews rViews){
        Intent intent = new Intent(context, MainAppWidgetProvider.class);
        intent.setAction(WIDGET_CLICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        rViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
    }

    // sets pending intent which gets fired on settings button
    private static void setSettingsPendingIntent(Context context, int[] appWidgetIds, RemoteViews rViews){
        Intent intent = new Intent(context, MainAppWidgetProvider.class);
        intent.setAction(SETTINGS_CLICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        rViews.setOnClickPendingIntent(R.id.widget_settings_button, pendingIntent);
    }

    // sets pending intent which gets fired on refresh button
    private static void setRefreshPendingIntent(Context context, int appWidgetId, RemoteViews rViews) {
        // Create an Intent with the AppWidgetManager.ACTION_APPWIDGET_UPDATE action
        Intent intentUpdate = new Intent(context, MainAppWidgetProvider.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Update the current widget instance only, by creating an array that contains the widget’s unique ID//
        int[] idArray = new int[] { appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);

        // Wrap the intent as a PendingIntent, using PendingIntent.getBroadcast()//
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Send the pending intent in response to the user tapping the ‘Update’ button
        rViews.setOnClickPendingIntent(R.id.widget_refresh_button, pendingUpdate);
    }

    // applies settings to widgets
    private static void applySettings(Context context, RemoteViews rViews) {
        WidgetSettingsPreferenceHelper sharedPref = new WidgetSettingsPreferenceHelper(context);

        int widgetBackground = 255 - sharedPref.getBackroundOpacity();
        //TODO - nastaveni barev, backround opacity, search distance
//        Color bg = Color.valueOf(context.getResources().g
//        int bg = context.getResources().getColor(R.color.colorPrimaryDark, null);
//        widgetBackground = Color.argb( widgetBackground, bg.toArgb(), bg.toArgb(), bg.toArgb());

        rViews.setInt(R.id.widget_container, "setBackgroundColor", widgetBackground);

        int textColor = sharedPref.getTextColor();
        rViews.setTextColor(R.id.widget_nearest_site_name, ContextCompat.getColor(context, textColor));


    }

    //populates widgets with current place name
    private static void populateData(Context context, RemoteViews remoteViews, int[] appWidgetIds, List<? extends CoffeeSite> coffeeSites) {

        // Default data, if no CoffeeSites found
        remoteViews.setTextViewText(R.id.widget_nearest_site_name, context.getString(R.string.no_site_found));
        remoteViews.setTextViewText(R.id.widget_nearest_site_distance, "");
        remoteViews.setTextViewText(R.id.widget_locAndTypeTextView, "");
        remoteViews.setTextViewText(R.id.widget_coffee_sort_and_price, "");
        remoteViews.setTextViewText(R.id.widget_number_of_other_sites, "");

        if (coffeeSites != null && coffeeSites.size() > 0) {
            remoteViews.setTextViewText(R.id.widget_nearest_site_name, coffeeSites.get(0).getName());
            remoteViews.setTextViewText(R.id.widget_nearest_site_distance, coffeeSites.get(0).getDistance() + " m");
            remoteViews.setTextViewText(R.id.widget_locAndTypeTextView, coffeeSites.get(0).getTypPodniku() + ", " +  coffeeSites.get(0).getTypLokality());
            remoteViews.setTextViewText(R.id.widget_coffee_sort_and_price, coffeeSites.get(0).getCoffeeSortsOneString() + ", " +  coffeeSites.get(0).getCena());
            remoteViews.setTextViewText(R.id.widget_number_of_other_sites, coffeeSites.size() > 1 ? "+" + (coffeeSites.size() - 1) : "" );

            if (!imagesLoaded) {
                if (!Utils.isOfflineModeOn(context)) {
                    Picasso.get().load(coffeeSites.get(0).getMainImageURL())
                            //.resize(100, 150)
                            .into(remoteViews, R.id.widget_nearest_site_image, appWidgetIds);
                } else {
                    Picasso.get().load(ImageUtil.getImageFile(context, ImageUtil.COFFEESITE_IMAGE_DIR, coffeeSites.get(0).getMainImageFileName()))
                            //.resize(100, 75)
                            .into(remoteViews, R.id.widget_nearest_site_image, appWidgetIds);
                }
                imagesLoaded = true;
            }
        }
    }

    //this is used to prevent loop
    //due to work manager ACTION_PACKAGE_CHANGED broadcast
    //which results in a call to onUpdate method
    private static boolean isFoundSitesServiceCalledRecently(Context context) {
        WidgetSettingsPreferenceHelper sharedPref = new WidgetSettingsPreferenceHelper(context);

        long lastApiCallTime = sharedPref.getLastServiceCall();

        if (lastApiCallTime != 0) {
            long currentTime = new Date().getTime();
            double mins = (currentTime - lastApiCallTime)/60000;
            if (mins > 5) {
                sharedPref.putLastServiceCall(currentTime);
                return false;
            }
        } else {
            sharedPref.putLastServiceCall(new Date().getTime());
        }
        return true;
    }

}