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

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteImageFragment;

public class CoffeeSiteImageActivity extends AppCompatActivity {

    private CoffeeSite cs;

    private TextView distLabel;

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
        distLabel.setText("Vzdálenost: "+ String.valueOf(cs.getDistance()) + " m");

        CoffeeSiteImageFragment fragment = new CoffeeSiteImageFragment();
        fragment.setCoffeeSiteId(cs);

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
