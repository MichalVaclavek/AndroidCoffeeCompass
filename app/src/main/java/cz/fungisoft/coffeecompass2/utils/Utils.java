package cz.fungisoft.coffeecompass2.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Utility class.
 */
public class Utils {

    private static String TAG = "Utils";

    /**
     * Checks if the connection to internet is available.
     * Basic method, can be used in UI thread.
     *
     * @return
     */
    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8"); // 8.8.8.8 is google.com
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { Log.e(TAG," Problem during internet connection check."); }
        catch (InterruptedException e) { Log.e(TAG," Problem during internet connection check"); }

        return false;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    /**
     * Check internet connection availability.
     * Must run in AsyncTask, i.e. not in UI thread.
     * Here we use {@link InternetCheckAsyncTask} to call this method.
     *
     * @param context
     * @return
     */
    public static boolean hasInternetAccess(Context context) {

        if (isNetworkAvailable(context)) {

            try {
                HttpURLConnection urlc = (HttpURLConnection)
                                         (new URL("http://clients3.google.com/generate_204")
                                         .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, context.getString(R.string.toast_no_internet));
        }
        return false;
    }

    /**
     * Show info Toast message, that internet connection is not available
     */
    public static void showNoInternetToast(Context appContext) {
        Toast toast = Toast.makeText(appContext,
                R.string.toast_no_internet,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    public static String getDeviceID(AppCompatActivity parentActivity) {
        return Settings.Secure.getString(parentActivity.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * Vrati instanci REST error z JSON chybove zpravy vracenou serverem
     * @param restErrorBody
     * @return
     */
    public static synchronized RestError getRestError(String restErrorBody) {

        RestError retVal = new RestError();
        try {
            JSONObject jsonObject = new JSONObject(restErrorBody);

            retVal = new RestError(jsonObject.getString("type"),
                                    jsonObject.getString("title"),
                                    jsonObject.getInt("status"),
                                    !jsonObject.getString("detail").equals("null")
                                        ? jsonObject.getString("detail")
                                        : "",
                                    jsonObject.getString("instance"));

            if (jsonObject.getString("errorParameter") != null) {
                retVal.setErrorParameter(jsonObject.getString("errorParameter"));
            }
            if (jsonObject.getString("errorParameterValue") != null) {
                retVal.setErrorParameterValue(jsonObject.getString("errorParameterValue"));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return retVal;
    }

    public static  String convertSearchDistance(int searchRange) {
        // Prevod na km
        return (searchRange >= 1000) ? " (" + searchRange/1000 + " km)"
                                     : " (" + searchRange + " m)";
    }

    public static  String convertSearchDistanceNoBrackets(int searchRange) {
        // Prevod na km
        return (searchRange >= 1000) ? searchRange/1000 + " km"
                                     : searchRange + " m";
    }


    /**
     * Get real file path from URI
     */
    public static String getRealPathFromUri(Uri contentUri, Context appContext) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = appContext.getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Rounds double or float to given number of decimal places
     *
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Returns distnace in meters in following format:
     *
     * 0 m like - m
     * up tp 999 m
     * over 1000 m lke 1.1 km and so on
     * over/equal 10 000 m like whole  kilometers
     * @param distance
     * @return
     */
    public static String getDistanceInBetterReadableForm(long distance) {

        if (distance == 0) {
            return "- m";
        }
        if (distance > 0 && distance < 1000) {
            return distance + " m";
        }
        if (distance >= 1000 && distance < 10000) {
            return Utils.round(distance / 1000d, 1) + " km";
        }
        if (distance >= 10000) {
            return (Math.round(distance / 1000d)) + " km";
        }
        return "- m";
    }

    /**
     * Pomocna metoda pro vypocet vzdalenosti mezi 2 body na mape/globu. Souradnice bodu ve formatu double.
     * Prevzato ze stackoverflow.com
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     *
     * @return vzdalenost 2 zadanych bodu v metrech
     */
    public static long countDistanceMetersFromSearchPoint(double lat1, double lon1, double lat2, double lon2) {
        long eRadius = 6372000; // polomer Zeme v metrech, v CR?
        long distance;
        double c, a;

        double latDist = Math.toRadians( lat2 - lat1 );
        double lonDist = Math.toRadians( lon2 - lon1 );
        a = Math.pow( Math.sin( latDist/2 ), 2 ) + Math.cos( Math.toRadians( lat1 ) ) * Math.cos( Math.toRadians( lat2 ) ) * Math.pow( Math.sin( lonDist / 2 ), 2 );
        c  = 2 * Math.atan2( Math.sqrt( a ), Math.sqrt( 1 - a ) );

        distance = Math.round(eRadius * c);

        return distance;
    }

}
