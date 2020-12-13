package cz.fungisoft.coffeecompass2.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
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

    //public static final String REFRESH_ACTION = "cz.fungisoft.coffeecompass2.widgets.REFRESH";
    public static final String DISPLAY_SITE = "cz.fungisoft.coffeecompass2.widgets.DISPLAY_SITE";
    public static final String WIDGET_CLICK = "cz.fungisoft.coffeecompass2.widgets.CLICK";
    public static final String SETTINGS_CLICK = "cz.fungisoft.coffeecompass2.widgets.SETTINGS";


    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;

    RemoteViews rv;

    public MainAppWidgetProvider() {
        // Start the worker thread
        sWorkerThread = new HandlerThread("MainAppWidgetProvider-worker");
        sWorkerThread.start();
        sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }


    private static boolean imagesLoaded = false;

//    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] appWidgetIds, List<? extends CoffeeSite> coffeeSites) {
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
//
//        // Create an Intent with the AppWidgetManager.ACTION_APPWIDGET_UPDATE action
//        Intent intentUpdate = new Intent(context, MainAppWidgetProvider.class);
//        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//
//        // Update the current widget instance only, by creating an array that contains the widget’s unique ID//
//        int[] idArray = new int[] {appWidgetId};
//        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
//
//        // Wrap the intent as a PendingIntent, using PendingIntent.getBroadcast()//
//        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Send the pending intent in response to the user tapping the ‘Update’ button
//        remoteViews.setOnClickPendingIntent(R.id.widget_refresh_Button, pendingUpdate);
//
//        if (coffeeSites != null && coffeeSites.size() > 0) {
//            remoteViews.setTextViewText(R.id.widget_nearest_site_name, coffeeSites.get(0).getName());
//            remoteViews.setTextViewText(R.id.widget_nearest_site_distance, coffeeSites.get(0).getDistance() + " m");
//            remoteViews.setTextViewText(R.id.widget_locAndTypeTextView, coffeeSites.get(0).getTypPodniku() + ", " +  coffeeSites.get(0).getTypLokality());
//            remoteViews.setTextViewText(R.id.widget_coffee_sort_and_price, coffeeSites.get(0).getCoffeeSortsOneString() + ", " +  coffeeSites.get(0).getCena());
//            remoteViews.setTextViewText(R.id.widget_number_of_other_sites, coffeeSites.size() > 1 ? "+" + (coffeeSites.size() - 1) : "" );
//
//            if (!imagesLoaded) {
//                if (!Utils.isOfflineModeOn(context)) {
//                    Picasso.get().load(coffeeSites.get(0).getMainImageURL())
//                            //.resize(100, 150)
//                            .into(remoteViews, R.id.widget_nearest_site_image, appWidgetIds);
//                } else {
//                    Picasso.get().load(ImageUtil.getImageFile(context, ImageUtil.COFFEESITE_IMAGE_DIR, coffeeSites.get(0).getMainImageFileName()))
//                            //.resize(100, 75)
//                            .into(remoteViews, R.id.widget_nearest_site_image, appWidgetIds);
//                }
//                imagesLoaded = true;
//            }
//        }
//
//        //String time = new Date();
//        remoteViews.setTextViewText(R.id.widget_number_of_other_sites, coffeeSites.size() > 1 ? "+" + (coffeeSites.size() - 1) : "" );
//
//        // Instruct the widget manager to update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
//        Log.i("Widget", "updateAppWidget() performed.");
//    }

    private int[] appWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        imagesLoaded = false;
//        for (int i = 0; i < appWidgetIds.length; ++i) {
//           //RemoteViews layout = buildLayout(context, appWidgetIds[i], mIsLargeLayout);
//            //appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
//            updateAppWidget(context, appWidgetManager, i, appWidgetIds, null);
//            //Toast.makeText(context, context.getResources().getText(R.string.widget_confirm_toast), Toast.LENGTH_SHORT).show();
//            setRefreshPendingIntent(context, i);
//        }

        this.appWidgetIds = appWidgetIds;
        //this logic prevents infinite loop which is caused due to
        //the broadcast of ACTION_PACKAGE_CHANGED in the process of
        // idle resource cleanup by the work manager
        if(isLocationWorkCalledRecently(context.getApplicationContext())){
            return;
        }
        // Updates via service
        updateViaService(context, appWidgetIds);
        //schedule site fetcher job
        //LocationScheduler.scheduleGetPlacesWork(appWidgetIds);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    // update location widgets
    public static void updateCoffeeSiteWidget(Context context, List<? extends CoffeeSite> coffeeSites, int refreshedWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager
                .getAppWidgetIds(new ComponentName(context, MainAppWidgetProvider.class));

        RemoteViews rviews = getRemoteViewsOfWidget(context, appWidgetIds, coffeeSites);
        setRefreshPendingIntent(context, refreshedWidgetId, rviews);

        appWidgetManager.updateAppWidget(appWidgetIds, rviews);
    }

    private void updateViaService(Context context, int[] appWidgetIds) {
        Intent intent = new Intent(context, CoffeeSitesInRangeFoundService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        try {
            context.startService(intent);
            Log.i("Widget", "CoffeeSitesInRangeFoundService started.");
        }
        catch (IllegalStateException ex) {
            Log.e("Widget", ex.getMessage());
            CoffeeSitesInRangeFoundService.enqueueWork(context, intent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(SETTINGS_CLICK));
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(WIDGET_CLICK));

        //location access permission is required
        if (!(ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)) {
            Intent mainActivity = new Intent(context, MainActivity.class);
            context.startActivity(mainActivity);
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        Log.i("Widget", "onReceive() started.");
        final int[] widgetIds = this.appWidgetIds;
        final Context context = ctx;
        final String action = intent.getAction();

        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            // BroadcastReceivers have a limited amount of time to do work, so for this sample, we
            // are triggering an update of the data on another thread.  In practice, this update
            // can be triggered from a background service, or perhaps as a result of user actions
            // inside the main application.
            sWorkerQueue.removeMessages(0);
            sWorkerQueue.post(new Runnable() {
                @Override
                public void run() {
                    // Updates via service
                    updateViaService(context, widgetIds);
                }
            });
        }

        if (intent.getAction().equals(SETTINGS_CLICK)) {
            //start settings activity
            Intent settings = new Intent(context, WidgetConfigurationActivity.class);
            settings.setAction(SETTINGS_CLICK);
            context.startActivity(settings);
        }

        if (intent.getAction().equals(WIDGET_CLICK)) {
            //start settings activity
            Intent settings = new Intent(context, FoundCoffeeSitesListActivity.class);
            settings.setAction(WIDGET_CLICK);
            context.startActivity(settings);
        }

        Log.i("Widget", "onReceive() finished.");

    }

    private static RemoteViews getRemoteViewsOfWidget(Context context, int[] appWidgetIds, List<? extends CoffeeSite> coffeeSites) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);

        // add pending intents to handle widget click events
        setWidgetClickPendingIntent(context, appWidgetIds, remoteViews);
        setSettingsPendingIntent(context, appWidgetIds, remoteViews);

        //apply current settings
        applySettings(context, appWidgetIds, remoteViews);

        //display current coffeeSite
        populateData(context, remoteViews, appWidgetIds, coffeeSites);

        return remoteViews;
    }

    //sets pending intent which gets fired on clicking widget
    private static void setWidgetClickPendingIntent(Context context, int[] appWidgetIds, RemoteViews rviews){
        Intent intent = new Intent(context, MainAppWidgetProvider.class);
        intent.setAction(WIDGET_CLICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        rviews.setOnClickPendingIntent(R.id.container, pendingIntent);
    }

    // sets pending intent which gets fired on settings button
    private static void setSettingsPendingIntent(Context context, int[] appWidgetIds, RemoteViews rviews){
        Intent intent = new Intent(context, MainAppWidgetProvider.class);
        intent.setAction(WIDGET_CLICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        rviews.setOnClickPendingIntent(R.id.widget_settings_button, pendingIntent);
    }

    // sets pending intent which gets fired on refresh button
    private static void setRefreshPendingIntent(Context context, int appWidgetId, RemoteViews rviews) {
        // Create an Intent with the AppWidgetManager.ACTION_APPWIDGET_UPDATE action
        Intent intentUpdate = new Intent(context, MainAppWidgetProvider.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Update the current widget instance only, by creating an array that contains the widget’s unique ID//
        int[] idArray = new int[] {appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);

        // Wrap the intent as a PendingIntent, using PendingIntent.getBroadcast()//
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Send the pending intent in response to the user tapping the ‘Update’ button
        rviews.setOnClickPendingIntent(R.id.widget_refresh_Button, pendingUpdate);
    }

    //applies settings to widgets
    private static void applySettings(Context context, int[] appWidgetIds, RemoteViews rviews) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.location_pref), Context.MODE_PRIVATE);

        int widgetBackground = 255 - sharedPref.getInt(
                context.getString(R.string.widget_background), 255);
        Color bg = Color.valueOf(context.getResources().getColor(R.color.widgetBg, null));
        widgetBackground = Color.argb((float)widgetBackground, bg.red(), bg.green(), bg.blue() );

        rviews.setInt(R.id.container, "setBackgroundColor", widgetBackground);

        int textcolor = sharedPref.getInt(context.getString(R.string.widget_color), R.id.black);
        rviews.setTextColor(R.id.location_name, ContextCompat.getColor(context, textcolor));
    }

    //populates widgets with current place name
    private static void populateData(Context context, RemoteViews remoteViews, int[] appWidgetIds, List<? extends CoffeeSite> coffeeSites) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.location_pref), Context.MODE_PRIVATE);
        if(placeName == null){
            placeName = sharedPref.getString(context.getString(R.string.place), placeName);
        }else{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(context.getString(R.string.place), placeName);
            editor.commit();
        }

        if(placeName == null || placeName.isEmpty()){
            placeName = DEFAULT_LOCATION;
        }

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

        String time = "10:00";

        remoteViews.setTextViewText(R.id.widget_update_time, time);
    }

    //this is used to prevent loop
    //due to work manager ACTION_PACKAGE_CHANGED broadcast
    //which results in a call to onUpdate method
    private static boolean isLocationWorkCalledRecently(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.location_pref), Context.MODE_PRIVATE);

        long lastApiCallTime = sharedPref.getLong(
                context.getString(R.string.last_api_call), 0);

        if(lastApiCallTime != 0){
            long currentTime = new Date().getTime();
            double mins = (currentTime - lastApiCallTime)/60000;
            if(mins > 5){
                sharedPref.edit().putLong(context.getString(R.string.last_api_call),
                        currentTime).commit();
                return false;
            }
        }else{
            sharedPref.edit().putLong(context.getString(R.string.last_api_call),
                    new Date().getTime()).commit();
        }
        return true;
    }

}