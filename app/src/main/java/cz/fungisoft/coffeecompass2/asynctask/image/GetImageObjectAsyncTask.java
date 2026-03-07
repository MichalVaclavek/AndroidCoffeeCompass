package cz.fungisoft.coffeecompass2.asynctask.image;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImageManageListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.ImagesApiRESTInterface;
import cz.fungisoft.coffeecompass2.entity.ImageObject;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Loads the {@link ImageObject} (with all {@link cz.fungisoft.coffeecompass2.entity.ImageFile}
 * entries) for the given object external ID from the Images API.
 * <p>
 * Endpoint: {@code GET /object/{objectExtId}}
 */
public class GetImageObjectAsyncTask {

    private static final String TAG = "GetImageObjectTask";

    private final WeakReference<CoffeeSiteImageManageListener> listener;
    private final String objectExtId;

    public GetImageObjectAsyncTask(CoffeeSiteImageManageListener listener, String objectExtId) {
        this.listener = new WeakReference<>(listener);
        this.objectExtId = objectExtId;
    }

    public void execute() {
        if (objectExtId == null || objectExtId.isEmpty()) {
            return;
        }

        Gson gson = new GsonBuilder().create();

        OkHttpClient client = Utils.getOkHttpClientBuilder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(ImagesApiRESTInterface.IMAGES_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ImagesApiTypedRESTInterface api = retrofit.create(ImagesApiTypedRESTInterface.class);
        Call<ImageObject> call = api.getImageObject(objectExtId);

        call.enqueue(new Callback<ImageObject>() {
            @Override
            public void onResponse(Call<ImageObject> call, Response<ImageObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (listener.get() != null) {
                        listener.get().onImageObjectLoaded(response.body());
                    }
                } else {
                    String errorMsg = "Failed to load image object. HTTP " + response.code();
                    Log.e(TAG, errorMsg);
                    if (listener.get() != null) {
                        listener.get().onImageObjectLoadFailed(
                                new Result.Error(new IOException(errorMsg)));
                    }
                }
            }

            @Override
            public void onFailure(Call<ImageObject> call, Throwable t) {
                Log.e(TAG, "Error loading image object: " + t.getMessage());
                if (listener.get() != null) {
                    listener.get().onImageObjectLoadFailed(
                            new Result.Error(new IOException("Error loading image object.", t)));
                }
            }
        });
    }

    /**
     * Internal typed Retrofit interface for getting ImageObject.
     * Uses Gson converter to deserialize JSON into {@link ImageObject}.
     */
    interface ImagesApiTypedRESTInterface {

        @retrofit2.http.GET("object/{objectExtId}")
        Call<ImageObject> getImageObject(@retrofit2.http.Path("objectExtId") String objectExtId);
    }
}
