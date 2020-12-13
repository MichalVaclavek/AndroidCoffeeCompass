package cz.fungisoft.coffeecompass2.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.FoundCoffeeSitesViewModel;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * The AppWidgetProvider for our sample weather widget.
 */
public class WidgetDataProvider extends ContentProvider {

    public static final Uri CONTENT_URI =
            Uri.parse("content://cz.fungisoft.coffeecompass2.widgets.provider");

    public static class Columns {
        public static final String ID = "_id";
        public static final String DAY = "day";
        public static final String TEMPERATURE = "temperature";
    }

    private static FoundCoffeeSitesViewModel foundCoffeeSitesViewModel;

    /**
     * Generally, this data will be stored in an external and persistent location (ie. File,
     * Database, SharedPreferences) so that the data can persist if the process is ever killed.
     * For simplicity, in this sample the data will only be stored in memory.
     */
    private static List<CoffeeSiteMovable> sData = new ArrayList<>();

    @Override
    public boolean onCreate() {
        // We are going to initialize the data provider with some default values
//        sData.add(new WeatherDataPoint("Monday", 13));
//        sData.add(new WeatherDataPoint("Tuesday", 1));
//        sData.add(new WeatherDataPoint("Wednesday", 7));
//        sData.add(new WeatherDataPoint("Thursday", 4));
//        sData.add(new WeatherDataPoint("Friday", 22));
//        sData.add(new WeatherDataPoint("Saturday", -10));
//        sData.add(new WeatherDataPoint("Sunday", -13));
//        sData.add(new WeatherDataPoint("Monday", 8));
//        sData.add(new WeatherDataPoint("Tuesday", 11));
//        sData.add(new WeatherDataPoint("Wednesday", -1));
//        sData.add(new WeatherDataPoint("Thursday", 27));
//        sData.add(new WeatherDataPoint("Friday", 27));
//        sData.add(new WeatherDataPoint("Saturday", 27));
//        sData.add(new WeatherDataPoint("Sunday", 27));

        foundCoffeeSitesViewModel = FoundCoffeeSitesViewModel.getInstance();

        return true;
    }

    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder) {
        assert(uri.getPathSegments().isEmpty());
        //sData = foundCoffeeSitesViewModel.getCurrentSitesInRange();
        // In this sample, we only query without any parameters, so we can just return a cursor to
        // all the weather data.
        final MatrixCursor c = new MatrixCursor(
                new String[] { Columns.ID, Columns.DAY, Columns.TEMPERATURE });
        for (int i = 0; i < sData.size(); ++i) {
            final CoffeeSiteMovable data = sData.get(i);
            c.addRow(new Object[]{ new Integer(i), data.getName(), new Long(data.getDistance()) });
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.weatherlistwidget.temperature";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // This example code does not support inserting
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // This example code does not support deleting
        return 0;
    }

    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection,
                                   String[] selectionArgs) {
        assert(uri.getPathSegments().size() == 1);
        //sData = foundCoffeeSitesViewModel.getCurrentSitesInRange();

        // In this sample, we only update the content provider individually for each row with new
        // temperature values.
        final int index = Integer.parseInt(uri.getPathSegments().get(0));
        final MatrixCursor c = new MatrixCursor(
                new String[]{ Columns.ID, Columns.DAY, Columns.TEMPERATURE });
        assert(0 <= index && index < sData.size());
        //final CoffeeSiteMovable data = sData.get(index);
        //data.setDistance(values.getAsInteger(Columns.TEMPERATURE));
        //data.setDistance(sData.get.getAsInteger(Columns.TEMPERATURE));

        // Notify any listeners that the data backing the content provider has changed, and return
        // the number of rows affected.
        getContext().getContentResolver().notifyChange(uri, null);
        return 1;
    }

}
