package cz.fungisoft.coffeecompass2.auto.screen;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.CarLocation;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.Metadata;
import androidx.car.app.model.Place;
import androidx.car.app.model.PlaceListMapTemplate;
import androidx.car.app.model.PlaceMarker;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.core.graphics.drawable.IconCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesFoundService;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateServiceConnector;
import cz.fungisoft.coffeecompass2.services.LocationService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesFoundListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeSearchOperationListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Default Android Auto screen: list of CoffeeSites in 10 km radius.
 */
public final class FoundCoffeeSitesCarScreen extends Screen
        implements CoffeeSitesInRangeServiceConnectionListener,
                   CoffeeSitesInRangeSearchOperationListener,
                   CoffeeSitesFoundListener {

    private static final String TAG = "CarFoundSites";

    private static final int DEFAULT_RANGE_METERS = 5_000;
    // PlaceListMapTemplate has a hard content limit of 6 items.
    private static final int MAX_ITEMS = 6;

    @Nullable
    private CoffeeSitesFoundService foundSitesService;
    @Nullable
    private CoffeeSitesInRangeUpdateServiceConnector serviceConnector;
    private boolean shouldUnbind;

    private boolean loading = true;
    @Nullable
    private String lastError;

    @Nullable
    private LocationService locationService;

    private final Set<CoffeeSiteMovable> distanceTrackedSites = new HashSet<>();

    @Nullable
    private CarIcon cupMarkerIcon;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long lastInvalidateAtMs;
    private boolean invalidateScheduled;
    private static final long INVALIDATE_THROTTLE_MS = 1500;

    private final PropertyChangeListener distanceChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt == null || evt.getPropertyName() == null) {
                return;
            }
            if (!"distance".equals(evt.getPropertyName())) {
                return;
            }
            Collections.sort(currentSites);
            throttleInvalidate();
        }
    };

    @NonNull
    private final List<CoffeeSiteMovable> currentSites = new ArrayList<>();

    public FoundCoffeeSitesCarScreen(@NonNull CarContext carContext) {
        super(carContext);

        getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                bindFoundSitesService();
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                unbindFoundSitesService();
            }
        });
    }

    private void bindFoundSitesService() {
        if (serviceConnector != null) {
            return;
        }

        serviceConnector = new CoffeeSitesInRangeUpdateServiceConnector(this);
        boolean ok = getCarContext().bindService(
                new Intent(getCarContext(), CoffeeSitesFoundService.class),
                serviceConnector,
                Context.BIND_AUTO_CREATE);
        shouldUnbind = ok;
        if (!ok) {
            lastError = getCarContext().getString(R.string.coffeesiteservice_error_message_not_available);
            Log.e(TAG, "Failed to bind CoffeeSitesFoundService");
            invalidate();
        }
    }

    private void unbindFoundSitesService() {
        if (!shouldUnbind || serviceConnector == null) {
            serviceConnector = null;
            foundSitesService = null;
            return;
        }

        if (foundSitesService != null) {
            foundSitesService.removeFoundSitesSearchOperationListener(this);
            foundSitesService.removeSitesFoundListener(this);
        }

        detachDistanceUpdates();

        getCarContext().unbindService(serviceConnector);
        shouldUnbind = false;
        serviceConnector = null;
        foundSitesService = null;
    }

    @Override
    public void onCoffeeSitesInRangeUpdateServiceConnected() {
        if (serviceConnector == null) {
            return;
        }
        foundSitesService = serviceConnector.getSitesInRangeUpdateService();
        if (foundSitesService == null) {
            lastError = getCarContext().getString(R.string.coffeesiteservice_error_message_not_available);
            invalidate();
            return;
        }

        foundSitesService.addFoundSitesSearchOperationListener(this);
        foundSitesService.addSitesFoundListener(this);
        foundSitesService.setCurrentSearchRange(DEFAULT_RANGE_METERS);

        locationService = foundSitesService.getLocationService();

        // Observe DB-backed list (works for both online/offline mode).
        foundSitesService.getFoundSites().observe(this, sites -> {
            currentSites.clear();
            if (sites != null) {
                currentSites.addAll(sites);
                Collections.sort(currentSites);
            }

            // Only invalidate when we have real content to show. In the empty case, rely on
            // onSearchingSitesFinished/onSearchingSitesError to switch out of the loading state.
            if (!currentSites.isEmpty()) {
                attachDistanceUpdates(currentSites);
                loading = false;
                invalidate();
            }
        });

        // Kick off initial search.
        List<Integer> ranges = Collections.singletonList(DEFAULT_RANGE_METERS);
        foundSitesService.requestUpdatesOfCurrentSitesInRange(null, DEFAULT_RANGE_METERS, ranges, "");
    }

    @Override
    public void onSitesInRangeFound(List<CoffeeSiteMovable> coffeeSiteMovables) {
        currentSites.clear();
        if (coffeeSiteMovables != null) {
            currentSites.addAll(coffeeSiteMovables);
            Collections.sort(currentSites);
        }

        attachDistanceUpdates(currentSites);

        loading = false;
        lastError = null;
        invalidate();
    }

    private void attachDistanceUpdates(@NonNull List<CoffeeSiteMovable> sites) {
        detachDistanceUpdates();

        if (locationService == null) {
            return;
        }

        for (CoffeeSiteMovable site : sites) {
            if (site == null) {
                continue;
            }
            site.setLocationService(locationService);
            locationService.addPropertyChangeListener(site);
            site.addPropertyChangeListener(distanceChangeListener);
            distanceTrackedSites.add(site);
        }
    }

    private void detachDistanceUpdates() {
        if (locationService == null) {
            distanceTrackedSites.clear();
            return;
        }
        for (CoffeeSiteMovable site : distanceTrackedSites) {
            try {
                site.removePropertyChangeListener(distanceChangeListener);
                locationService.removePropertyChangeListener(site);
            } catch (Exception ignored) {
                // no-op
            }
        }
        distanceTrackedSites.clear();
    }

    private void throttleInvalidate() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastInvalidateAtMs;
        if (elapsed >= INVALIDATE_THROTTLE_MS) {
            lastInvalidateAtMs = now;
            invalidate();
            return;
        }
        if (invalidateScheduled) {
            return;
        }
        invalidateScheduled = true;
        long delay = INVALIDATE_THROTTLE_MS - elapsed;
        mainHandler.postDelayed(() -> {
            invalidateScheduled = false;
            lastInvalidateAtMs = System.currentTimeMillis();
            invalidate();
        }, delay);
    }

    @Override
    public void onStartSearchingSites() {
        loading = true;
        lastError = null;
    }

    @Override
    public void onSearchingSitesFinished(int numOfSitesInRanges) {
        loading = false;

        // If no results arrived via LiveData, switch from loading to an empty/error row.
        if (currentSites.isEmpty()) {
            invalidate();
        }
    }

    @Override
    public void onSearchingSitesError(String error) {
        loading = false;
        lastError = (error != null && !error.trim().isEmpty())
                ? error
                : getCarContext().getString(R.string.car_error_generic);
        invalidate();
    }

    @Nullable
    private CarIcon getCupMarkerIcon() {
        if (cupMarkerIcon != null) {
            return cupMarkerIcon;
        }
        try {
            cupMarkerIcon = new CarIcon.Builder(
                    IconCompat.createWithResource(getCarContext(), R.drawable.ic_coffee_cup_main))
                    .build();
        } catch (Exception e) {
            Log.w(TAG, "Failed to build marker icon, falling back to label", e);
            cupMarkerIcon = null;
        }
        return cupMarkerIcon;
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        PlaceListMapTemplate.Builder template = new PlaceListMapTemplate.Builder()
                .setTitle(getCarContext().getString(R.string.car_found_sites_title))
                .setHeaderAction(Action.APP_ICON)
                .setCurrentLocationEnabled(true);

        if (loading) {
            return template.setLoading(true).build();
        }

        ItemList.Builder items = new ItemList.Builder();
        int count = Math.min(currentSites.size(), MAX_ITEMS);

        // Anchor tells the host to fit the map so the anchor + all markers are visible.
        // Use the centroid of the closest few sites (plus current location if known) so a
        // distant outlier among the 6 displayed doesn't pull the zoom too far out.
        if (count > 0) {
            int anchorSites = Math.min(count, 3);
            double sumLat = 0;
            double sumLon = 0;
            int anchorPoints = 0;
            for (int i = 0; i < anchorSites; i++) {
                CoffeeSiteMovable s = currentSites.get(i);
                sumLat += s.getLatitude();
                sumLon += s.getLongitude();
                anchorPoints++;
            }
            Location here = (locationService != null) ? locationService.getCurrentLocation() : null;
            if (here != null) {
                sumLat += here.getLatitude();
                sumLon += here.getLongitude();
                anchorPoints++;
            }
            CarLocation anchorLoc = CarLocation.create(sumLat / anchorPoints, sumLon / anchorPoints);
            template.setAnchor(new Place.Builder(anchorLoc).build());
        }

        if (!currentSites.isEmpty()) {
            for (int i = 0; i < count; i++) {
                final CoffeeSiteMovable site = currentSites.get(i);
                String distance = Utils.getDistanceInBetterReadableForm(site.getDistance());
                String line1 = site.getTypPodniku() + ", " + site.getTypLokality();

                PlaceMarker.Builder markerBuilder = new PlaceMarker.Builder()
                        .setColor(CarColor.YELLOW);
                CarIcon icon = getCupMarkerIcon();
                if (icon != null) {
                    markerBuilder.setIcon(icon, PlaceMarker.TYPE_ICON);
                } else {
                    markerBuilder.setLabel(String.valueOf(i + 1));
                }

                Place place = new Place.Builder(CarLocation.create(
                                site.getLatitude(), site.getLongitude()))
                        .setMarker(markerBuilder.build())
                        .build();

                Row row = new Row.Builder()
                        .setTitle(site.getName())
                        .addText(line1)
                        .addText(distance)
                        .setMetadata(new Metadata.Builder().setPlace(place).build())
                        .setBrowsable(true)
                        .setOnClickListener(() -> getScreenManager().push(
                                new CoffeeSiteDetailCarScreen(getCarContext(), site)))
                        .build();
                items.addItem(row);
            }
        } else {
            String noSites = getCarContext().getString(R.string.car_no_sites);
            if (lastError != null && !lastError.trim().isEmpty()) {
                noSites = lastError;
            }
            items.setNoItemsMessage(noSites);
        }

        return template.setItemList(items.build()).build();
    }
}
