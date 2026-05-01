package cz.fungisoft.coffeecompass2.auto;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.Screen;
import androidx.car.app.Session;

import cz.fungisoft.coffeecompass2.auto.screen.FoundCoffeeSitesCarScreen;

/**
 * Android Auto session for CoffeeCompass.
 */
public final class CoffeeCompassCarSession extends Session {

    @NonNull
    @Override
    public Screen onCreateScreen(@Nullable Intent intent) {
        return new FoundCoffeeSitesCarScreen(getCarContext());
    }
}
