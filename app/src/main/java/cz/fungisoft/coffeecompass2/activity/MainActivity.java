package cz.fungisoft.coffeecompass2.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.lukelorusso.verticalseekbar.VerticalSeekBar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import cz.fungisoft.coffeecompass2.FunctionalUtils;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.data.SearchDistancePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.UserDataViewActivity;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.ReadStatsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.Statistics;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceConnectionListener;

/**
 * Main activity to show:
 *  - search buttons to find either ESPRESSO CoffeeSites only or any CoffeeSite within specified distance range
 *  - info about location of the phone and accuracy of this location
 *  - basic statistics info about CoffeeSites and Users
 *
 *  Is capable to detect it's current location to allow searching of CoffeeSites based on current location.
 */
public class MainActivity extends ActivityWithLocationService implements PropertyChangeListener, UserLoginServiceConnectionListener {

    private static final int LOCATION_REQUEST_CODE = 101;
    private static final String TAG = "MainActivity";

    private static final long MAX_STARI_DAT = 1000 * 60; // pokud jsou posledni zname udaje o poloze starsi jako 2 minuty, zjistit nove (po spusteni app.)
    private static final float GOOD_PRESNOST = 10.0f;
    private static final float LAST_PRESNOST = 500.0f;

    private boolean bPrvni = true;
    private int barvaBlack = Color.BLACK;
    private int barvaRed = Color.RED;

    private TextView accuracy;

    private ImageView locationImageView;

    private Location location;

//    private Button searchEspressoButton;
    private static Button searchKafeButton;


    private Toolbar mainToolbar;

    private VerticalSeekBar searchDistanceSeekBar;

    private LinearLayout  searchDistancesScaleLinearLayout;

    private static int searchRange = 500; // range in meters for searching from current position - 500 m default value
    private static  String searchRangeString;

    // Saves selected search distance range
    private static SearchDistancePreferenceHelper searchRangePreferenceHelper;

    /**
     * Service to perform operations with CoffeeSite.
     * Saves, updates, deletes CoffeeSite and related
     * CoffeeSiteEntities
     */
    private CoffeeSiteService coffeeSiteService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get serachDistance from Preferences
        searchRangePreferenceHelper = new SearchDistancePreferenceHelper(this);
        searchRange = searchRangePreferenceHelper.getSearchDistanc();

        searchKafeButton = (Button) findViewById(R.id.searchKafeButton);

        searchDistanceSeekBar = (VerticalSeekBar) findViewById(R.id.searchDistanceSeekBar);

        //searchDistanceSeekBar.setThumbPlaceholderDrawable(getDrawable(R.drawable.cup_basic));
        String[] vzdalenosti = getResources().getStringArray(R.array.vzdalenosti);
        // Seek bar for selecting searching distance
        searchDistanceSeekBar.setMaxValue(vzdalenosti.length-1);

        searchDistancesScaleLinearLayout = findViewById(R.id.searchDistancesScaleLinearLayout);
        // Text view for searchDistances to be accessible for changing its property when selected by seekbar
        TextView[] searchDistanceTextViews = new TextView[vzdalenosti.length];

        for (int i = vzdalenosti.length-1; i >= 0 ; i--) {
            TextView searchDistTextView = new TextView(this);
            searchDistTextView.setText(vzdalenosti[i]);
            searchDistTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            searchDistTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            searchDistanceTextViews[i] = searchDistTextView;
            searchDistancesScaleLinearLayout.addView(searchDistTextView);
        }
        // find selected searchRangeTextView to be highlited
        for (int i = vzdalenosti.length-1; i >= 0 ; i--) {
            if (String.valueOf(searchRange).equals(vzdalenosti[i])) {
                searchDistanceSeekBar.setProgress(i);
                searchDistanceTextViews[i].setTypeface(null, Typeface.BOLD);
                searchDistanceTextViews[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                searchDistanceTextViews[i].setBackgroundResource(R.color.selectedSearchDistanceBackround);
                break;
            }
        }

        // SeekBar onChangeListener()
        searchDistanceSeekBar.setOnProgressChangeListener(FunctionalUtils.fromConsumer((progress) -> {
            for (int i = searchDistanceTextViews.length-1; i >= 0 ; i--) {
                if (i == progress) {
                    searchDistanceTextViews[i].setTypeface(null, Typeface.BOLD);
                    searchDistanceTextViews[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    searchDistanceTextViews[i].setBackgroundResource(R.color.selectedSearchDistanceBackround);
                } else {
                    searchDistanceTextViews[i].setTypeface(null, Typeface.NORMAL);
                    searchDistanceTextViews[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    searchDistanceTextViews[i].setBackgroundResource(R.color.activityBackround);
                }
            }
            searchRange = Integer.parseInt(vzdalenosti[progress]);
            searchRangeString = Utils.convertSearchDistance(searchRange);
            searchKafeButton.setText(Html.fromHtml("KÁVA<br><small>" + searchRangeString + "</small>" ));
            searchRangePreferenceHelper.putSearchDistance(searchRange);

        }));

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        findViewById(R.id.AllSitesTextView);

        //Location info
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE);

        accuracy = (TextView) findViewById(R.id.accuracy);

        searchRangeString = Utils.convertSearchDistance(searchRange);

        //TODO - text from R.string. ...
        searchKafeButton.setTransformationMethod(null);
        searchKafeButton.setText(Html.fromHtml("KÁVA<br><small>" + searchRangeString + "</small>" ));

        locationImageView = (ImageView) findViewById(R.id.locationImageView);

        //Drawable locBad = getResources().getDrawable(R.drawable.location_bad);
        Drawable locBad = ResourcesCompat.getDrawable(getResources(), R.drawable.location_bad, null);
        locationImageView.setBackground(locBad);

        // Floating action button to open Activity for creating new CoffeeSite
        FloatingActionButton fab = findViewById(R.id.add_site_floatingActionButton);

        // effective final this activity instance for annonymous onClick() handler
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userAccountService != null && userAccountService.isUserLoggedIn()) {
                    openNewCoffeeSiteActivity();
                } else {
                    Snackbar mySnackbar = Snackbar.make(view, R.string.new_site_only_for_registered_user, Snackbar.LENGTH_LONG);
                    mySnackbar.show();
                }
            }
        });
        fab.setVisibility(View.VISIBLE);

        // UserLoginAndRegister service connection
        doBindUserLoginService();

        loadCoffeeSiteEntitiesFromServer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(coffeeSiteServiceReciever);
    }


    /**
     * Start service operation loading all CoffeeSiteEntity instancies
     * from server.
     */
    private void loadCoffeeSiteEntitiesFromServer() {
        registerCoffeeSiteEntitiesLoadReceiver();
        startCoffeeSiteService();
    }

    private CoffeeSiteServiceReciever coffeeSiteServiceReciever;

    public void startCoffeeSiteService() {

        if (Utils.isOnline()) {
            Intent cfServiceIntent = new Intent();
            cfServiceIntent.setClass(this, CoffeeSiteService.class);
            cfServiceIntent.putExtra("operation_type", CoffeeSiteService.COFFEE_SITE_ENTITIES_LOAD);
            startService(cfServiceIntent);
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    private void registerCoffeeSiteEntitiesLoadReceiver() {
        coffeeSiteServiceReciever = new CoffeeSiteServiceReciever();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoffeeSiteService.COFFEE_SITE_ENTITY );

        LocalBroadcastManager.getInstance(this).registerReceiver(coffeeSiteServiceReciever, intentFilter);
    }

        private class CoffeeSiteServiceReciever extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String result = intent.getStringExtra("operationResult");
                Log.i(TAG, "Load CoffeeSiteEntities operation result: " + result);
            }
        }


    /**
     * Method to update color of location Accuracy indicator image according
     * current accuracy value.
     * <br>
     * If the accuracy is not known or too old, then it has default RED color
     * if the accuracy is known, but higher than GOOD_PRESNOST, then it has ORANGE (R.drawable.location_better) color
     * if the accuracy is known, and lower than GOOD_PRESNOST, then it has GREEN (R.drawable.location_good) color
     *
     * @param location
     */
    private void updateAccuracyIndicator(Location location) {

        Drawable  locIndic = getResources().getDrawable(R.drawable.location_bad);
        if (location != null) {
            locIndic = getResources().getDrawable(R.drawable.location_better);

            if (location.getAccuracy() <= GOOD_PRESNOST) {
                locIndic = getResources().getDrawable(R.drawable.location_good);
            }
        }
        locationImageView.setBackground(locIndic);
    }

    private void zobrazPresnostPolohy(Location location) {
        if (location != null && location.hasAccuracy()) {
            setAccuracyTextColor(barvaBlack);
            accuracy.setText("(\u00B1 "  + location.getAccuracy() + " m)");
        } else {
            setAccuracyTextColor(barvaRed);
            accuracy.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (userAccountService != null && userAccountService.isUserLoggedIn()) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_color_24px));
        } else {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_24px));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_login:
                if (userAccountService != null && userAccountService.isUserLoggedIn()) {
                    openUserProfileActivity();
                } else {
                    openLoginActivity();
                }
                return true;
//            case R.id.action_settings:
//                aktivujNastaveni();
//                return true;
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_map:
                if (Utils.isOnline()) {
                    openMap();
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openNewCoffeeSiteActivity() {
        Intent activityIntent = new Intent(this, CreateCoffeeSiteActivity.class);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.putExtra("currentUserName", userAccountService.getLoggedInUser().getUserName());
        this.startActivity(activityIntent);
    }

    private void openLoginActivity() {
        if (Utils.isOnline()) {
            Intent activityIntent = new Intent(this, LoginActivity.class);
            //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(activityIntent);
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    private void openUserProfileActivity() {
        Intent activityIntent = new Intent(this, UserDataViewActivity.class);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.putExtra("currentUserProfile", userAccountService.getLoggedInUser());
        this.startActivity(activityIntent);
    }

    private void showAbout() {
        Intent i = new Intent(this, AboutActivity.class);
        this.startActivity(i);
    }

    private void setAccuracyTextColor(int barva) {
        accuracy.setTextColor(barva);
    }

    /**
     * To show statistics, can be called from AsyncTask
     *
     * @param stats
     */
    public void zobrazStatistiky(Statistics stats) {

        if (stats != null) {
            TextView sitesView = (TextView) findViewById(R.id.AllSitesTextView);
            TextView sites7View = (TextView) findViewById(R.id.AllSites7TextView);
            TextView sitesToday = (TextView) findViewById(R.id.TodaySitesTextView);
            TextView usersView = (TextView) findViewById(R.id.AllUsersTextView);

            sitesView.setText(stats.numOfSites);
            sitesToday.setText(stats.numOfSitesToday);
            sites7View.setText(stats.numOfSitesLastWeek);
            usersView.setText(stats.numOfUsers);
        }
    }

    private void openMap() {
        if (location != null) {
            Intent mapIntent = new Intent(this, MapsActivity.class);
            mapIntent.putExtra("currentLong", location.getLongitude());
            mapIntent.putExtra("currentLat", location.getLatitude());
            startActivity(mapIntent);
        }
    }

    // Currently not needed as only one Button i used to search for any type of coffee
    public void onHledejEspressoClick(View view) {

        if (location == null) {
            return;
        }
        if (Utils.isOnline()) {
            new GetSitesInRangeAsyncTask(this,
                                         location.getLatitude(),
                                         location.getLongitude(),
                                         searchRange,
                                "espresso").execute();
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    public void onHledejKafeClick(View view) {

        if (location == null) {
            return;
        }
        if (Utils.isOnline()) {
            new GetSitesInRangeAsyncTask(this,
                                        location.getLatitude(),
                                        location.getLongitude(),
                                        searchRange,
                                "").execute();
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    /**
     * Show info Toast message, that no CoffeeSite was found.
     * Currently not needed as a new Activity is shown for every search result
     * including nothing found.
     */
    public void showNothingFoundStatus(String subject) {

        int resource = ("espresso".equals(subject)) ? R.string.no_espresso_found : R.string.no_site_found;
        Toast toast = Toast.makeText(getApplicationContext(),
                                    resource,
                                    Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void requestPermission(String permissionType, int requestCode) {

        int permission = ContextCompat.checkSelfPermission(this, permissionType);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {permissionType}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Vyzaduje se pristup k poloze", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /**
     * Setup locationService listeners and RecyclerView which also
     * requires locationService to be activated/connected.
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();

        location = locationService.posledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
        locationService.addPropertyChangeListener(this);

        zobrazPresnostPolohy(location);
        updateAccuracyIndicator(location);
        if (location != null) {
//            searchEspressoButton.setEnabled(true);
            searchKafeButton.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Read stats
        // Load CoffeeSiteEntities
        if (Utils.isOnline()) {
            new ReadStatsAsyncTask(this).execute();
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (locationService != null) {
            location = locationService.posledniPozice(LAST_PRESNOST, MAX_STARI_DAT);

            zobrazPresnostPolohy(location);
            updateAccuracyIndicator(location);
            if (location != null) {
//                searchEspressoButton.setEnabled(true);
                searchKafeButton.setEnabled(true);
            } else {
//                searchEspressoButton.setEnabled(false);
                searchKafeButton.setEnabled(false);
            }
        }

        MenuItem userAccountMenuItem = mainToolbar.getMenu().size() > 0 ? mainToolbar.getMenu().getItem(0) : null;
        if (userAccountMenuItem != null) {
            if (userAccountService != null && userAccountService.isUserLoggedIn()) {
                userAccountMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_color_24px));
            } else {
                userAccountMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_24px));
            }
        }
    }

    @Override
    protected void onPause() {
//        searchEspressoButton.setEnabled(false);
        searchKafeButton.setEnabled(false);
        // Kontrola opravneni
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        super.onPause();
    }

    /**
     * Change of location detected, update all what should be updated, i.e.
     * accuracy indicator.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (bPrvni) { // prvni platna detekce polohy
            bPrvni = false;
            setAccuracyTextColor(barvaBlack);
        }

        if (locationService != null) {
            location = locationService.getCurrentLocation();
            zobrazPresnostPolohy(location);
            updateAccuracyIndicator(location);
//            searchEspressoButton.setEnabled(true);
            searchKafeButton.setEnabled(true);
        }
    }

    // ** UserLogin Service connection/disconnection ** //

    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userLoginServiceConnector;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    private void doBindUserLoginService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userLoginServiceConnector = new UserAccountServiceConnector(this);
        if (bindService(new Intent(this, UserAccountService.class),
                userLoginServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserLoginService() {
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            unbindService(userLoginServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindUserLoginService();
        super.onDestroy();
    }

    /**
     * We need AccountService to check if a user is logged in,
     * But it is not needed to listen for login event.
     */
    @Override
    public void onUserLoginServiceConnected() {
        userAccountService = userLoginServiceConnector.getUserLoginService();

        MenuItem userAccountMenuItem = mainToolbar.getMenu().size() > 0 ? mainToolbar.getMenu().getItem(0) : null;
        if (userAccountMenuItem != null) {
            if (userAccountService != null && userAccountService.isUserLoggedIn()) {
                userAccountMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_color_24px));
            } else {
                userAccountMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_24px));
            }
        }
    }

}
