package cz.fungisoft.coffeecompass;

import android.util.Log;

import java.io.IOException;

public class Utils {

    private static String TAG = "Utils";

    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { Log.e(TAG," Problem during internet connection check"); }
        catch (InterruptedException e) { Log.e(TAG," Problem during internet connection check"); }

        return false;
    }

}
