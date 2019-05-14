package cz.fungisoft.coffeecompass2.activity;

import android.os.Bundle;

//import android.support.design.widget.CollapsingToolbarLayout;
//import android.support.design.widget.C
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.support.DistanceChangeTextView;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.ui.fragments.CoffeeSiteDetailFragment;
import cz.fungisoft.coffeecompass2.ui.fragments.CoffeeSiteImageFragment;

/**
 * Activity to show main Image of the CoffeeSite and the distance attribute
 * of the CoffeeSite
 */
public class CoffeeSiteImageActivity extends ActivityWithLocationService
{
    private static final String TAG = "CoffeeSiteImageAct";

    private CoffeeSiteMovable cs;

    private DistanceChangeTextView distLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.coffee_site_image_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.image_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout appBarLayout = findViewById(R.id.image_toolbar_layout);

        cs = (CoffeeSiteMovable) getIntent().getSerializableExtra("coffeeSite");

        if (appBarLayout != null) {
            appBarLayout.setTitle(cs.getName());
        }

        distLabel = (DistanceChangeTextView) findViewById(R.id.distTextView);
        distLabel.setText(String.valueOf(cs.getDistance()) + " m");
        distLabel.setTag(TAG + ". DistanceTextView for " + cs.getName());
        distLabel.setCoffeeSite(cs);

        CoffeeSiteImageFragment fragment = new CoffeeSiteImageFragment();

//        Bundle arguments = new Bundle();

        fragment.setCoffeeSite(cs);

        if (savedInstanceState == null) { // is this enough?
            if (cs != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.imageContainer, fragment)
                        .commitNow();
            } else {
                imageNotAvailable();
            }
        }
    }

    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();
        if (cs != null) {
            cs.setLocationService(locationService);
            locationService.addPropertyChangeListener(cs);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        cs.removePropertyChangeListener(distLabel);
//        Log.d(TAG, ". Distance Text View " + distLabel.getTag() + " removed to listen distance change of " + cs.getName() + ". Object id: " + cs);

        // Listener for Location service can be removed, as there is no 'follow' Activity, from which
        // the CoffeeSiteImageActivity could be called back
        if (locationService != null) {
            locationService.removePropertyChangeListener(cs);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationService != null) {
            locationService.addPropertyChangeListener(cs);
        }
        cs.addPropertyChangeListener(distLabel);
        distLabel.setText(String.valueOf(cs.getDistance()) + " m");
//        Log.d(TAG, ". Distance Text View " + distLabel.getTag() + " added to listen distance change of " + cs.getName() + ". Object id: " + cs);
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

    public void imageNotAvailable() {
    }

}