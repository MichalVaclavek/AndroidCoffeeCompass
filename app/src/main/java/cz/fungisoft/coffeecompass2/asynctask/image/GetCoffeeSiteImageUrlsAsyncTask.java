package cz.fungisoft.coffeecompass2.asynctask.image;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImagesLoadListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImagesRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.ImageRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Async task for loading all image URLs for a CoffeeSite.
 */
public class GetCoffeeSiteImageUrlsAsyncTask {

    private static final String REQ_TAG = "GetCSImageUrls";

    private final WeakReference<CoffeeSiteImagesLoadListener> resultListener;
    private final CoffeeSite coffeeSite;

    public GetCoffeeSiteImageUrlsAsyncTask(CoffeeSiteImagesLoadListener resultListener, CoffeeSite coffeeSite) {
        this.resultListener = new WeakReference<>(resultListener);
        this.coffeeSite = coffeeSite;
    }

    public void execute() {
        if (coffeeSite == null) {
            return;
        }

        Gson gson = new GsonBuilder().setDateFormat("dd.MM. yyyy HH:mm")
                .create();

        OkHttpClient client = Utils.getOkHttpClientBuilder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(CoffeeSiteImagesRESTInterface.COFFEESITE_API_PUBLIC_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CoffeeSiteImagesRESTInterface api = retrofit.create(CoffeeSiteImagesRESTInterface.class);
        Call<List<String>> call = api.getAllImageUrls(coffeeSite.getId());

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    List<String> body = response.body();
                    if (body != null) {
                        if (resultListener.get() != null) {
                            resultListener.get().onImageUrlsLoaded(body);
                        }
                    } else {
                        Result.Error error = new Result.Error(new IOException("Image URLs response empty."));
                        if (resultListener.get() != null) {
                            resultListener.get().onImageUrlsLoadFailed(error);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        if (resultListener.get() != null) {
                            resultListener.get().onImageUrlsLoadFailed(new Result.Error(Utils.getRestError(errorBody)));
                        }
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error loading image URLs. " + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error loading image URLs.", e));
                        if (resultListener.get() != null) {
                            resultListener.get().onImageUrlsLoadFailed(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e(REQ_TAG, "Error loading image URLs." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error loading image URLs.", t));
                if (resultListener.get() != null) {
                    resultListener.get().onImageUrlsLoadFailed(error);
                }
            }
        });
    }
}
