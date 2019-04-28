package cz.fungisoft.coffeecompass2.activity;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteListContent;

/**
 * Activity to show CoffeeSite's location on the map
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    /**
     * Current phone location to show
     */
    private LatLng currentLoc;

    /**
     * One CoffeeSites to show
     */
    private CoffeeSite site;

    /**
     * List of found CoffeeSites to show
     */
    private CoffeeSiteListContent content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_fragment);

        currentLoc = (LatLng) getIntent().getExtras().get("currentLocation");
        content = (CoffeeSiteListContent) getIntent().getSerializableExtra("listContent");
        site = (CoffeeSite) getIntent().getSerializableExtra("site");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * This callback is triggered when the map is ready to be used.
     *
     * Shows the current phone location as passed from parent activity
     * and
     * one CoffeeSite if passed from CoffeeSiteDetailActivity
     * or
     * list of CoffeeSites if passed from CoffeeSiteListActivity
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // Marker for current location
        if (currentLoc != null) {
            mMap.addMarker(new MarkerOptions().position(currentLoc).title(getString(R.string.currentPosition)));
            builder.include(currentLoc);
        }

        // Markers for the list of CoffeeSites
        if (content != null) {
            for (final CoffeeSite cs : content.getItems()) {
                addMarkerWithInfoListener(cs, mMap, builder);
            }
        }

        // Marker for one CoffeeSite
        if (site != null) {
            addMarkerWithInfoListener(site, mMap, builder);
        }

        LatLngBounds bounds = builder.build();

        if ((site == null) && (content == null) && (currentLoc != null)) { // only current position to be shown
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 10));
        } else { // more then one marker/position to be shown
            // Copied from github where similar problem was discussed. Sometimes mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 99)) was throwing exception
            // because of problems with layout dimension, it was detected to be 0 size.
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
        }
    }

    private void addMarkerWithInfoListener(final CoffeeSite cs, GoogleMap map, LatLngBounds.Builder builder) {

        LatLng siteLoc = new LatLng(cs.getLatitude(), cs.getLongitude());

        GoogleMap.OnInfoWindowClickListener listener = new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent detailsIntent = new Intent(MapsActivity.this, CoffeeSiteDetailActivity.class);

                CoffeeSite cs = (CoffeeSite) marker.getTag();
                detailsIntent.putExtra("coffeeSite", cs);
                startActivity(detailsIntent);
            }
        };

        map.setOnInfoWindowClickListener(listener);

        Marker marker = map.addMarker(new MarkerOptions()
                .position(siteLoc)
                .title(cs.getName())
                .snippet(cs.getTypPodniku()) // .snippet(cs.getTypPodniku()).snippet(cs.getHodnoceni())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.coffee_bean)));

        marker.setTag(cs);

        builder.include(siteLoc);
    }

}
