package cz.fungisoft.coffeecompass2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.FoundCoffeeSitesViewModel;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeFoundService;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateServiceConnector;

/**
 * Implementation of App Widget functionality.
 */
public class MainAppWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_CLICK = "ACTION_CLICK";

    private static FoundCoffeeSitesViewModel foundCoffeeSitesViewModel;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String timeString =
                DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());

        //Construct the RemoteViews object//
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
        //Retrieve and display the time//
        views.setTextViewText(R.id.widget_nearest_site_name,timeString);

        //Create an Intent with the AppWidgetManager.ACTION_APPWIDGET_UPDATE action//
        Intent intentUpdate = new Intent(context, MainAppWidgetProvider.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        //Update the current widget instance only, by creating an array that contains the widget’s unique ID//
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);

        //Wrap the intent as a PendingIntent, using PendingIntent.getBroadcast()//
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(
                context, appWidgetId, intentUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Send the pending intent in response to the user tapping the ‘Update’ TextView//
        views.setOnClickPendingIntent(R.id.widget_refresh_Button, pendingUpdate);

        if (foundCoffeeSitesViewModel != null) {
            List<CoffeeSiteMovable> goro = foundCoffeeSitesViewModel.getCurrentSitesInRange();
            views.setTextViewText(R.id.widget_nearest_site_name, goro.get(0).getName());
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            Toast.makeText(context, "Widget has been updated! ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        foundCoffeeSitesViewModel = FoundCoffeeSitesViewModel.getInstance();
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}