package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.MapsActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.FoundCoffeeSitesViewModel;
import cz.fungisoft.coffeecompass2.activity.ui.notification.StaticCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.activity.ui.notification.TownNamesArrayAdapter;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovableListContent;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesFoundService;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesFoundListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeSearchOperationListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.widgets.MainAppWidgetProvider;


/**
 * An activity representing a list of CoffeeSites currently in the search range.
 * <p>
 * This activity has different presentations for handset and tablet-size devices.
 * On handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CoffeeSiteDetailActivity} representing
 * item details.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p><p/>
 * Uses FoundCoffeeSitesViewModel, which holds information about CoffeeSites
 * newly entered into search range and CoffeeSites, which left the current range.
 * This is used to be inserted to FoundCoffeeSitesRecyclerViewAdapter, which shows
 * changes of CoffeeSites in the current search range (and location).
 * <p>
 */
public class FoundCoffeeSitesListActivity extends ActivityWithLocationService
                                          implements CoffeeSitesInRangeSearchOperationListener,
                                                     CoffeeSitesInRangeServiceConnectionListener,
                                                     CoffeeSitesFoundListener {

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

    /**
     * Adapter to show current CoffeeSites in range.
     */
    private FoundCoffeeSitesRecyclerViewAdapter recyclerViewAdapter;
    private Parcelable mListState;

    private int searchRange;
    private LatLng searchLocation;
    private String searchCoffeeSort;

    private static final String LIST_STATE_KEY = "CoffeeSiteList";

    private Toolbar toolbar;
    private String originalToolbarTitle;

    /**
     * Model providing "new" and "old" CoffeeSites to the Activity and it's recycler view adapter
     */
    private static FoundCoffeeSitesViewModel coffeeSitesViewModel;

    /**
     * Flag to indicate, if current search is based on location/range or town
     * Used to update view/recyclerViewAdapter in case searching is finished.
     */
    private boolean searchingInRange = true;

    /**
     * Service which provides updates of CoffeeSites list in the current
     * search Range to FoundCoffeeSitesViewModel to further evaluation of the "new"/"old"
     * CoffeeSites.
     */
    private CoffeeSitesFoundService foundSitesService;
    private CoffeeSitesInRangeUpdateServiceConnector sitesInRangeUpdateServiceConnector;
    private boolean mShouldUpdateSitesInRangeUnbind;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_coffeesites_list);

        this.searchLocation = (LatLng) getIntent().getExtras().get("latLongFrom");
        this.searchCoffeeSort = (String) getIntent().getExtras().get("coffeeSort");
        if (getIntent().getExtras().get("searchRange") != null) {
            this.searchRange = (int) getIntent().getExtras().get("searchRange");
        }

        toolbar = (Toolbar) findViewById(R.id.sitesListToolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        setTitle(R.string.found_coffeesite_activity_title);
        originalToolbarTitle = String.valueOf(getTitle());
        layoutManager = new LinearLayoutManager(this);

        doBindSitesInRangeService();

        prepareAndActivateRecyclerView();
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
        mListState = state.getParcelable(LIST_STATE_KEY);
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
        super.onDestroy();
        foundSitesService.removeSitesFoundListener(coffeeSitesViewModel);
        doUnBindSitesInRangeService();
    }


    private void doBindSitesInRangeService() {
        // Attempts to establish a connection with the service. We use an
        // explicit class name because we want a specific service implementation,
        // that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
        sitesInRangeUpdateServiceConnector = new CoffeeSitesInRangeUpdateServiceConnector(this);
        if (bindService(new Intent(this, CoffeeSitesFoundService.class),
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
            if (foundSitesService != null) {
                foundSitesService.removeFoundSitesSearchOperationListener(this);
            }
            unbindService(sitesInRangeUpdateServiceConnector);
            mShouldUpdateSitesInRangeUnbind = false;
        }
    }

    @Override
    public void onCoffeeSitesInRangeUpdateServiceConnected() {
        foundSitesService = sitesInRangeUpdateServiceConnector.getSitesInRangeUpdateService();
        foundSitesService.addFoundSitesSearchOperationListener(this);

        coffeeSitesViewModel = new FoundCoffeeSitesViewModel(this);
        coffeeSitesViewModel.setCoffeeSitesInRangeFoundService(foundSitesService);
        foundSitesService.addSitesFoundListener(coffeeSitesViewModel);

        if (coffeeSitesViewModel != null) {
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
                            currentContent.getItems().add(csm);
                        }
                    }
                    recyclerViewAdapter.onNewSitesInRange(newCoffeeSitesInRange);
                    MainAppWidgetProvider.updateCoffeeSiteWidget(getApplicationContext(), newCoffeeSitesInRange, true); // update Widget
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
                            currentContent.getItems().remove(csm);
                        }
                    }
                    recyclerViewAdapter.onSitesOutOfRange(goneCoffeeSitesInRange, searchingInRange);
                    MainAppWidgetProvider.updateCoffeeSiteWidget(getApplicationContext(), currentContent.getItems(), true); // update Widget
                }
            });
        }

        startSearchingSites();
    }

    /**
     * Processes changes of the 'new' and/or 'old' CoffeeSites LiveData in range
     * as provided by coffeeSitesViewModel
     */
    private void startSearchingSites() {
        final int currentSearchRange = this.searchRange;
        final LatLng currentSearchFromLocation = this.searchLocation;

        if (foundSitesService != null) {
            foundSitesService.requestUpdatesOfCurrentSitesInRange(currentSearchFromLocation, currentSearchRange, this.searchCoffeeSort);
        }
    }

    @SuppressLint("RestrictedApi") // due to searchAutoComplete.setThreshold(2);
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // StaticCoffeeSitesListActivity is the activity to show result of searchView input
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
                    // handleSearchIntent(); follows
                }
                Log.i(TAG, "Selected town: " + townName);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_map:
                // Map can be opened even in Offline, if the user has downloaded a map ...?
                openMap();
                return true;
            case android.R.id.home:
                goToMainActivityAndFinish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToMainActivityAndFinish() {
        // go to MainActivity
        goToMainActivity();
        finish();
    }

    private void goToMainActivity() {
        Intent i = new Intent(FoundCoffeeSitesListActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
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
    public void onStartSearchingSites() {
        recyclerViewAdapter.newSitesSearchingStarted();
    }

    @Override
    public void onSearchingSitesFinished() {
        recyclerViewAdapter.newSitesSearchingFinished();
        if (recyclerViewAdapter.getCurrentNumberOfSitesShown() > 0) {
            //toolbar.setTitle(originalToolbarTitle + ": " + Utils.convertSearchDistanceNoBrackets(this.searchRange) + " (" + recyclerViewAdapter.getCurrentNumberOfSitesShown() + ")");
            toolbar.setTitle(originalToolbarTitle + " (" + recyclerViewAdapter.getCurrentNumberOfSitesShown() + ")");
        } else {
            toolbar.setTitle(originalToolbarTitle);
        }
    }

    @Override
    public void onSearchingSitesError(String error) {
        recyclerViewAdapter.newSitesSearchingFinished();
        Toast toast = Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT);
        toast.show();
    }
}