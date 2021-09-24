package cz.fungisoft.coffeecompass2.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lukelorusso.verticalseekbar.VerticalSeekBar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Map;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.NotificationSubscriptionPreferencesHelper;
import cz.fungisoft.coffeecompass2.activity.data.SearchDistancePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.StatisticsPrefencesHelper;
import cz.fungisoft.coffeecompass2.activity.data.UserPreferencesHelper;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteEntitiesServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.UserDataViewActivity;
import cz.fungisoft.coffeecompass2.activity.ui.notification.NewsSubscriptionActivity;
import cz.fungisoft.coffeecompass2.activity.ui.notification.StaticCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.activity.ui.notification.TownNamesArrayAdapter;
import cz.fungisoft.coffeecompass2.asynctask.ReadStatsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.Statistics;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteEntitiesServiceConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.FirebaseMessageService;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.FunctionalUtils;
import cz.fungisoft.coffeecompass2.utils.NetworkStateReceiver;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Main activity to show:
 *
 *  - main search buttons to find CoffeeSites within selected distance range
 *  - info about location of the phone and accuracy of this location
 *  - basic statistics info about CoffeeSites and Users
 *  - show icon allowing to sign-in into application
 *  - show icon indicating, that logged-in user has created coffee sites and which leads to MyCoffeeSitesListActivity
 *  - show icon to load Notifications settings or to show list of new CoffeeSites received upon notification from Firebase
 *
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

    private static final long MAX_STARI_DAT = 1000 * 60; // pokud jsou posledni zname udaje o poloze starsi jako 1 minuta, zjistit nove (po spusteni app.)
    private static final float GOOD_PRESNOST = 10.0f;
    private static final float LAST_PRESNOST = 500.0f;

    private boolean firstLocationDetection = true;
    private final int barvaBlack = Color.BLACK;
    private final int barvaRed = Color.RED;

    private TextView accuracy;

    private ImageView locationImageView;

    private Location location;

    private Button searchKafeButton;

    private Toolbar mainToolbar;

    private VerticalSeekBar searchDistanceSeekBar;

    private LinearLayout  searchDistancesScaleLinearLayout;

    private static int searchRange = 500; // range in meters for searching from current position - 500 m default value
    private static  String searchRangeString;

    // Saves selected search distance range
    private SearchDistancePreferenceHelper searchRangePreferenceHelper;

    // Saves Statistics
    private StatisticsPrefencesHelper statisticsPrefencesHelper;

    // Saves number of Not canceled sites created by user
    private UserPreferencesHelper userPreferencesHelper;

    private ProgressBar mainActivityProgressBar;

    private LinearLayout statisticsLayout;

    private final int DAYS_BACK_FOR_LOAD_STATISTICS = 7;

    /**
     * To indicate if a user clicked on statistics View, to load latest
     * CoffeeSites after reading statistics.
     */
    private boolean statisticsCalledUponUsersClick = false;

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

    private FloatingActionButton fab;

    private TextView textNotificationBellIconItemCount;

    /**
     * Counts number of new CoffeeSitess notifications received
     */
    private int newSitesNotificationCount = 0;

    /**
     * Should be synchronized as called from more threads
     * @return
     */
    private synchronized int getNewSitesNotificationCount() {
        return newSitesNotificationCount;
    }

    private synchronized void setNewSitesNotificationCount(int newSitesNotificationCount) {
        this.newSitesNotificationCount = newSitesNotificationCount;
    }

    private synchronized void increaseNewSitesNotificationCount() {
        this.newSitesNotificationCount++;
    }


    private String newNotificationCoffeeSiteURL = "";

    private ArrayList<String> newNotificationCoffeeSiteURLs = new ArrayList<>();

    private MenuItem newSitesNotificationMenuItem;

    private CardView statisticsAndNewsCardView; // needed to change backround color, when statistics shows new CoffeeSites last week

    /**
     * Save info about latest processed new notified CoffeeSite URL
     */
    private NotificationSubscriptionPreferencesHelper notificationSubscriptionPreferencesHelper;

    /* ************* METHODS START ********************* */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statisticsPrefencesHelper = new StatisticsPrefencesHelper(this);

        setContentView(R.layout.activity_main);

        /*
         * If there are Extras, the Main activity was opened from notification tray
         * upon received firebase notification and user's click, i.e. when the
         * app. was in background
        */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // bundle should contain all info sent in "data" field of the notification
            // one of data is "coffeeSiteURL"
            String data = bundle.getString("coffeeSiteURL");
            if (data != null && !data.isEmpty()) {
                Intent intent = new Intent(this, CoffeeSiteDetailActivity.class);
                intent.putExtra("coffeeSiteUrl", data);
                startActivity(intent);
                // User now see the new CoffeeSites, trigger to show statistics view in original coloe
                statisticsPrefencesHelper.putNumOfSitesLastWeekChanged(false);
            }
        }

        notificationSubscriptionPreferencesHelper = new NotificationSubscriptionPreferencesHelper(this);

        /*
         * Observes and handles received push message notification, when app is in foreground.
         * onChanged is called every time, the activity is created (for example goes from background
         * to foreground) and it was already called fro some previous new coffeeSite URL
         * notification. So, we need to check, if onChange() is called because of
         * another ne URL or if it is already processed URL.
         */
        FirebaseMessageService
                .Notification
                .getInstance()
                .getMessageData()
                .observe(this, new Observer<Map<String, String>>() {
                    @Override
                    public void onChanged(Map<String, String> data) {
                        if (data != null && data.get("coffeeSiteURL") != null) {
                            newNotificationCoffeeSiteURL = data.get("coffeeSiteURL");
                            // is this a new URL to be processed?
                            if (!notificationSubscriptionPreferencesHelper.getLatestReceivedUrl().equals(newNotificationCoffeeSiteURL)) {
                                startReadStatistics();
                                notificationSubscriptionPreferencesHelper.putLatestReceivedUrl(newNotificationCoffeeSiteURL);
                                newNotificationCoffeeSiteURLs.add(newNotificationCoffeeSiteURL);
                                increaseNewSitesNotificationCount();
                                setupNotificationCountBadge();
                            }
                        }
                    }
                });

        mainActivityProgressBar = findViewById(R.id.progress_main_activity);

        statisticsAndNewsCardView = findViewById(R.id.statistics_news_main_card_view);

        if (statisticsPrefencesHelper.getNumOfSitesLastWeekChanged()) {
            statisticsAndNewsCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        userPreferencesHelper = new UserPreferencesHelper(this);

        // Get current searchDistance from Preferences
        searchRangePreferenceHelper = new SearchDistancePreferenceHelper(this);
        searchRange = searchRangePreferenceHelper.getSearchDistanc();

        searchKafeButton = (Button) findViewById(R.id.searchKafeButton);

        searchDistanceSeekBar = (VerticalSeekBar) findViewById(R.id.searchDistanceSeekBar);

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

        // Location info
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
        fab = findViewById(R.id.add_site_floatingActionButton);

        // effective final this activity instance for anonymous onClick() handler
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userAccountService != null && userAccountService.isUserLoggedIn()) {
                    if (coffeeSiteEntitiesService != null && coffeeSiteEntitiesService.isDataReadFromServer()) {
                        openCreateNewCoffeeSiteActivity();
                    } else {
                        Snackbar mySnackbar = Snackbar.make(view, R.string.data_for_offline_coffee_site_create_not_available, Snackbar.LENGTH_LONG);
                        mySnackbar.show();
                    }
                } else {
                    Snackbar mySnackbar = Snackbar.make(view, R.string.new_site_only_for_registered_user, Snackbar.LENGTH_LONG);
                    mySnackbar.show();
                }
            }
        });

        fab.setVisibility(!Utils.isOfflineModeOn(getApplicationContext()) ? VISIBLE : GONE);

        // Lets bind CoffeeSiteEntitiesService and load all CoffeeSiteEntities
        // in onCoffeeSiteEntitiesServiceConnected() method
        doBindCoffeeSiteEntitiesService();

        getFirebaseToken();

        //showCurrentFirebaseToken();
        statisticsLayout = findViewById(R.id.statistics_layout);

        statisticsLayout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    onStatisticsClick(v);
                                                }
                                            });

        fab.setVisibility(VISIBLE);
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
        // (and thus won't be supporting component replacement by other applications).
        coffeeSiteEntitiesServiceConnector = new CoffeeSiteEntitiesServiceConnector();
        coffeeSiteEntitiesServiceConnector.addCoffeeSiteEntitiesServiceConnectionListener(this);
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
        coffeeSiteEntitiesService = coffeeSiteEntitiesServiceConnector.getCoffeeSiteEntitiesService();
        if (coffeeSiteEntitiesService != null) {
            coffeeSiteEntitiesService.addCoffeeSiteEntitiesOperationsListener(this);
            // read this data only once as they do not change usually
            if (Utils.isOnline(getApplicationContext())) {
                if (!coffeeSiteEntitiesService.isDataReadFromServer()) {
                    coffeeSiteEntitiesService.populateCSEntities();
                }
            } else {
                Utils.showNoInternetToast(getApplicationContext());
            }
        }
    }

    private void doUnbindCoffeeSiteEntitiesService() {
        if (mShouldUnbindCoffeeSiteEntitiesService) {
            if (coffeeSiteEntitiesService != null) {
                coffeeSiteEntitiesService.removeCoffeeSiteEntitiesOperationsListener(this);
                coffeeSiteEntitiesService.resetDataReadFromServer();
            }
            // Release information about the service's state.
            coffeeSiteEntitiesServiceConnector.removeCoffeeSiteEntitiesServiceConnectionListener(this);
            unbindService( coffeeSiteEntitiesServiceConnector);
            mShouldUnbindCoffeeSiteEntitiesService = false;
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
        // (and thus won't be supporting component replacement by other  applications).
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
            if (Utils.isOnline(getApplicationContext())) {
                startNumberOfCoffeeSitesFromUserCall();
            }
        }
    }

    private void doUnbindCoffeeSiteLoadOperationsService() {
        if (mShouldUnbindCoffeeSiteLoadOperationsService) {
            if (coffeeSiteLoadOperationsService != null) {
                coffeeSiteLoadOperationsService.removeLoadOperationsListener(this);
            }
            // Release information about the service's state.
            coffeeSiteLoadOperationsServiceConnector.removeCoffeeSiteServiceConnectionListener(this);
            unbindService(coffeeSiteLoadOperationsServiceConnector);
            mShouldUnbindCoffeeSiteLoadOperationsService = false;
        }
    }


    public void startNumberOfCoffeeSitesFromUserCall() {
        if (Utils.isOnline(getApplicationContext())) {
            if (coffeeSiteLoadOperationsService != null && !numberOfCoffeeSitesCreatedByLoggedInUserChecked) {
                //showProgressbar(); // we don't want to show progressbar on MainActivity for short time REST requests
                coffeeSiteLoadOperationsService.findNumberOfCoffeeSitesFromCurrentUser();
            }
        }
    }


    @Override
    public void onNumberOfCoffeeSiteFromLoggedInUserLoaded(int coffeeSitesNumber, String error) {
        hideProgressbar();
        if (error.isEmpty()) {
            numberOfCoffeeSitesCreatedByLoggedInUser = coffeeSitesNumber;
            numberOfCoffeeSitesCreatedByLoggedInUserChecked = true;
            userPreferencesHelper.putNumOfNotCanceledSites(numberOfCoffeeSitesCreatedByLoggedInUser);

            updateMyCoffeeSitesMenuItem();
        }
    }

    /**
     * Shows/hides MenuItem myCoffeeSitesMenuItem according data saved in Preferences helpers
     * to allow/disallow user to open MyCoffeeSitesListActivity with CoffeeSites created by the user.
     */
    private void updateMyCoffeeSitesMenuItem() {
        DataForOfflineModePreferenceHelper offlineDataPreferenceHelper = new DataForOfflineModePreferenceHelper(getApplicationContext());
        MenuItem myCoffeeSitesMenuItem = mainToolbar.getMenu().size() > 1 ? mainToolbar.getMenu().findItem(R.id.action_my_coffeesites) : null;

        if (myCoffeeSitesMenuItem != null) {
            if (offlineDataPreferenceHelper.getDataSavedOfflineAvailable() || userPreferencesHelper.getNumOfNotCanceledSites() > 0) {
                myCoffeeSitesMenuItem.setVisible(true);
            } else {
                myCoffeeSitesMenuItem.setVisible(false);
            }
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
        Drawable  locIndic = getDrawable(R.drawable.location_bad);
        if (location != null) {
            locIndic = getDrawable(R.drawable.location_better);

            if (location.getAccuracy() <= GOOD_PRESNOST) {
                locIndic = getDrawable(R.drawable.location_good);
            }
        }
        locationImageView.setBackground(locIndic);
    }

    private void showLocationAccuracy(Location location) {
        if (location != null && location.hasAccuracy()) {
            setAccuracyTextColor(barvaBlack);
            accuracy.setText("(\u00B1 "  + Math.round(location.getAccuracy()) + " m)");
        } else {
            setAccuracyTextColor(barvaRed);
            accuracy.setText("");
        }
    }

    @SuppressLint("RestrictedApi") // due to searchAutoComplete.setThreshold(2);
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Default do not show menu icon for going to MyCoffeeSitesListActivity
        menu.findItem(R.id.action_my_coffeesites).setVisible(false);

        // Default icon for this action
        menu.findItem(R.id.action_login).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_24px));

        // Setup menu icon with badge showing number of new CoffeeSites notification
        newSitesNotificationMenuItem = menu.findItem(R.id.new_sites_notification);

        View actionView = newSitesNotificationMenuItem.getActionView();
        textNotificationBellIconItemCount = (TextView) actionView.findViewById(R.id.notification_bell_badge);

        setupNotificationCountBadge();

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(newSitesNotificationMenuItem);
            }
        });

        // Get the SearchView (for searching sites in town) and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search_main).getActionView();

        // StaticCoffeeSitesListActivity is the activity to show result of searchView input
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, StaticCoffeeSitesListActivity.class)));
        searchView.setQueryHint(getString(R.string.search_by_city_hint));
        searchView.setIconifiedByDefault(true); // iconify the widget; and expand after user's click

        TownNamesArrayAdapter townNamesArrayAdapter = new TownNamesArrayAdapter(getApplicationContext(), R.layout.suggestion);
        townNamesArrayAdapter.setNotifyOnChange(true);

        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setThreshold(2);
        searchAutoComplete.setAdapter(townNamesArrayAdapter);

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // City name is selected from list
                String townName =  parent.getItemAtPosition(position).toString();
                if (townName.length() > 1) {
                    searchView.setQuery(townName, true);
                    // handleSearchInTownIntent() follows in StaticCoffeeSiteActivity
                }
                Log.i(TAG, "Selected town: " + townName);
            }
        });

        return true;
    }

    private void setupNotificationCountBadge() {
        if (textNotificationBellIconItemCount != null) {
            if (getNewSitesNotificationCount() == 0) {
                if (textNotificationBellIconItemCount.getVisibility() != View.GONE) {
                    textNotificationBellIconItemCount.setVisibility(View.GONE);
                }
            } else {
                textNotificationBellIconItemCount.setText(String.valueOf(Math.min(getNewSitesNotificationCount(), 99)));
                if (textNotificationBellIconItemCount.getVisibility() != View.VISIBLE) {
                    textNotificationBellIconItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.new_sites_notification: {
                if (newNotificationCoffeeSiteURLs.size() > 1) {
                    Intent intent = new Intent(this, StaticCoffeeSitesListActivity.class);
                    intent.putStringArrayListExtra("newCoffeeSitesURLs", newNotificationCoffeeSiteURLs);
                    startActivity(intent);

                    newNotificationCoffeeSiteURL = "";
                    newNotificationCoffeeSiteURLs.clear();
                    setNewSitesNotificationCount(0);
                    setupNotificationCountBadge();
                    return true;
                }
                if (!notificationSubscriptionPreferencesHelper.getLatestReceivedUrl().isEmpty()
                       && getNewSitesNotificationCount() == 1) {
                    Intent intent = new Intent(this, CoffeeSiteDetailActivity.class);
                    intent.putExtra("coffeeSiteUrl", newNotificationCoffeeSiteURL);
                    startActivity(intent);

                    newNotificationCoffeeSiteURL = "";
                    newNotificationCoffeeSiteURLs.clear();
                    setNewSitesNotificationCount(0);
                    setupNotificationCountBadge();
                } else { // notification subscription setup
                    Intent i = new Intent(MainActivity.this, NewsSubscriptionActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                return true;
            }

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
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_offline_mode:
                openOfflineSettingsActivity();
                return true;
            case R.id.action_map:
                if (Utils.isOnline(getApplicationContext())) {
                    openMap();
                } else {
                    Utils.showMapNotAvailableIfNoInternetToast(getApplicationContext());
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
        activityIntent.putExtra("showListNotModifiedCoffeeSites", true);

        this.startActivity(activityIntent);
    }

    private void openCreateNewCoffeeSiteActivity() {
        Intent activityIntent = new Intent(this, CreateCoffeeSiteActivity.class);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(activityIntent);
    }

    private void openLoginActivity() {
        if (Utils.isOnline(getApplicationContext())) {
            Intent activityIntent = new Intent(this, LoginActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(activityIntent);
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    private void openUserProfileActivity() {
        Intent activityIntent = new Intent(this, UserDataViewActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.putExtra("currentUserProfile", userAccountService.getLoggedInUser());
        this.startActivity(activityIntent);
    }

    private void openOfflineSettingsActivity() {
        Intent activityIntent = new Intent(this, OfflineModeSelectionActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // should run only one instance of the Activity
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
     * if internet is available.<br>
     * Can also be read later, after internet becomes available, see {@link NetworkStateReceiver}
     */
    public synchronized void startReadStatistics() {
        if (Utils.isOnline(getApplicationContext())) {
            if (statisticsCalledUponUsersClick) {
                showProgressbar();
            }
            new ReadStatsAsyncTask(this).execute();
        }
    }

    /**
     * To show statistics, can be called from AsyncTask
     *
     * @param stats
     */
    public void showAndSaveStatistics(Statistics stats) {
        hideProgressbar();
        if (Integer.parseInt(statisticsPrefencesHelper.getNumOfSitesLastWeek()) < Integer.parseInt(stats.numOfSitesLastWeek)) {
            statisticsPrefencesHelper.putNumOfSitesLastWeekChanged(true);
        }
        if (Integer.parseInt(stats.numOfSitesLastWeek) == 0) {
            statisticsPrefencesHelper.putNumOfSitesLastWeekChanged(false);
        }
        statisticsPrefencesHelper.saveStatistics(stats);

        TextView sitesView = findViewById(R.id.all_sites_TextView);
        TextView sites7View = findViewById(R.id.AllSites7TextView);
        TextView sitesToday = findViewById(R.id.TodaySitesTextView);
        TextView usersView = findViewById(R.id.AllUsersTextView);

        sitesView.setText(stats.numOfSites);
        sitesToday.setText(stats.numOfSitesToday);
        sites7View.setText(stats.numOfSitesLastWeek);
        if (statisticsPrefencesHelper.getNumOfSitesLastWeekChanged()) {
            statisticsAndNewsCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        usersView.setText(stats.numOfUsers);

        // Where the statistics shown upon user's click on Statistics CardView ?
        if (statisticsCalledUponUsersClick && Integer.parseInt(stats.numOfSitesLastWeek) > 0 ) {
            statisticsCalledUponUsersClick = false;
            if (Utils.isOnline(getApplicationContext())) {
                Intent intent = new Intent(this, StaticCoffeeSitesListActivity.class);
                intent.putExtra("daysBack", DAYS_BACK_FOR_LOAD_STATISTICS);
                startActivity(intent);
                // user now checked the new CoffeeSites of the week, so background of the Statistics CardView can
                // changed back to it's original color
                statisticsAndNewsCardView.setCardBackgroundColor(getResources().getColor(R.color.white_transparent));
                statisticsPrefencesHelper.putNumOfSitesLastWeekChanged(false);
            } else {
                Snackbar mySnackbar = Snackbar.make(statisticsLayout, R.string.toast_no_internet_no_offline_data, Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
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


    public void onSearchCoffeeClick(View view) {
        if (location == null) {
            return;
        }
        if (Utils.isOnline(getApplicationContext()) || Utils.offlineDataAvailable(getApplicationContext())) {
            Intent csListIntent = new Intent(this, FoundCoffeeSitesListActivity.class);
            csListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            csListIntent.putExtra("latLongFrom", new LatLng(location.getLatitude(), location.getLongitude()));
            csListIntent.putExtra("searchRange", searchRange);
            csListIntent.putExtra("coffeeSort", "");

            startActivity(csListIntent);
        } else {
            Snackbar mySnackbar = Snackbar.make(view, R.string.toast_no_internet_no_offline_data, Snackbar.LENGTH_LONG);
            mySnackbar.show();
        }
    }

    /**
     * After click on Statistics View, load the latest statistics and if there are some new
     * CoffeeSites, load and show them.
     *
     * @param view
     */
    public void onStatisticsClick(View view) {
        statisticsCalledUponUsersClick = true;
        statisticsAndNewsCardView.setCardBackgroundColor(getResources().getColor(R.color.white_transparent));
        startReadStatistics();
    }


    protected void requestPermission(String permissionType, int requestCode) {
        int permission = ContextCompat.checkSelfPermission(this, permissionType);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {permissionType}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Je vyžadován přístup k poloze.", Toast.LENGTH_LONG).show();
                }
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

        if (locationService != null) {
            location = locationService.getPosledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
            locationService.addPropertyChangeListener(this);
        }

        showLocationAccuracy(location);
        updateAccuracyIndicator(location);
        if (location != null) {
            searchKafeButton.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Read statistics
        startReadStatistics();
        // Read number of CoffeeSites created by current user to show/hide menu icon to open list of MyCoffeeSitesActivity
        startNumberOfCoffeeSitesFromUserCall();

        if (!Utils.isOnline(getApplicationContext()) && Utils.offlineDataAvailable(getApplicationContext())) {
            showAndSaveStatistics(statisticsPrefencesHelper.getSavedStatistics());
        }

        updateMyCoffeeSitesMenuItem();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (locationService != null) {
            locationService.addPropertyChangeListener(this);
            location = locationService.getPosledniPozice(LAST_PRESNOST, MAX_STARI_DAT);

            showLocationAccuracy(location);
            updateAccuracyIndicator(location);

            searchKafeButton.setEnabled(location != null);
        }

        doBindCoffeeSiteLoadOperationsService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        searchKafeButton.setEnabled(false);
        // Kontrola opravneni
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        if (locationService != null) {
            locationService.removeAllLocationChangeListeners();
        }
        doUnbindCoffeeSiteLoadOperationsService();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // UserAccountService service connection first. CoffeeSiteLoadOperationsService next in the onResume()
        doBindUserAccountService();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeStateReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        numberOfCoffeeSitesCreatedByLoggedInUserChecked = false;
        unregisterReceiver(networkChangeStateReceiver);
        doUnbindUserAccountService();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindCoffeeSiteEntitiesService();
    }


    /**
     * Change of location detected, update all what should be updated, i.e.
     * accuracy indicator.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (firstLocationDetection) { // prvni platna detekce polohy
            firstLocationDetection = false;
            setAccuracyTextColor(barvaBlack);
        }

        if (locationService != null) {
            location = locationService.getCurrentLocation();
            showLocationAccuracy(location);
            updateAccuracyIndicator(location);
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
        // (and thus won't be supporting component replacement by other applications).
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
        evaluateLoginMenuIcon();
        if (userAccountService.isUserLoggedIn()) {
            updateMyCoffeeSitesMenuItem();
        }
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
     * Helper method. Not used currently, but ready for the future, if thre would be a long running job
     */
    public void showProgressbar() {
        mainActivityProgressBar.setVisibility(VISIBLE);
    }

    /**
     * Helper method ...
     */
    public void hideProgressbar() {
        mainActivityProgressBar.setVisibility(GONE);
    }

    public void enableFab(boolean isEnabled) {
        fab.setVisibility(isEnabled ? VISIBLE : GONE);
    }

    private void doUnbindUserAccountService() {

        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            userAccountServiceConnector.removeUserAccountServiceConnectionListener(this);
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }


    /**
     * Zatim pomocna metoda k ziskani Firebase tokenu pro aktualni zarizeni - inicializace Firebase
     */
    private void getFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed: ", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.firebase_token_msg) + token;
                        Log.d(TAG, msg);
                    }
                });
    }

    /**
     * Pomocna metoda k vypsani aktualniho Firebase token, ulozeneho v NotificationSubscriptionPreferencesHelper,
     * do logu
     */
//    private void showCurrentFirebaseToken() {
//        NotificationSubscriptionPreferencesHelper preferencesHelper = new NotificationSubscriptionPreferencesHelper(this);
//        Log.i(TAG, "Current firebase token: " + preferencesHelper.getFirebaseToken());
//    }

}
