package cz.fungisoft.coffeecompass2;

import android.app.Application;
import android.util.Log;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

import cz.fungisoft.coffeecompass2.utils.Utils;

public class CoffeeCompassApp extends Application {

    private static final String TAG = "CoffeeCompassApp";

    @Override
    public void onCreate() {
        super.onCreate();
        initPicasso();
    }

    private void initPicasso() {
        OkHttpClient client = Utils.getOkHttpClientBuilder().build();
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(client))
                .listener((p, uri, exception) -> Log.e(TAG, "Picasso load failed for " + uri, exception))
                .build();

        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Picasso singleton already set", e);
        }
    }
}
