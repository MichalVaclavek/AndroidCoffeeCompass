package cz.fungisoft.coffeecompass2.activity.ui.notification;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.MapsActivity;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static android.view.View.GONE;


/**
 * Activity to show list of new CoffeeSites received upon subscribed notification.<br>
 * Or after user's click on Statistics View on MainActivity, which leads to load
 * CoffeeSites created in last week/7 days.<br>
 * Or after user searches CoffeeSites in town.<br>
 * Allows to select the CoffeeSite from the list and open detail activity for that CoffeeSite.
 */
public class StaticCoffeeSitesListActivity extends AppCompatActivity
                                           implements CoffeeSiteServicesConnectionListener,
                                                      CoffeeSiteLoadServiceOperationsListener  {

    private static final String TAG = "StaticSitesListAct";

    /**
     * The main attribute of activity containing all new CoffeeSites to show
     * within this Activities
     */
    private List<CoffeeSite> content = new ArrayList<>();

    /**
     * List of CoffeeSites to be shown in MapsActivity
     */
    private final CoffeeSiteListContent mapContent = new CoffeeSiteListContent();

    /**
     * URLs of the new CoffeeSites
     */
    private List<String> newCoffeeSitesURLs;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private StaticCoffeeSitesListRecyclerViewAdapter recyclerViewAdapter;

    protected CoffeeSiteLoadOperationsService coffeeSiteLoadOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteLoadOperationsService> coffeeSiteLoadOperationsServiceConnector;

    private ProgressBar loadCoffeeSiteProgressBar;

    /**
     * Number of days back from now to load latest ACTIVated CoffeeSites
     * Used, when this activity is called upon user's click on Statistics View
     */
    private int numOfDaysToLoadLatestSites = 0; // 0 means not valid

    /**
     * Name of town to search CoffeeSites in
     */
    private String townName = "";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device. Not used, now.
     */
    private boolean mTwoPane;

    private Toolbar toolbar;

    public StaticCoffeeSitesListActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notification_activity_new_coffee_sites_list);

        loadCoffeeSiteProgressBar = findViewById(R.id.progress_notification_new_coffeesites_load);

        if (savedInstanceState != null && content != null) { // i.e. after orientation was changed
            Collections.sort(content);
        }

        toolbar = (Toolbar) findViewById(R.id.notification_new_sitesList_Toolbar);
        setSupportActionBar(toolbar);
        // Setup main toolbar with back button arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutManager = new LinearLayoutManager(this);

        newCoffeeSitesURLs = getIntent().getStringArrayListExtra("newCoffeeSitesURLs");
        numOfDaysToLoadLatestSites = getIntent().getIntExtra("daysBack", 0);

        if (newCoffeeSitesURLs == null && numOfDaysToLoadLatestSites == 0) { // this is request to show sites in town, not the latest sites
            handleSearchInTownIntent(getIntent());
        }

        doBindCoffeeSiteLoadOperationsService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSearchInTownIntent(intent);
    }

    /**
     * To handle search intent from SearchManager - searchViews in FoundCoffeeSiteActivity and in MainActivity
     * @param intent
     */
    private void handleSearchInTownIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            this.townName = query.trim();
            this.townName = Character.toUpperCase(townName.charAt(0)) + townName.substring(1);

            toolbar.setTitle(this.townName + " (0)");
        }
    }

    private void goToMainActivity() {
        Intent i = new Intent(StaticCoffeeSitesListActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_static_sites_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            case R.id.action_map:
                // Map can be opened even in Offline, if the user has downloaded a map ...?
                openMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starts MapsActivity to show found CoffeeSites. Available only Online.
     */
    private void openMap() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        for (CoffeeSite csm : content) {
            mapContent.add(csm);
        }
        mapIntent.putExtra("listContent", mapContent);
        startActivity(mapIntent);
    }

    /**
     * Loads either latest CoffeeSites or newly notified CoffeeSites
     */
    private void loadAllNewCoffeeSites() {
        if (Utils.isOnline(getApplicationContext())) {
            if (this.numOfDaysToLoadLatestSites > 0) { // load latest CoffeeSites, user request for Statistics
                content.clear();
                startLatestCoffeeSitesLoad(this.numOfDaysToLoadLatestSites);
                return;
            }
            if (newCoffeeSitesURLs != null && !newCoffeeSitesURLs.isEmpty()) { // load newly notified CoffeeSites
                content.clear();
                for (String coffeeSiteURL : newCoffeeSitesURLs) {
                    startCoffeeSiteLoad(coffeeSiteURL);
                }
            }
        }
    }

    private void startCoffeeSiteLoad(String coffeeSiteURL) {
        showProgressbar();
        coffeeSiteLoadOperationsService.findCoffeeSiteByURL(coffeeSiteURL);
    }

    private void startLatestCoffeeSitesLoad(int numOfDaysBack) {
        showProgressbar();
        coffeeSiteLoadOperationsService.getCoffeeSitesActivatedLastDays(numOfDaysBack);
    }

    private void startCoffeeSitesInTownLoad(String townName) {
        showProgressbar();
        coffeeSiteLoadOperationsService.getCoffeeSitesInTown(townName);
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void showProgressbar() {
        if (loadCoffeeSiteProgressBar.getVisibility() == GONE) {
            loadCoffeeSiteProgressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void hideProgressbar() {
        loadCoffeeSiteProgressBar.setVisibility(GONE);
    }

    /**
     * One CoffeeSites's data loaded from server, show the data.
     *
     * @param loadedCoffeeSite - CoffeeSite's data loaded from server
     * @param error - indication, if there was error during loading
     */
    @Override
    public void onCoffeeSiteLoaded(CoffeeSite loadedCoffeeSite, String error) {
        hideProgressbar();
        Log.i(TAG, "CoffeeSite load success?: " + error.isEmpty());
        if (error.isEmpty()) {
            if (loadedCoffeeSite != null) {
                content.add(loadedCoffeeSite);
            }
            if (newCoffeeSitesURLs != null && !newCoffeeSitesURLs.isEmpty()) {
                if (content.size() == newCoffeeSitesURLs.size()) { // all CoffeeSites loaded
                    prepareAndActivateRecyclerView(content);
                }
            }
        } else {
            showCoffeeSiteLoadFailure(error);
        }
    }

    /**
     * Latest CoffeeSites data loaded from server, show the data or error.
     *
     * @param loadedCoffeeSite - CoffeeSite's data loaded from server
     * @param error - indication, if there was error during loading
     */
    @Override
    public void onLatestCoffeeSitesLoaded(List<CoffeeSite> loadedCoffeeSites, String error) {
        hideProgressbar();
        if (!error.isEmpty()) {
            showCoffeeSiteLoadFailure(error);
            return;
        }
        Log.i(TAG, "CoffeeSites load success?: " + error.isEmpty());
        if (loadedCoffeeSites != null) {
            content = loadedCoffeeSites;
            if (!content.isEmpty()) {
                prepareAndActivateRecyclerView(content);
            }
        }
    }

    /**
     * CoffeeSites in town data loaded from server/DB, show the data or error.
     *
     * @param loadedCoffeeSite - CoffeeSite's data loaded from server
     * @param error - indication, if there was error during loading
     */
    @Override
    public void onCoffeeSitesInTownLoaded(List<CoffeeSite> loadedCoffeeSites, String error) {
        hideProgressbar();
        if (!error.isEmpty()) {
            showCoffeeSiteLoadFailure(error);
            return;
        }
        Log.i(TAG, "CoffeeSites in town load success.");
        if (loadedCoffeeSites != null) {
            content = loadedCoffeeSites;
            if (!content.isEmpty()) {
                toolbar.setTitle(this.townName + " (" + content.size() + ")");
                prepareAndActivateRecyclerView(content);
            }
        }
    }

    private void showCoffeeSiteLoadFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffee_site_load_failure);
        Toast toast = Toast.makeText(getApplicationContext(),
                error, Toast.LENGTH_SHORT);
        toast.show();
    }

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteLoadOperationsService;

    private void doBindCoffeeSiteLoadOperationsService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
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

    /**
     * When the CoffeeSiteLoadOperationsService is connected, loads the current CoffeeSite's data
     * from server.
     */
    @Override
    public void onCoffeeSiteServiceConnected() {
        if (coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService() != null) {
            coffeeSiteLoadOperationsService = coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService();
            coffeeSiteLoadOperationsService.addLoadOperationsListener(this);
            // refresh CoffeeSites after start
            if (content.isEmpty() && numOfDaysToLoadLatestSites > 0) {
                loadAllNewCoffeeSites();
                return; // return as only the new coffee sites or only sites in town are to be displayed in the Activity
            }
            if (content.isEmpty() && !townName.isEmpty()) {
                startCoffeeSitesInTownLoad(townName);
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

    private void prepareAndActivateRecyclerView(List<CoffeeSite> coffeeSites) {
        recyclerView = findViewById(R.id.notification_new_coffeesites_list);
        assert recyclerView != null;

        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new StaticCoffeeSitesListRecyclerViewAdapter(this, coffeeSites, mTwoPane);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     */
    @Override
    protected void onDestroy() {
        if (coffeeSiteLoadOperationsService != null) {
            coffeeSiteLoadOperationsService.removeLoadOperationsListener(this);
        }
        doUnbindCoffeeSiteLoadOperationsService();
        super.onDestroy();
    }
}
