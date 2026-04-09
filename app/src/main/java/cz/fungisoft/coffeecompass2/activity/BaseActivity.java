package cz.fungisoft.coffeecompass2.activity;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import cz.fungisoft.coffeecompass2.R;

/**
 * Common parent for activities that keeps system bars outside of app content
 * and applies consistent colors across Android versions.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySystemBarsLayout();
    }

    private void applySystemBarsLayout() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        final @ColorInt int systemBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        getWindow().setStatusBarColor(systemBarColor);
        getWindow().setNavigationBarColor(systemBarColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        final WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            final boolean useDarkIcons = ColorUtils.calculateLuminance(systemBarColor) > 0.5d;
            insetsController.setAppearanceLightStatusBars(useDarkIcons);
            insetsController.setAppearanceLightNavigationBars(useDarkIcons);
        }
    }
}
