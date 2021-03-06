package cz.fungisoft.coffeecompass2.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntityRepositories;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesService;

/**
 * Receiver of events indicating change of network connectivity.
 * <p>
 * Up to now, used in case the network connectivity becomes available
 * in MainActivity after it was lost or not available on startup.
 */
public class NetworkStateReceiver extends BroadcastReceiver implements InternetCheckAsyncTask.Consumer {

    /**
     * Usualu Activity or Service using this BroadcastReceiver
     */
    private Context context;

    public boolean isOnline() {
        return online;
    }

    /**
     * Main attribute to hold information about internet connectivity status
     */
    private static boolean online = true;  // we expect the app being online when starting

    public static final String TAG = NetworkStateReceiver.class.getSimpleName();

    /**
     * Status of network connetivity has changed. Check if we are connected to internet.
     *
     * @param context
     * @param intent
     */
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Network connectivity change");

        /**
         * Async task to check if the connection to internet is available after the IP network connectivity
         * is fine (this is the event this Receiver is listening too).
         * IP network may be available, but not the internet. It has to b checked subsequently.
         */
        InternetCheckAsyncTask internetCheckAsyncTask = new InternetCheckAsyncTask(this);

        this.context = context;

        if (intent.getExtras() != null) {

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            int networkType = (int) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_TYPE);
            boolean isWiFi = networkType == ConnectivityManager.TYPE_WIFI;
            boolean isMobile = networkType == ConnectivityManager.TYPE_MOBILE;
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(networkType);
            boolean isConnected = networkInfo.isConnected();

            if (isWiFi) {
                if (isConnected) {
                    if (!online) { // We've become online. But is the internet connection available?
                        internetCheckAsyncTask.execute();
                    }
                    Log.i(TAG, "Wi-Fi - CONNECTED");

                } else {
                    Log.i(TAG, "Wi-Fi - DISCONNECTED");
                    online = false;
                }
            } else if (isMobile) {
                if (isConnected) {
                    if (!online) { // We've become online. But is the internet connection available?
                        internetCheckAsyncTask.execute();
                    }
                    Log.i(TAG, "Mobile - CONNECTED");
                } else {
                    Log.i(TAG, "Mobile - DISCONNECTED");
                    online = false;
                }
            } else {
                if (isConnected) {
                    if (!online) { // We've become online. But is the internet connection available?
                        internetCheckAsyncTask.execute();
                    }
                    Log.i(TAG, networkInfo.getTypeName() + " - CONNECTED");
                } else {
                    Log.i(TAG, networkInfo.getTypeName() + " - DISCONNECTED");
                    online = false;
                }
            }
            if (intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                online = false;
                Log.d(TAG, "There's no network connectivity");
            }
        }
    }

    /**
     * Calls MainActivity method to load statistics from server.
     *
     * @param isOnline status of internet connectivity
     * @param context calling context, usually Activity which registered this Receiver
     */
    private void startReadStatisticsInMainActivity(boolean isOnline, Context context) {
        if (isOnline) {
            if (context instanceof MainActivity) {
                MainActivity ma = (MainActivity) context;
                ma.startReadStatistics();
            }
        }
    }

    /**
     * Calls MainActivity method to start loading of CoffeeSite entities from server.
     *
     * @param isOnline status of internet connectivity
     * @param context calling context, usually Activity which registered this Receiver
     */
    private void startLoadCSEntities(boolean isOnline, Context context) {
        if (isOnline) {
            if (context instanceof CoffeeSiteEntitiesService) {
                CoffeeSiteEntitiesService coffeeSiteEntitiesService = (CoffeeSiteEntitiesService) context;
                if (!CoffeeSiteEntityRepositories.isDataReadFromServer()
                      && !Utils.isOfflineModeOn(context)) {
                    coffeeSiteEntitiesService.populateCSEntities();
                }
            }
        }
    }

    /**
     * Calls MainActivity method to start loading number of CoffeeSite created by user.
     *
     * @param isOnline status of internet connectivity
     * @param context calling context, usually Activity which registered this Receiver
     */
    private void startLoadNumberOfSitesOfUserInMainActivity(boolean isOnline, Context context) {
        //TODO zohlednit OFFLINE mode
        if (isOnline) {
            if (context instanceof MainActivity) {
                MainActivity ma = (MainActivity) context;
                if (!ma.isNumberOfCoffeeSitesCreatedByLoggedInUserChecked()) {
                    ma.startNumberOfCoffeeSitesFromUserService();
                }
            }
        }
    }

    // *** Methods implementing InternetCheckAsyncTask.Consumer interface * //
    // Needed to react on internet connectivity AsyncTask check result //

    @Override
    public void accept(Boolean internet) {
        online = internet;
        startReadStatisticsInMainActivity(online, this.context);
        startLoadCSEntities(online, this.context);
        startLoadNumberOfSitesOfUserInMainActivity(online, this.context);
    }

    @Override
    public Context getContext() {
        return this.context;
    }

}
