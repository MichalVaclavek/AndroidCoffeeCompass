package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.MapsActivity;
import cz.fungisoft.coffeecompass2.activity.support.CoffeeSiteMovableItemRecyclerViewAdapter;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovableListContent;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateService;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.SitesInRangeUpdateListener;


/**
 * An activity representing a list of CoffeeSites. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CoffeeSiteDetailActivity} representing
 * item details.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p><p/>
 * Used to show list of found CoffeeSites based on location.
 * Another activity is used to show CoffeeSites created by user.
 */
public class FoundCoffeeSitesListActivity extends ActivityWithLocationService implements SitesInRangeUpdateListener {

    private static final String TAG = "FoundCoffeeSitesAct";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    /**
     * The main attribute of activity containing all the CoffeeSites to show
     * on this or child Activities
     */
    private CoffeeSiteMovableListContent content;

    private CoffeeSiteMovableItemRecyclerViewAdapter recyclerViewAdapter;
    private Parcelable mListState;

    /**
     * Service which provides updates of CoffeeSites list in the current
     * search Range
     */
    private CoffeeSitesInRangeUpdateService sitesInRangeUpdateService;
    private CoffeeSitesInRangeUpdateServiceConnector sitesInRangeUpdateServiceConnector;
    private boolean mShouldUpdateSitesInRangeUnbind;
    private Intent updateSitesServiceIntent;

    private int searchRange;
    private LatLng searchLocation;
    private String searchCoffeeSort;

    private static final String LIST_STATE_KEY = "CoffeeSiteList";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_list);

        if (findViewById(R.id.coffeesite_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        content = getIntent().getParcelableExtra("listContent");
        if (savedInstanceState != null) { // i.e. after orientation was changed
            Collections.sort(content.getItems());
        }

        this.searchLocation = (LatLng) getIntent().getExtras().get("latLongFrom");
        this.searchCoffeeSort = (String) getIntent().getExtras().get("coffeeSort");
        this.searchRange = (int) getIntent().getExtras().get("searchRange");

        Toolbar toolbar = (Toolbar) findViewById(R.id.sitesListToolbar);
        String searchRangeString;
        // Prevod na km
        if (searchRange >= 1000) {
            searchRangeString = " (" + searchRange/1000 + " km)";
        } else {
            searchRangeString = " (" + searchRange + " m)";
        }
        toolbar.setTitle(getTitle() + searchRangeString);
        setSupportActionBar(toolbar);

        layoutManager = new LinearLayoutManager(this);

        updateSitesServiceIntent = new Intent(this, CoffeeSitesInRangeUpdateService.class);
        startService(updateSitesServiceIntent);

        doBindSitesInRangeService();
    }

    /**
     * Setup locationService listeners and RecyclerView which also
     * requires locationService to be activated/connected.
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();

        if (content != null) {
            for (CoffeeSiteMovable csm : content.getItems()) {
                csm.setLocationService(locationService);
                locationService.addPropertyChangeListener(csm);
            }
        }
        prepareAndActivateRecyclerView();
    }

    @Override
    public void onCoffeeSitesInRangeUpdateServiceConnected() {
        sitesInRangeUpdateService = sitesInRangeUpdateServiceConnector.getSitesInRangeUpdateService();
        sitesInRangeUpdateService.addSitesInRangeUpdateListener(this);
        if (this.content != null) {
            sitesInRangeUpdateService.requestUpdatesOfCurrentSitesInRange(this.content.getItems(), this.searchLocation, this.searchRange, this.searchCoffeeSort);
        }
    }

    private void prepareAndActivateRecyclerView() {
        Bundle extras = getIntent().getExtras();
        recyclerView = findViewById(R.id.coffeesite_list);
        assert recyclerView != null;

        recyclerView.setLayoutManager(layoutManager);
        if (extras != null && content != null) {
            Collections.sort(content.getItems());
            setupRecyclerView((RecyclerView) recyclerView, content);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save CoffeeSites list state
        mListState = layoutManager.onSaveInstanceState();
        state.putParcelable(LIST_STATE_KEY, mListState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Retrieve CoffeeSites list state and item positions
        if(state != null) {
            mListState = state.getParcelable(LIST_STATE_KEY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mListState != null) {
            layoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // save RecyclerView state
        Bundle listState = new Bundle();
        mListState = recyclerView.getLayoutManager().onSaveInstanceState();
        listState.putParcelable(LIST_STATE_KEY, mListState);
    }

    @Override
    public void onDestroy() {
        if (locationService != null) {
            for (CoffeeSiteMovable csm : content.getItems()) {
                if (csm.isLocationServiceAssigned()) {
                    locationService.removePropertyChangeListener(csm);
                }
            }
        }

        if (sitesInRangeUpdateService != null) {
            sitesInRangeUpdateService.removePropertyChangeListener(this);
            doUnBindSitesInRangeService();
        }

        stopService(updateSitesServiceIntent);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_map:
                if (Utils.isOnline()) {
                    openMap();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "No Internet connection.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starts MapsActivity to show found CoffeeSites
     */
    private void openMap() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("currentLocation", locationService.getCurrentLatLng());
        mapIntent.putExtra("listContent", (Parcelable) content);
        startActivity(mapIntent);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, CoffeeSiteMovableListContent listContent) {
        //recyclerViewAdapter = new CoffeeSiteMovableItemRecyclerViewAdapter(this, listContent, this.searchRange, locationService , mTwoPane);
        recyclerViewAdapter = new CoffeeSiteMovableItemRecyclerViewAdapter(this, listContent, this.searchRange, mTwoPane);
        for (CoffeeSiteMovable csm : content.getItems()) {
            csm.addPropertyChangeListener(recyclerViewAdapter);
        }
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void doBindSitesInRangeService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        sitesInRangeUpdateServiceConnector = new CoffeeSitesInRangeUpdateServiceConnector(this);
        if (bindService(updateSitesServiceIntent, sitesInRangeUpdateServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUpdateSitesInRangeUnbind = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSitesInRangeUpdateService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnBindSitesInRangeService() {
        if (mShouldUpdateSitesInRangeUnbind) {
            // Release information about the service's state.
            unbindService(sitesInRangeUpdateServiceConnector);
            mShouldUpdateSitesInRangeUnbind = false;
        }
    }

    @Override
    public void onNewSitesInRange(List<CoffeeSiteMovable> newSitesInRange) {
        // Add all new sites into current list
        for (CoffeeSiteMovable csm : newSitesInRange) {
            // First add new CoffeeSites as locationService listeners
            csm.setLocationService(locationService);
            locationService.addPropertyChangeListener(csm);
            // Listen to location change to allow correct sorting according distance
            csm.addPropertyChangeListener(recyclerViewAdapter);
        }
        recyclerViewAdapter.insertNewSites(newSitesInRange);
    }

    @Override
    public void onSitesOutOfRange(List<CoffeeSiteMovable> goneSitesOutOfRange) {
        recyclerViewAdapter.removeOldSites(goneSitesOutOfRange);
        // Remove CoffeeSiteMovable as a locationService listener
        // as it will be removed from displaying in a list
        for (CoffeeSiteMovable csm : goneSitesOutOfRange) {
            locationService.removePropertyChangeListener(csm);
            csm.removePropertyChangeListener(recyclerViewAdapter);
        }
    }

}