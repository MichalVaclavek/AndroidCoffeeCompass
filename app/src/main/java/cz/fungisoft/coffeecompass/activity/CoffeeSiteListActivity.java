package cz.fungisoft.coffeecompass.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.Utils;
import cz.fungisoft.coffeecompass.activity.support.CoffeeSiteItemRecyclerViewAdapterForCoffeeSites;
import cz.fungisoft.coffeecompass.activity.support.DistanceChangeTextView;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass.services.LocationService;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteDetailFragment;


/**
 * An activity representing a list of CoffeeSites. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CoffeeSiteDetailActivity} representing
 * item details.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CoffeeSiteListActivity extends ActivityWithLocationService {

    private static final String TAG = "CoffeeSiteListActivity";

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
    private CoffeeSiteListContent content;

    private CoffeeSiteItemRecyclerViewAdapterForCoffeeSites recyclerViewAdapter;
    private Parcelable mListState;

    private static final String LIST_STATE_KEY = "CoffeeSiteList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.sitesListToolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.coffeesite_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        content = (CoffeeSiteListContent) getIntent().getSerializableExtra("listContent");
        if (savedInstanceState != null) { // i.e. after orientation was changed
            Collections.sort(content.getItems());
        }

        layoutManager = new LinearLayoutManager(this);
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

    private void prepareAndActivateRecyclerView() {
        Bundle extras = getIntent().getExtras();
        recyclerView = findViewById(R.id.coffeesite_list);
        assert recyclerView != null;

        recyclerView.setLayoutManager(layoutManager);
        if (extras != null) {
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
        if(state != null)
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
        if (locationService != null) {
            for (CoffeeSiteMovable csm : content.getItems()) {
                locationService.removePropertyChangeListener(csm);
            }
        }
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

    private void openMap() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("currentLocation", locationService.getCurrentLocation());
        mapIntent.putExtra("listContent", content);
        startActivity(mapIntent);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, CoffeeSiteListContent listContent) {
        recyclerViewAdapter = new CoffeeSiteItemRecyclerViewAdapterForCoffeeSites(this, listContent, locationService , mTwoPane);
        for (CoffeeSiteMovable csm : content.getItems()) {
            csm.addPropertyChangeListener(recyclerViewAdapter);
        }
        recyclerView.setAdapter(recyclerViewAdapter);
    }

}
