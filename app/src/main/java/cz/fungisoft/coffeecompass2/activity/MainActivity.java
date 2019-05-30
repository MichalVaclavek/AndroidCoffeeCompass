package cz.fungisoft.coffeecompass2.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.asynctask.GetSitesInRangeAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.ReadStatsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.Statistics;

/**
 * Main activity to show:
 *  - search buttons to find either ESPRESSO CoffeeSites only or any CoffeeSite within specified distance range
 *  - info about location of the phone and accuracy of this location
 *  - basic statistics info about CoffeeSites and Users
 *
 *  Is capable to detect it's current location to allow searching of CoffeeSites based on current location.
 *  Calls standard Android service to detect location based on GPS or network info.
 */
public class MainActivity extends ActivityWithLocationService implements PropertyChangeListener {

    private static final int LOCATION_REQUEST_CODE = 101;
    private static final String TAG = "MainActivity";

    private static final long MAX_STARI_DAT = 1000 * 120; // pokud jsou posledni zname udaje o poloze starsi jako 2 minuty, zjistit nove (po spusteni app.)
    private static final float GOOD_PRESNOST = 10.0f;
    private static final float LAST_PRESNOST = 500.0f;

    private boolean bPrvni = true;
    private int barva = Color.BLACK;

    private TextView accuracy;

    private ImageView locationImageView;

    private Location location;

    private Button searchEspressoButton;
    private Button searchKafeButton;

    private int searchRange = 500; // range in meters for searching from current position - 500 m default value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        findViewById(R.id.AllSitesTextView);

         if (getIntent().getStringExtra("searchRange") != null) {
            this.searchRange = Integer.parseInt(getIntent().getStringExtra("searchRange"));
        }

        //Location info
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE);

        accuracy = (TextView) findViewById(R.id.accuracy);

        searchEspressoButton = (Button) findViewById(R.id.searchEspressoButton);
        searchEspressoButton.setTransformationMethod(null);
        searchEspressoButton.setText("Hledej\n\r" + "ESPRESSO\n\r" + "(" + searchRange + " m)");

        searchKafeButton = (Button) findViewById(R.id.searchKafeButton);
        searchKafeButton.setTransformationMethod(null);
        searchKafeButton.setText("Hledej\n\r" + "KAFE\n\r" + "(" + searchRange + " m)");

        locationImageView = (ImageView) findViewById(R.id.locationImageView);

        Drawable locBad = getResources().getDrawable(R.drawable.location_bad);
        locationImageView.setBackground(locBad);

        if (Utils.isOnline()) {
            new ReadStatsAsyncTask(this).execute();
        } else {
            showNoInternetToast();
        }
    }

    /**
     * Method to update color of location Accuracy indicator image according
     * current accuracy value.
     * <br>
     * If the accuracy is not known or too old, then it has default RED color
     * if the accuracy is known, but higher than GOOD_PRESNOST, then it has ORANGE (R.drawable.location_better) color
     * if the accuracy is known, and lower than GOOD_PRESNOST, then it has GREEN (R.drawable.location_good) color
     *
     * @param location
     */
    private void updateAccuracyIndicator(Location location) {

        if (location != null) {
            Drawable locIndic = getResources().getDrawable(R.drawable.location_better);

            if (location.getAccuracy() <= GOOD_PRESNOST) {
                locIndic = getResources().getDrawable(R.drawable.location_good);
            }
            locationImageView.setBackground(locIndic);
        }
    }

    private void zobrazPresnostPolohy(Location location) {
        if (location != null) {
            accuracy.setText("(přesnost: " + location.getAccuracy() + " m)");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                aktivujNastaveni();
                return true;
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_map:
                if (Utils.isOnline()) {
                    openMap();
                } else {
                    showNoInternetToast();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void aktivujNastaveni() {
        Intent selectSearchDistIntent = new Intent(this, SelectSearchDistanceActivity.class);
        selectSearchDistIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        selectSearchDistIntent.putExtra("searchRange", this.searchRange);
        this.startActivity(selectSearchDistIntent);
    }

    private void showAbout() {
        Intent i = new Intent(this, AboutActivity.class);
        this.startActivity(i);
    }

    private void setAccuracyTextColor(int barva) {
        accuracy.setTextColor(barva);
    }

    /**
     * To show statistics, can be called from AsyncTask
     *
     * @param stats
     */
    public void zobrazStatistiky(Statistics stats) {

        if (stats != null) {
            TextView sitesView = (TextView) findViewById(R.id.AllSitesTextView);
            TextView sites7View = (TextView) findViewById(R.id.AllSites7TextView);
            TextView sitesToday = (TextView) findViewById(R.id.TodaySitesTextView);
            TextView usersView = (TextView) findViewById(R.id.AllUsersTextView);

            sitesView.setText(stats.numOfSites);
            sitesToday.setText(stats.numOfSitesToday);
            sites7View.setText(stats.numOfSitesLastWeek);
            usersView.setText(stats.numOfUsers);
        }
    }

    private void openMap() {
        if (location != null) {
            Intent mapIntent = new Intent(this, MapsActivity.class);
            mapIntent.putExtra("currentLong", location.getLongitude());
            mapIntent.putExtra("currentLat", location.getLatitude());
            startActivity(mapIntent);
        }
    }

    public void onHledejEspressoClick(View view) {

        if (Utils.isOnline()) {
            new GetSitesInRangeAsyncTask(this,
                                         location.getLatitude(),
                                         location.getLongitude(),
                                         searchRange,
                                         "espresso").execute();
        } else {
            showNoInternetToast();
        }
    }

    public void onHledejKafeClick(View view) {

        if (Utils.isOnline()) {
            new GetSitesInRangeAsyncTask(this,
                    location.getLatitude(),
                    location.getLongitude(),
                    searchRange,
                    "").execute();
        } else {
            showNoInternetToast();
        }
    }

    /**
     * Show info Toast message, that internet connection is not available
     */
    private void showNoInternetToast() {
        Toast toast = Toast.makeText(getApplicationContext(),
                "No Internet connection.",
                Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Show info Toast message, that no CoffeeSite was found
     */
    public void showNothingFoundStatus(String subject) {

        int resource = ("espresso".equals(subject)) ? R.string.no_espresso_found : R.string.no_site_found;
        Toast toast = Toast.makeText(getApplicationContext(),
                                    resource,
                                    Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void requestPermission(String permissionType, int requestCode) {

        int permission = ContextCompat.checkSelfPermission(this, permissionType);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {permissionType}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Vyzaduje se pristup k poloze", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (locationService != null) {
            location = locationService.posledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
            updateAccuracyIndicator(location);
            zobrazPresnostPolohy(location);
        }
    }

    /**
     * Setup locationService listeners and RecyclerView which also
     * requires locationService to be activated/connected.
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();

        location = locationService.posledniPozice(LAST_PRESNOST, MAX_STARI_DAT);
        locationService.addPropertyChangeListener(this);

        zobrazPresnostPolohy(location);
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Kontrola opravneni
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
    }

    /**
     * Change of location detected, update all what should be updated, i.e.
     * accuracy indicator.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (bPrvni) { // prvni platna detekce polohy
            setAccuracyTextColor(barva);
            bPrvni = false;
            searchEspressoButton.setEnabled(true);
            searchKafeButton.setEnabled(true);
        }

        if (locationService != null)
        {
            location = locationService.getCurrentLocation();
            zobrazPresnostPolohy(location);
            updateAccuracyIndicator(location);

            if (!searchEspressoButton.isEnabled()) {
                searchEspressoButton.setEnabled(true);
                searchKafeButton.setEnabled(true);
            }
        }
    }

}
