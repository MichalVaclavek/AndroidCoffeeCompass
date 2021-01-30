package cz.fungisoft.coffeecompass2.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import cz.fungisoft.coffeecompass2.R;

/**
 * Shows the About app. info, author, version, aim of the app.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Setup main toolbar with back button arrow
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.aboutl_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mainToolbar != null) {
            getSupportActionBar().setTitle(getString(R.string.about_toolbar_title));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
