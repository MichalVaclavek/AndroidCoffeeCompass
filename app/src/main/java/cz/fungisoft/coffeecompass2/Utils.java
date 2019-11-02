package cz.fungisoft.coffeecompass2;

import android.util.Log;

import java.io.IOException;

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

}
