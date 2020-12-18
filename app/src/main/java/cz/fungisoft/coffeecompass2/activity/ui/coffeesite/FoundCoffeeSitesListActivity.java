package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.MapsActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.FoundCoffeeSitesViewModel;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovableListContent;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeFoundService;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeSearchOperationListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;


/**
 * An activity representing a list of CoffeeSites.
 * This activity has different presentations for handset and tablet-size devices.
 * On handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CoffeeSiteDetailActivity} representing
 * item details.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p><p/>
 * Used to start AsyncTask for searching CoffeeSites in range and to show
 * list of found CoffeeSites based on location.
 * <p>
 * Another activity is used to show CoffeeSites created by user.
 */
public class FoundCoffeeSitesListActivity extends ActivityWithLocationService
                                          implements CoffeeSitesInRangeSearchOperationListener,
                                                     CoffeeSitesInRangeServiceConnectionListener {
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
    private final CoffeeSiteMovableListContent currentContent = new CoffeeSiteMovableListContent();

    private FoundCoffeeSitesRecyclerViewAdapter recyclerViewAdapter;
    private Parcelable mListState;

    private int searchRange;
    private LatLng searchLocation;
    private String searchCoffeeSort;

    private static final String LIST_STATE_KEY = "CoffeeSiteList";

    private Toolbar toolbar;
    private String originalToolbarTitle;

    /**
     * Model providing found coffeeSites to Activity and it's recycler view adapter
     */
    private static FoundCoffeeSitesViewModel coffeeSitesViewModel;

    /**
     * Service which provides updates of CoffeeSites list in the current
     * search Range
     */
    private static CoffeeSitesInRangeFoundService sitesInRangeUpdateService;
    private static CoffeeSitesInRangeUpdateServiceConnector sitesInRangeUpdateServiceConnector;
    private boolean mShouldUpdateSitesInRangeUnbind;


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

        this.searchLocation = (LatLng) getIntent().getExtras().get("latLongFrom");
        this.searchCoffeeSort = (String) getIntent().getExtras().get("coffeeSort");
        this.searchRange = (int) getIntent().getExtras().get("searchRange");

        toolbar = (Toolbar) findViewById(R.id.sitesListToolbar);
        setSupportActionBar(toolbar);

        originalToolbarTitle = String.valueOf(getTitle());

        layoutManager = new LinearLayoutManager(this);

        sitesInRangeUpdateServiceConnector = new CoffeeSitesInRangeUpdateServiceConnector(this);
        if (sitesInRangeUpdateService == null && !mShouldUpdateSitesInRangeUnbind) {
            doBindSitesInRangeService();
        }
        else {
            startSearchingSites();
        }

        prepareAndActivateRecyclerView();
    }

    /**
     * Setup locationService listeners and RecyclerView which also
     * requires locationService to be activated/connected.
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();
    }

    private void prepareAndActivateRecyclerView() {
        Bundle extras = getIntent().getExtras();
        recyclerView = findViewById(R.id.coffeesite_list);
        assert recyclerView != null;

        recyclerView.setLayoutManager(layoutManager);
        if (extras != null) {
            setupRecyclerView( recyclerView);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerViewAdapter = new FoundCoffeeSitesRecyclerViewAdapter(this, this.searchRange, mTwoPane);
        recyclerView.setAdapter(recyclerViewAdapter);
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
        if (state != null) {
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
        if ((locationService != null) && currentContent.getItems().size() > 0) {
            for (CoffeeSiteMovable csm : currentContent.getItems()) {
                if (csm.isLocationServiceAssigned()) {
                    locationService.removePropertyChangeListener(csm);
                }
            }
        }

        sitesInRangeUpdateService.removeSitesInRangeFoundListener(coffeeSitesViewModel);

        doUnBindSitesInRangeService();
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
                    Utils.showMapNotAvailableIfNoInternetToast(getApplicationContext());
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starts MapsActivity to show found CoffeeSites. Available only Online.
     */
    private void openMap() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("currentLocation", locationService.getCurrentLatLng());
        mapIntent.putExtra("listContent", currentContent);
        startActivity(mapIntent);
    }

    @Override
    public void onStartSearchingSitesInRange() {
        recyclerViewAdapter.newSitesInRangeSearchingStarted();
    }

    @Override
    public void onSearchingSitesInRangeFinished() {
        recyclerViewAdapter.newSitesInRangeSearchingFinished();
        if (recyclerViewAdapter.getCurrentNumberOfSitesShown() > 0) {
            toolbar.setTitle(originalToolbarTitle + " : " + Utils.convertSearchDistanceNoBrackets(this.searchRange) + " (" + recyclerViewAdapter.getCurrentNumberOfSitesShown() + ")");
        } else {
            toolbar.setTitle(originalToolbarTitle);
        }
    }

    @Override
    public void onSearchingSitesInRangeError(String error) {
        recyclerViewAdapter.newSitesInRangeSearchingFinished();
        Toast toast = Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT);
        toast.show();
    }


    private void doBindSitesInRangeService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
        if (bindService(new Intent(this, CoffeeSitesInRangeFoundService.class),
                sitesInRangeUpdateServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUpdateSitesInRangeUnbind = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSitesInRangeUpdateService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    public void doUnBindSitesInRangeService() {
        // Release information about the service's state.
        if (mShouldUpdateSitesInRangeUnbind) {
            if (sitesInRangeUpdateService != null) {
                sitesInRangeUpdateService.removeSitesInRangeSearchOperationListener(this);
            }
            unbindService(sitesInRangeUpdateServiceConnector);
            mShouldUpdateSitesInRangeUnbind = false;
        }
    }

    @Override
    public void onCoffeeSitesInRangeUpdateServiceConnected() {
        sitesInRangeUpdateService = sitesInRangeUpdateServiceConnector.getSitesInRangeUpdateService();
        sitesInRangeUpdateService.addSitesInRangeSearchOperationListener(this);

        coffeeSitesViewModel = FoundCoffeeSitesViewModel.getInstance(getApplication(), sitesInRangeUpdateService);
        sitesInRangeUpdateService.addSitesInRangeFoundListener(coffeeSitesViewModel);

        startSearchingSites();
    }

    private void startSearchingSites() {
        final int currentSearchRange = this.searchRange;
        final LatLng currentSearchFromLocation = this.searchLocation;

        if (sitesInRangeUpdateService != null) {
            sitesInRangeUpdateService.requestUpdatesOfCurrentSitesInRange(currentSearchFromLocation, currentSearchRange, this.searchCoffeeSort);
        }
        if (coffeeSitesViewModel != null) {
            coffeeSitesViewModel.getFoundCoffeeSites().observe(this, new Observer<List<CoffeeSiteMovable>>() {
                @Override
                public void onChanged(@Nullable final List<CoffeeSiteMovable> coffeeSitesInRange) {
                    // Process found CoffeeSites - leads to update list of new and gone CoffeeSites, see below
                    coffeeSitesViewModel = coffeeSitesViewModel.processFoundCoffeeSites(coffeeSitesInRange);
                }
            });

            coffeeSitesViewModel.getNewSitesInRange().observe(this, new Observer<List<CoffeeSiteMovable>>() {
                @Override
                public void onChanged(@Nullable final List<CoffeeSiteMovable> newCoffeeSitesInRange) {
                    // Update the cached copy of the newCoffeeSitesInRange in the adapter.
                    assert newCoffeeSitesInRange != null;
                    for (CoffeeSiteMovable csm : newCoffeeSitesInRange) {
                        // Add new CoffeeSites as locationService listeners
                        csm.setLocationService(locationService);
                        if (locationService != null) {
                            locationService.addPropertyChangeListener(csm);
                        }
                    }
                    currentContent.setItems(newCoffeeSitesInRange);
                    recyclerViewAdapter.onNewSitesInRange(newCoffeeSitesInRange);
                    onSearchingSitesInRangeFinished();
                }
            });

            coffeeSitesViewModel.getGoneSitesOutOfRange().observe(this, new Observer<List<CoffeeSiteMovable>>() {
                @Override
                public void onChanged(@Nullable final List<CoffeeSiteMovable> goneCoffeeSitesInRange) {
                    // Update the cached copy of the goneCoffeeSitesInRange in the adapter.
                    assert goneCoffeeSitesInRange != null;
                    for (CoffeeSiteMovable csm : goneCoffeeSitesInRange) {
                        if (locationService != null) {
                            locationService.removePropertyChangeListener(csm);
                        }
                    }
                    recyclerViewAdapter.onSitesOutOfRange(goneCoffeeSitesInRange);
                    onSearchingSitesInRangeFinished();
                }
            });
        }
    }

}