package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.support.DistanceChangeTextView;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.ui.fragments.CoffeeSiteImageFragment;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Activity to show main Image of the CoffeeSite and the distance attribute
 * of the CoffeeSite
 */
public class CoffeeSiteImageActivity extends ActivityWithLocationService
{
    private static final String TAG = "CoffeeSiteImageAct";

    private CoffeeSite cs;

    private DistanceChangeTextView distLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.coffee_site_image_activity);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            if (cs instanceof  CoffeeSiteMovable) {
                cs = (CoffeeSiteMovable) bundle.getParcelable("coffeeSite");
            } else {
                cs = bundle.getParcelable("coffeeSite");
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.image_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (toolbar != null && cs != null) {
            getSupportActionBar().setTitle("Fotky");
            toolbar.setSubtitle(cs.getName());
        }

        distLabel = (DistanceChangeTextView) findViewById(R.id.distTextView);
        if (cs instanceof CoffeeSiteMovable) {
            distLabel.setVisibility(View.VISIBLE);
            distLabel.setText(Utils.getDistanceInBetterReadableForm(cs.getDistance()));
            distLabel.setTag(TAG + ". DistanceTextView for " + cs.getName());
            distLabel.setCoffeeSite((CoffeeSiteMovable) cs);
        } else {
            distLabel.setVisibility(View.GONE);
        }


        CoffeeSiteImageFragment fragment = new CoffeeSiteImageFragment();

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
        if (cs != null && cs instanceof  CoffeeSiteMovable) {
            ((CoffeeSiteMovable)cs).setLocationService(locationService);
            locationService.addPropertyChangeListener((CoffeeSiteMovable) cs);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cs instanceof  CoffeeSiteMovable) {
            ((CoffeeSiteMovable) cs).removePropertyChangeListener(distLabel);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cs instanceof  CoffeeSiteMovable) {
            ((CoffeeSiteMovable) cs).addPropertyChangeListener(distLabel);
            distLabel.setText(Utils.getDistanceInBetterReadableForm(cs.getDistance()));
        }
    }

    @Override
    public void onStop() {
        if (cs instanceof  CoffeeSiteMovable) {
            locationService.removePropertyChangeListener((CoffeeSiteMovable) cs);
        }
        super.onStop();;
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
