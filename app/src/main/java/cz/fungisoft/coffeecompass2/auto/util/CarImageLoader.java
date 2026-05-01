package cz.fungisoft.coffeecompass2.auto.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.CarContext;
import androidx.car.app.model.CarIcon;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.IconCompat;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;

public final class CarImageLoader {

    private static final String TAG = "CarImageLoader";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    // Car App Library recommends Pane side images up to 480x480 dp; using a square
    // keeps the host from awkwardly letterboxing/cropping the photo.
    private static final int MAIN_IMAGE_MAX_WIDTH_PX = 480;
    private static final int MAIN_IMAGE_MAX_HEIGHT_PX = 480;

    private CarImageLoader() {
    }

    public interface Callback {
        void onLoaded(@Nullable CarIcon icon);
    }

    public static void loadMainImage(@NonNull CarContext carContext,
                                     @NonNull CoffeeSite coffeeSite,
                                     @NonNull Callback callback) {
        final String url = coffeeSite.getMainImageURL();
        final boolean isOnline = Utils.isOnline(carContext.getApplicationContext());
        EXECUTOR.execute(() -> {
            CarIcon icon = null;
            try {
                Bitmap bmp;
                if (isOnline && url != null && !url.trim().isEmpty()) {
                    // URL can stay the same while the image changes; ensure fresh fetch.
                    Picasso.get().invalidate(url.trim());
                    bmp = Picasso.get()
                            .load(url.trim())
                            .resize(MAIN_IMAGE_MAX_WIDTH_PX, MAIN_IMAGE_MAX_HEIGHT_PX)
                            .centerCrop()
                            .onlyScaleDown()
                            .get();
                } else {
                    File imageFile = ImageUtil.getCoffeeSiteImageFile(carContext.getApplicationContext(), coffeeSite);
                    if (imageFile.exists() && imageFile.isFile()) {
                        // Same file path can be overwritten; invalidate file-based cache too.
                        Picasso.get().invalidate(Uri.fromFile(imageFile));
                        bmp = Picasso.get()
                                .load(imageFile)
                                .resize(MAIN_IMAGE_MAX_WIDTH_PX, MAIN_IMAGE_MAX_HEIGHT_PX)
                                .centerCrop()
                                .onlyScaleDown()
                                .get();
                    } else {
                        bmp = null;
                    }
                }
                if (bmp != null) {
                    File cacheDir = new File(carContext.getCacheDir(), "auto_images");
                    // noinspection ResultOfMethodCallIgnored
                    cacheDir.mkdirs();
                    File out = new File(cacheDir, "site_" + coffeeSite.getId() + ".png");
                    writePng(bmp, out);

                    Uri uri = FileProvider.getUriForFile(
                            carContext,
                            carContext.getString(R.string.file_provider),
                            out);

                    grantToHost(carContext, uri);
                    icon = new CarIcon.Builder(IconCompat.createWithContentUri(uri)).build();
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to load main image for site id=" + coffeeSite.getId() + " url=" + url, e);
            }

            CarIcon result = icon;
            carContext.getMainExecutor().execute(() -> callback.onLoaded(result));
        });
    }

    private static void writePng(@NonNull Bitmap bitmap, @NonNull File outFile) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
        } catch (Exception e) {
            Log.w(TAG, "Failed to write cached auto image to " + outFile.getAbsolutePath(), e);
        }
    }

    private static void grantToHost(@NonNull CarContext carContext, @NonNull Uri uri) {
        try {
            String hostPackage = carContext.getHostInfo() != null
                    ? carContext.getHostInfo().getPackageName()
                    : null;
            if (hostPackage != null && !hostPackage.trim().isEmpty()) {
                carContext.grantUriPermission(hostPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to grant URI permission to host for uri=" + uri, e);
            // no-op
        }
    }
}
