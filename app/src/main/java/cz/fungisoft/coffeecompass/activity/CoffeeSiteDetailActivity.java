package cz.fungisoft.coffeecompass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.Utils;
import cz.fungisoft.coffeecompass.asynctask.GetCommentsAsyncTask;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteDetailFragment;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteImageFragment;

/**
 * An activity representing a single CoffeeSite detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CoffeeSiteListActivity}.
 */
public class CoffeeSiteDetailActivity extends ActivityWithLocationService {

    private CoffeeSiteListContent content;

    private String selectedItemID;

    private Button commentsButton;

    private CoffeeSiteDetailFragment detailFragment;

    private CoffeeSiteMovable coffeeSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_detail);

        commentsButton = (Button) findViewById(R.id.commentsButton);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        selectedItemID = getIntent().getStringExtra(CoffeeSiteDetailFragment.ARG_ITEM_ID);
        content = (CoffeeSiteListContent) getIntent().getSerializableExtra("listContent");

        coffeeSite = content.getItemsMap().get(selectedItemID);

        boolean imageAvail = !coffeeSite.getMainImageURL().isEmpty();

        if (imageAvail) {
            Button imageButton = (Button) findViewById(R.id.imageButton);
            imageButton.setEnabled(true);
        }

        // Async task to check if the Comments are available for the site
        if (Utils.isOnline()) {
            new GetCommentsAsyncTask(this, coffeeSite).execute();
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

            arguments.putString(CoffeeSiteDetailFragment.ARG_ITEM_ID, selectedItemID);
            detailFragment = new CoffeeSiteDetailFragment();
            detailFragment.setArguments(arguments);
//            detailFragment.setCoffeeSite(coffeeSite);
            detailFragment.setCoffeeSiteListContent(content);
        }
    }

    /**
     * locationService is not null here
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();
        coffeeSite.setLocationService(locationService);
        locationService.addPropertyChangeListener(coffeeSite);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.coffeesite_detail_container, detailFragment)
                .commit();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
//            navigateUpTo(new Intent(this, CoffeeSiteListActivity.class));

            /*
            * The standard way, navigateUpTo(new Intent(this, CoffeeSiteListActivity.class));
            * did not work for me. I have implemented this hint based on
            * internet advice. It transforms the Back button click from Navigation bar
            * to "main" Back button of Android click
             */
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onImageButtonClick(View v) {
        Intent imageIntent = new Intent(this, CoffeeSiteImageActivity.class);
//        imageIntent.putExtra("site", coffeeSite); //TODO check if only one coffeeSite could be passed to CoffeeSiteImageActivity
        imageIntent.putExtra("listContent", content);
        imageIntent.putExtra(CoffeeSiteImageFragment.ARG_ITEM_ID, String.valueOf(coffeeSite.getId()));
        startActivity(imageIntent);
    }

    public void onCommentsButtonClick(View v) {
        Intent commentsIntent = new Intent(this, CommentsListActivity.class);
        commentsIntent.putExtra("site", coffeeSite);
        startActivity(commentsIntent);
    }

    public void onMapButtonClick(View v) {
        if (locationService != null) {
            Intent mapIntent = new Intent(this, MapsActivity.class);
            mapIntent.putExtra("currentLocation", locationService.getCurrentLocation());
            mapIntent.putExtra("site", coffeeSite);
            startActivity(mapIntent);
        }
    }

    /**
     * To enable the commentsButton in case comments are available for the Coffee site
     * found in GetCommentsAsyncTask
     */
    public void enableCommentsButton() {
        commentsButton.setEnabled(true);
    }

}
