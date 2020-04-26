package cz.fungisoft.coffeecompass2.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;

/**
 * Activity to select geo location of CoffeeSite from maps.google.com by dragging a marker<br>
 * by user.
 * <p>
 * Usually called only from {@link CreateCoffeeSiteActivity} by startActivityForResult()<br>
 * method.
 */
public class SelectLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    /**
     * Current phone location to show
     */
    private LatLng selectedLocation;

    private MarkerOptions movableLocationMarker;

    private Toolbar mainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_to_select_location);

        mainToolbar = (Toolbar) findViewById(R.id.toolbar_select_map);
        setSupportActionBar(mainToolbar);

        // When started from CreateCoffeeSiteActivity
        selectedLocation = (LatLng) getIntent().getExtras().get("selectedLocation");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_select_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location_selected_ok:
                Intent createCoffeeSiteBackIntent = new Intent(this, CreateCoffeeSiteActivity.class);
                createCoffeeSiteBackIntent.putExtra("selectedLocation", selectedLocation);
                setResult(Activity.RESULT_OK, createCoffeeSiteBackIntent);
                finish();
                break;
            case R.id.action_location_selection_cancel:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * This callback is triggered when the map is ready to be used.
     * Shows the location passed from parent activity CoffeeSiteDetailActivity
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Marker for current location
        if (selectedLocation != null) {
            movableLocationMarker = new MarkerOptions().position(selectedLocation).title(getString(R.string.move_map_marker)).draggable(true);
            mMap.addMarker(movableLocationMarker);
        }

        GoogleMap.OnMarkerDragListener listener = new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                selectedLocation = marker.getPosition();
            }
        };

        mMap.setOnMarkerDragListener(listener);

        if (selectedLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 16));
        }
    }

}
