package cz.fungisoft.coffeecompass2.activity.ui.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
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
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static android.view.View.GONE;


/**
 * Activity to show list of new CoffeeSites received upon subscribed notification.
 * Allows to select the new CoffeeSite and open detail activity for that new CoffeeSite.
 */
public class NotificationNewCoffeeSitesListActivity extends AppCompatActivity
                                                    implements CoffeeSiteServicesConnectionListener,
                                                               CoffeeSiteLoadServiceOperationsListener  {

    private static final String TAG = "NotificationsListAct";

    /**
     * The main attribute of activity containing all new CoffeeSites to show
     * within this Activities
     */
    private final List<CoffeeSite> content = new ArrayList<>();

    /**
     * URLs of the new CoffeeSites
     */
    private List<String> newCoffeeSitesURLs;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private NotificationNewCoffeeSitesRecyclerViewAdapter recyclerViewAdapter;
    private Parcelable mListState;

    private static final String LIST_STATE_KEY = "CoffeeSiteList";

    protected CoffeeSiteLoadOperationsService coffeeSiteLoadOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteLoadOperationsService> coffeeSiteLoadOperationsServiceConnector;

    private ProgressBar loadCoffeeSiteProgressBar;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device. Not used, now.
     */
    private boolean mTwoPane;

    public NotificationNewCoffeeSitesListActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notification_activity_new_coffee_sites_list);

        loadCoffeeSiteProgressBar = findViewById(R.id.progress_notification_new_coffeesites_load);

        newCoffeeSitesURLs = getIntent().getStringArrayListExtra("newCoffeeSitesURLs");

        if (savedInstanceState != null && content != null) { // i.e. after orientation was changed
            Collections.sort(content);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.notification_new_sitesList_Toolbar);
        setSupportActionBar(toolbar);
        // Setup main toolbar with back button arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutManager = new LinearLayoutManager(this);

        doBindCoffeeSiteLoadOperationsService();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToMainActivity() {
        Intent i = new Intent(NotificationNewCoffeeSitesListActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void loadAllNewCoffeeSites() {
        if (Utils.isOnline()) {
            for (String coffeeSiteURL : newCoffeeSitesURLs) {
                startCoffeeSiteLoad(coffeeSiteURL);
            }
        }
    }

    public void startCoffeeSiteLoad(String coffeeSiteURL) {
        showProgressbar();
        coffeeSiteLoadOperationsService.findCoffeeSiteByURL(coffeeSiteURL);
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
     * CoffeeSites's data loaded from server, show the data.
     *
     * @param loadedCoffeeSite - CoffeeSite's data loaded from server
     * @param error - indication, if there was error during loading
     */
    @Override
    public void onCoffeeSiteLoaded(CoffeeSite loadedCoffeeSite, String error) {
        hideProgressbar();
        Log.i(TAG, "CoffeeSite load success?: " + error.isEmpty());
        if (error.isEmpty()) {
            // Loaded actual instance of CoffeeSite - transform it to CoffeeSiteMovable
            if (loadedCoffeeSite != null) {
                content.add(loadedCoffeeSite);
            }
            if (content.size() == newCoffeeSitesURLs.size()) {
                prepareAndActivateRecyclerView(content);
            }
        } else {
            showCoffeeSiteLoadFailure(error);
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
            // refresh CoffeeSite after start
            if (content.isEmpty()) {
                loadAllNewCoffeeSites();
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
        //Bundle extras = getIntent().getExtras();
        recyclerView = findViewById(R.id.notification_new_coffeesites_list);
        assert recyclerView != null;

        recyclerView.setLayoutManager(layoutManager);
        //boolean offLineModeOn = Utils.isOfflineModeOn(getApplicationContext());
        recyclerViewAdapter = new NotificationNewCoffeeSitesRecyclerViewAdapter(this, coffeeSites, mTwoPane);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    /**
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindCoffeeSiteLoadOperationsService();
    }
}
