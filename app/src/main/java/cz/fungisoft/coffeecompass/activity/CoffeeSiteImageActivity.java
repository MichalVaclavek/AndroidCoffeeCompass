package cz.fungisoft.coffeecompass.activity;

import android.os.Bundle;

//import android.support.design.widget.CollapsingToolbarLayout;
//import android.support.design.widget.C
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.services.UpdateDistanceTimerTask;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteImageFragment;

/**
 * Activity to show main Image of the CoffeeSite and the distance attribute
 * of the CoffeeSite
 */
public class CoffeeSiteImageActivity extends ActivityWithLocationService {

    private CoffeeSite cs;
    private LatLng siteLatLng;

    private TextView distLabel;

    private UpdateDistanceTimerTask checkingDistanceTimerTask;

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

        cs = (CoffeeSite) getIntent().getSerializableExtra("site");

        if (appBarLayout != null) {
            appBarLayout.setTitle(cs.getName());
        }

        distLabel = (TextView) findViewById(R.id.distTextView);
        distLabel.setText("Vzdálenost: " + String.valueOf(cs.getDistance()) + " m");

        CoffeeSiteImageFragment fragment = new CoffeeSiteImageFragment();
        fragment.setCoffeeSite(cs);

        siteLatLng = new LatLng(cs.getLatitude(), cs.getLongitude());

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
    public void onDestroy() {
        if (checkingDistanceTimerTask != null) {
            checkingDistanceTimerTask.stopTimerTask();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (checkingDistanceTimerTask != null) {
            checkingDistanceTimerTask.stopTimerTask();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkingDistanceTimerTask != null && !checkingDistanceTimerTask.isRunning() && locationService != null) {
            checkingDistanceTimerTask  = new UpdateDistanceTimerTask(this, -1, siteLatLng, locationService);
            checkingDistanceTimerTask.startTimerTask(1000, 1000);
        }
    }

    @Override
    public void updateDistanceTextViewAndOrModel(int position, long meters) {
        cs.setDistance(meters);
        distLabel.setText("Vzdálenost: " + String.valueOf(meters) + " m");
    }

    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();
        checkingDistanceTimerTask  = new UpdateDistanceTimerTask(this, -1, siteLatLng, locationService);
        checkingDistanceTimerTask.startTimerTask(1000, 1000);
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
