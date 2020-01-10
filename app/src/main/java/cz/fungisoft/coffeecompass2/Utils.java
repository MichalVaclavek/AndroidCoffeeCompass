package cz.fungisoft.coffeecompass2;

import android.content.Context;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.function.Consumer;

import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Utility class. Up to now only one method checking if the internet connection is available.
 */
public class Utils {

    private static String TAG = "Utils";

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
    public static RestError getRestError(String restErrorBody) {

        RestError retVal = new RestError();
        try {
            JSONObject jsonObject = new JSONObject(restErrorBody);

            retVal = new RestError(jsonObject.getString("type"),
                                    jsonObject.getString("title"),
                                    jsonObject.getInt("status"),
                                    !jsonObject.getString("detail").equals("null")
                                    ? jsonObject.getString("detail") : "",
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

 public static  String converSearchDistance(int searchRange) {
     // Prevod na km
     if (searchRange >= 1000) {
         return " (" + searchRange/1000 + " km)";
     } else {
         return  " (" + searchRange + " m)";
     }
 }

}
