package cz.fungisoft.coffeecompass2.auto.screen;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.Pane;
import androidx.car.app.model.PaneTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.auto.util.CarImageLoader;
import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfRatings;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesFoundService;
import cz.fungisoft.coffeecompass2.services.LocationService;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Android Auto detail screen for selected CoffeeSite.
 */
public final class CoffeeSiteDetailCarScreen extends Screen {

    private final CoffeeSiteMovable coffeeSite;

    @Nullable
    private CarIcon mainImage;
    private boolean mainImageRequested;

    @Nullable
    private LocationService locationService;
    private boolean shouldUnbind;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long lastInvalidateAtMs;
    private boolean invalidateScheduled;
    private static final long INVALIDATE_THROTTLE_MS = 1500;

    private final PropertyChangeListener distanceChangeListener = evt -> {
        if (evt == null || evt.getPropertyName() == null) {
            return;
        }
        if (!"distance".equals(evt.getPropertyName())) {
            return;
        }
        throttleInvalidate();
    };

    private final ServiceConnection foundSitesServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CoffeeSitesFoundService foundSitesService = ((CoffeeSitesFoundService.LocalBinder) service).getService();
            if (foundSitesService == null) {
                return;
            }
            locationService = foundSitesService.getLocationService();
            attachDistanceUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
        }
    };

    public CoffeeSiteDetailCarScreen(@NonNull CarContext carContext, @NonNull CoffeeSiteMovable coffeeSite) {
        super(carContext);
        this.coffeeSite = coffeeSite;

        getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                bindFoundSitesService();
                requestMainImageIfNeeded();
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                unbindFoundSitesService();
            }
        });
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        Pane.Builder pane = new Pane.Builder();

        String distance = Utils.getDistanceInBetterReadableForm(coffeeSite.getDistance());
        String typeLine = coffeeSite.getTypPodniku() + ", " + coffeeSite.getTypLokality();

        AverageStarsWithNumOfRatings rating = coffeeSite.getHodnoceni();
        String ratingText = (rating != null && rating.getNumOfHodnoceni() > 0)
                ? getCarContext().getString(R.string.car_label_rating) + ": " + rating
                : null;

        String opening = buildOpening(coffeeSite.getOteviraciDobaDny(), coffeeSite.getOteviraciDobaHod());
        String openingText = !opening.isEmpty()
                ? getCarContext().getString(R.string.car_label_opening_hours) + ": " + opening
                : null;

        String sorts = coffeeSite.getCoffeeSortsOneString();
        String sortsText = (sorts != null && !sorts.trim().isEmpty()) ? sorts.trim() : null;

        // Pane.setImage() is the "big side image" API (API level 4+). The call does not
        // fail when the host won't render it (e.g. DHU silently drops it), so we cannot
        // detect support at runtime — always keep a small Row icon as a visible fallback.
        if (mainImage != null) {
            try {
                if (getCarContext().getCarAppApiLevel() >= 4) {
                    pane.setImage(mainImage);
                }
            } catch (Exception ignored) {
                // no-op
            }
        }

        // Pack everything into 2 rows so a host that does render the Pane image is happy
        // (Pane image is only shown when the pane has at most 2 rows on most hosts).
        Row.Builder topRow = new Row.Builder().setTitle(distance);
        topRow.addText(typeLine);
        if (mainImage != null) {
            topRow.setImage(mainImage, Row.IMAGE_TYPE_LARGE);
        }
        pane.addRow(topRow.build());

        if (openingText != null || sortsText != null) {
            String secondTitle = openingText != null ? openingText : sortsText;
            Row.Builder secondRow = new Row.Builder().setTitle(secondTitle);
            if (openingText != null && ratingText != null) {
                secondRow.addText(ratingText);
            }
            if (openingText != null && sortsText != null) {
                secondRow.addText(sortsText);
            }
            pane.addRow(secondRow.build());
        }

        Action navigateAction = new Action.Builder()
                .setTitle(getCarContext().getString(R.string.car_action_navigate))
                .setOnClickListener(this::startNavigation)
                .build();

        ActionStrip actionStrip = new ActionStrip.Builder()
                .addAction(navigateAction)
                .build();

        return new PaneTemplate.Builder(pane.build())
                .setTitle(coffeeSite.getName())
                .setHeaderAction(Action.BACK)
                .setActionStrip(actionStrip)
                .build();
    }

    private void startNavigation() {
        String dest = coffeeSite.getLatitude() + "," + coffeeSite.getLongitude();
        Uri geoUri = Uri.parse("geo:" + dest);

        Intent carNavIntent = new Intent(CarContext.ACTION_NAVIGATE, geoUri);
        try {
            getCarContext().startCarApp(carNavIntent);
            return;
        } catch (Exception ex) {
            // Host refused (e.g. no default nav app on car). Fall through to phone-side Maps.
        }

        Uri phoneNavUri = Uri.parse("google.navigation:q=" + dest);
        Intent phoneIntent = new Intent(Intent.ACTION_VIEW, phoneNavUri);
        phoneIntent.setPackage("com.google.android.apps.maps");
        phoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            getCarContext().startActivity(phoneIntent);
        } catch (ActivityNotFoundException ex) {
            Intent fallback = new Intent(Intent.ACTION_VIEW, phoneNavUri);
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getCarContext().startActivity(fallback);
        }
    }

    private void bindFoundSitesService() {
        shouldUnbind = getCarContext().bindService(
                new Intent(getCarContext(), CoffeeSitesFoundService.class),
                foundSitesServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void unbindFoundSitesService() {
        detachDistanceUpdates();
        if (!shouldUnbind) {
            return;
        }
        try {
            getCarContext().unbindService(foundSitesServiceConnection);
        } catch (Exception ignored) {
            // no-op
        }
        shouldUnbind = false;
        locationService = null;
    }

    private void attachDistanceUpdates() {
        if (locationService == null) {
            return;
        }
        coffeeSite.setLocationService(locationService);
        locationService.addPropertyChangeListener(coffeeSite);
        coffeeSite.addPropertyChangeListener(distanceChangeListener);
    }

    private void detachDistanceUpdates() {
        if (locationService == null) {
            return;
        }
        try {
            coffeeSite.removePropertyChangeListener(distanceChangeListener);
            locationService.removePropertyChangeListener(coffeeSite);
        } catch (Exception ignored) {
            // no-op
        }
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

    private void requestMainImageIfNeeded() {
        if (mainImageRequested) {
            return;
        }
        String url = coffeeSite.getMainImageURL();
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        mainImageRequested = true;
        CarImageLoader.loadMainImage(getCarContext(), coffeeSite, icon -> {
            if (icon != null) {
                mainImage = icon;
                invalidate();
            }
        });
    }

    @NonNull
    private static String buildOpening(@Nullable String days, @Nullable String hours) {
        String d = (days != null) ? days.trim() : "";
        String h = (hours != null) ? hours.trim() : "";
        if (d.isEmpty()) {
            return h;
        }
        if (h.isEmpty()) {
            return d;
        }
        return d + ": " + h;
    }
}
