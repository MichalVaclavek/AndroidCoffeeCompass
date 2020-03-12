package cz.fungisoft.coffeecompass2.activity;

import android.Manifest;
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

import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.lukelorusso.verticalseekbar.VerticalSeekBar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteEntitiesServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesServiceConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.utils.FunctionalUtils;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.utils.NetworkStateReceiver;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.SearchDistancePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.UserDataViewActivity;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.ReadStatsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.Statistics;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;

/**
 * Main activity to show:
 *
 *  - search buttons to find either ESPRESSO CoffeeSites only or any CoffeeSite within specified distance range
 *  - info about location of the phone and accuracy of this location
 *  - basic statistics info about CoffeeSites and Users
 *  - show icon allowing to sign-in into application
 *  - show icon indicating, that logged-in user has created coffee sites and which leads to
 *  {@link MyCoffeeSitesListActivity}
 *
 *  Is capable to detect it's current location to allow searching of CoffeeSites based on current location.
 */
public class MainActivity extends ActivityWithLocationService
                          implements PropertyChangeListener,
                                     UserAccountServiceConnectionListener,
                                     CoffeeSiteEntitiesServiceConnectionListener,
                                     CoffeeSiteEntitiesServiceOperationsListener,
                                     CoffeeSiteServicesConnectionListener,
                                     CoffeeSiteLoadServiceOperationsListener {

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
    private Button searchKafeButton;

    private Toolbar mainToolbar;

    private VerticalSeekBar searchDistanceSeekBar;

    private LinearLayout  searchDistancesScaleLinearLayout;

    private static int searchRange = 500; // range in meters for searching from current position - 500 m default value
    private static  String searchRangeString;

    // Saves selected search distance range
    private SearchDistancePreferenceHelper searchRangePreferenceHelper;


    private ProgressBar mainActivityProgressBar;

    /**
     * Needed to decide if the menu icon for starting MyCoffeeSitesListActivity
     * should be visible or not
     */
    private int numberOfCoffeeSitesCreatedByLoggedInUser = 0;

    /**
     * To detect, if it is needed to load number of CoffeeSites from user after
     * the internet connection is re-established
     */
    private boolean numberOfCoffeeSitesCreatedByLoggedInUserChecked = false;

    public boolean isNumberOfCoffeeSitesCreatedByLoggedInUserChecked() {
        return numberOfCoffeeSitesCreatedByLoggedInUserChecked;
    }

    /**
     * Detector of internet connection change
     */
    private final NetworkStateReceiver networkChangeStateReceiver = new NetworkStateReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mainActivityProgressBar = findViewById(R.id.progress_main_activity);

        // there is an attempt to connect to LocationService in super Activity
        // lets show progress bar as it can take some time, when the
        // MainActivity runs for the first time
        // Progress bar is hidden in onLocationServiceConnected()
        showProgressbar();

        // Get cuurent serachDistance from Preferences
        searchRangePreferenceHelper = new SearchDistancePreferenceHelper(this);
        searchRange = searchRangePreferenceHelper.getSearchDistanc();

        searchKafeButton = (Button) findViewById(R.id.searchKafeButton);

        searchDistanceSeekBar = (VerticalSeekBar) findViewById(R.id.searchDistanceSeekBar);

        //searchDistanceSeekBar.setThumbPlaceholderDrawable(getDrawable(R.drawable.cup_basic));
        String[] vzdalenosti = getResources().getStringArray(R.array.vzdalenosti);
        // Seek bar for selecting searching distance
        searchDistanceSeekBar.setMaxValue(vzdalenosti.length - 1);

        searchDistancesScaleLinearLayout = findViewById(R.id.searchDistancesScaleLinearLayout);
        // Text view for searchDistances to be accessible for changing its property when selected by seekbar
        TextView[] searchDistanceTextViews = new TextView[vzdalenosti.length];

        for (int i = vzdalenosti.length - 1; i >= 0 ; i--) {
            TextView searchDistTextView = new TextView(this);
            searchDistTextView.setText(vzdalenosti[i]);
            searchDistTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            searchDistTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            searchDistanceTextViews[i] = searchDistTextView;
            searchDistancesScaleLinearLayout.addView(searchDistTextView);
        }
        // find selected searchRangeTextView to be highlited
        for (int i = vzdalenosti.length - 1; i >= 0 ; i--) {
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

        //Location info
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE);

        accuracy = (TextView) findViewById(R.id.accuracy);

        searchRangeString = Utils.convertSearchDistance(searchRange);

        //TODO - text from R.string. ...
        searchKafeButton.setTransformationMethod(null);
        searchKafeButton.setText(Html.fromHtml("KÁVA<br><small>" + searchRangeString + "</small>" ));

        locationImageView = (ImageView) findViewById(R.id.locationImageView);

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
    }


    // ** CoffeeSiteEntitiesService connection/disconnection ** //

    protected CoffeeSiteEntitiesService coffeeSiteEntitiesService;
    private CoffeeSiteEntitiesServiceConnector coffeeSiteEntitiesServiceConnector;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteEntitiesService;

    private void doBindCoffeeSiteEntitiesService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        coffeeSiteEntitiesServiceConnector = new CoffeeSiteEntitiesServiceConnector();
        coffeeSiteEntitiesServiceConnector.addCoffeeSiteImageServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteEntitiesService.class),
                coffeeSiteEntitiesServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteEntitiesService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteEntitiesService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    @Override
    public void onCoffeeSiteEntitiesServiceConnected() {
        coffeeSiteEntitiesService = coffeeSiteEntitiesServiceConnector.getCoffeeSiteImageService();
        if (coffeeSiteEntitiesService != null) {
            coffeeSiteEntitiesService.addCoffeeSiteEntitiesOperationsListener(this);
            startLoadingCoffeeSiteEntities();
        }
    }

    private void doUnbindCoffeeSiteEntitiesService() {
        if (mShouldUnbindCoffeeSiteEntitiesService) {
            if (coffeeSiteEntitiesService != null) {
                coffeeSiteEntitiesService.removeCoffeeSiteEntitiesOperationsListener(this);
            }
            // Release information about the service's state.
            coffeeSiteEntitiesServiceConnector.removeCoffeeSiteImageServiceConnectionListener(this);
            unbindService( coffeeSiteEntitiesServiceConnector);
            mShouldUnbindCoffeeSiteEntitiesService = false;
        }
    }

    public void startLoadingCoffeeSiteEntities() {
        if (coffeeSiteEntitiesService != null) {
            coffeeSiteEntitiesService.readAndSaveAllEntitiesFromServer();
        }
    }


    // ****** CoffeeSiteLoadOperationsService connection/disconnection ****** //

    protected CoffeeSiteLoadOperationsService coffeeSiteLoadOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteLoadOperationsService> coffeeSiteLoadOperationsServiceConnector;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteLoadOperationsService;

    private void doBindCoffeeSiteLoadOperationsService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        coffeeSiteLoadOperationsServiceConnector = new CoffeeSiteServicesConnector<>();
        coffeeSiteLoadOperationsServiceConnector.addCoffeeSiteServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteLoadOperationsService.class),
                coffeeSiteLoadOperationsServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteLoadOperationsService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteLoadOperationsService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    @Override
    public void onCoffeeSiteServiceConnected() {
        if (coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService() != null) {
            coffeeSiteLoadOperationsService = coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService();
            coffeeSiteLoadOperationsService.addLoadOperationsListener(this);
            startNumberOfCoffeeSitesFromUserService();
        }
    }

    private void doUnbindCoffeeSiteLoadOperationsService() {
        if (mShouldUnbindCoffeeSiteLoadOperationsService) {
            coffeeSiteLoadOperationsService.removeLoadOperationsListener(this);
            // Release information about the service's state.
            coffeeSiteLoadOperationsServiceConnector.removeCoffeeSiteServiceConnectionListener(this);
            unbindService(coffeeSiteLoadOperationsServiceConnector);
            mShouldUnbindCoffeeSiteLoadOperationsService = false;
        }
    }

    @Override
    public void onNumberOfCoffeeSiteFromLoggedInUserLoaded(int coffeeSitesNumber, String error) {
        hideProgressbar();

        if (error.isEmpty()) {
            numberOfCoffeeSitesCreatedByLoggedInUser = coffeeSitesNumber;
            numberOfCoffeeSitesCreatedByLoggedInUserChecked = true;
            MenuItem myCoffeeSitesMenuItem = mainToolbar.getMenu().size() > 1 ? mainToolbar.getMenu().findItem(R.id.action_my_coffeesites) : null;
            if (myCoffeeSitesMenuItem != null) {
                if (numberOfCoffeeSitesCreatedByLoggedInUser > 0) {
                    myCoffeeSitesMenuItem.setVisible(true);
                }
            }
        }
    }

    public void startNumberOfCoffeeSitesFromUserService() {
        if (coffeeSiteLoadOperationsService != null) {
            coffeeSiteLoadOperationsService.findNumberOfCoffeeSitesFromCurrentUser();
        }
    }

    /**************************************************************************/

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
            accuracy.setText("(\u00B1 "  + Math.round(location.getAccuracy()) + " m)");
        } else {
            setAccuracyTextColor(barvaRed);
            accuracy.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Default do not show menu icon for going to MyCoffeeSitesListActivity
        menu.findItem(R.id.action_my_coffeesites).setVisible(false);

        // Default icon for this action
        menu.findItem(R.id.action_login).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_24px));

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
            case R.id.action_my_coffeesites:
                if (userAccountService != null && userAccountService.isUserLoggedIn()) {
                    goToMyCoffeeSitesActivity();
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

    /**
     * Starts MyCoffeeSitesListActivity
     */
    private void goToMyCoffeeSitesActivity() {
        Intent activityIntent = new Intent(this, MyCoffeeSitesListActivity.class);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.putExtra("myCoffeeSitesNumber", numberOfCoffeeSitesCreatedByLoggedInUser);
        this.startActivity(activityIntent);
    }

    private void openNewCoffeeSiteActivity() {
        Intent activityIntent = new Intent(this, CreateCoffeeSiteActivity.class);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //activityIntent.putExtra("currentUserName", userAccountService.getLoggedInUser().getUserName());
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
     * Starts AsyncTask to read app. statistics to be shown in MainActivity
     */
    public void startReadStatistics() {
        new ReadStatsAsyncTask(this).execute();
    }

    /**
     * To show statistics, can be called from AsyncTask
     *
     * @param stats
     */
    public void zobrazStatistiky(Statistics stats) {

        if (stats != null) {

            TextView sitesView = (TextView) findViewById(R.id.all_sites_TextView);
            TextView sites7View = (TextView) findViewById(R.id.AllSites7TextView);
            TextView sitesToday = (TextView) findViewById(R.id.TodaySitesTextView);
            TextView usersView = (TextView) findViewById(R.id.AllUsersTextView);

            sitesView.setText(stats.numOfSites);
            sitesToday.setText(stats.numOfSitesToday);
            sites7View.setText(stats.numOfSitesLastWeek);
            usersView.setText(stats.numOfUsers);
        }
    }

    /**
     * Start MapsActivity
     */
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
                    Toast.makeText(this, "Vyžaduje se přístup k poloze.", Toast.LENGTH_LONG).show();
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
        hideProgressbar();

        location = locationService.posledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
        locationService.addPropertyChangeListener(this);

        zobrazPresnostPolohy(location);
        updateAccuracyIndicator(location);
        if (location != null) {
            searchKafeButton.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeStateReceiver, filter);

        // Read stats if internet is available
        // Can be read later after internet becomes available, see NetworkStateReceiver
        if (Utils.isOnline()) {
            startReadStatistics();
            //startNumberOfCoffeeSitesFromUserService();
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

        // Suppose we do not know actual status of loading CoffeeSites number from user
        numberOfCoffeeSitesCreatedByLoggedInUserChecked = false;
        doBindCoffeeSiteLoadOperationsService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Lets bind CoffeeSiteEntitiesService and load all CoffeeSiteEntities
        // in onCoffeeSiteEntitiesConnected() method
        // TODO - Verify, if calling this in onResume() would be more convenient
        doBindCoffeeSiteEntitiesService();

        // UserAccountService service connection first. CoffeeSiteLoadOperationsService next in the onResume()
        doBindUserAccountService();
    }

    @Override
    protected void onStop() {
        doUnbindUserAccountService();
        doUnbindCoffeeSiteEntitiesService();
        super.onStop();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(networkChangeStateReceiver);

        searchKafeButton.setEnabled(false);
        // Kontrola opravneni
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        doUnbindCoffeeSiteLoadOperationsService();

        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    private UserAccountServiceConnector userAccountServiceConnector;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    private void doBindUserAccountService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userAccountServiceConnector = new UserAccountServiceConnector();
        userAccountServiceConnector.addUserAccountServiceConnectionListener(this);
        if (bindService(new Intent(this, UserAccountService.class),
                userAccountServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    @Override
    public void onUserAccountServiceConnected() {
        userAccountService = userAccountServiceConnector.getUserLoginService();
        //startNumberOfCoffeeSitesFromUserService();
        evaluateLoginMenuIcon();
        evaluateMyCoffeeSitesIcon();
    }

    /**
     * Evaluates what login icon is shown according userAccountService
     * If user is logged in, show green icon, othervise black.
     */
    private void evaluateLoginMenuIcon() {
        MenuItem userAccountMenuItem = mainToolbar.getMenu().size() > 0 ? mainToolbar.getMenu().findItem(R.id.action_login) : null;
        if (userAccountMenuItem != null) {
            if (userAccountService != null && userAccountService.isUserLoggedIn()) {
                userAccountMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_color_24px));
            } else {
                userAccountMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_24px));
            }
        }
    }

    /**
     * Evaluates if the MyCoffeeSitesIcon should be displayd or not.
     * This icon is shown only if logged in user has more then 0
     * CoffeeSites created.
     * So, the Service call to find number of created sites is started,
     * and the result is evaluated in callback method of this service call
     */
    private void evaluateMyCoffeeSitesIcon() {
        // Not show icon as a default
        MenuItem myCoffeeSitesMenuItem = mainToolbar.getMenu().size() > 1 ? mainToolbar.getMenu().findItem(R.id.action_my_coffeesites) : null;
        if (myCoffeeSitesMenuItem != null) {
            myCoffeeSitesMenuItem.setVisible(false);
        }
    }

    /**
     * Helper method ...
     */
    public void showProgressbar() {
        mainActivityProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method ...
     */
    public void hideProgressbar() {
        mainActivityProgressBar.setVisibility(View.GONE);
    }

    private void doUnbindUserAccountService() {

        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            userAccountServiceConnector.removeUserAccountServiceConnectionListener(this);
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

}
