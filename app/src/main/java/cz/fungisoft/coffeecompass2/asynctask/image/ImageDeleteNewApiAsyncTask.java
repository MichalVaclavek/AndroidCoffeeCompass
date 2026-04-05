package cz.fungisoft.coffeecompass2.asynctask.image;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImageManageListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.ImagesApiSecuredRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Deletes a single image from the Images API.
 * <p>
 * Endpoint: {@code DELETE /object/{objectExtId}/image/{imageExtId}}
 */
public class ImageDeleteNewApiAsyncTask {

    private static final String TAG = "ImageDeleteNewApi";

    private final WeakReference<CoffeeSiteImageManageListener> listener;
    private final UserAccountActionsProvider userAccountService;
    private final String objectExtId;
    private final String imageExtId;

    public ImageDeleteNewApiAsyncTask(CoffeeSiteImageManageListener listener,
                                      UserAccountActionsProvider userAccountService,
                                      String objectExtId,
                                      String imageExtId) {
        this.listener = new WeakReference<>(listener);
        this.userAccountService = userAccountService;
        this.objectExtId = objectExtId;
        this.imageExtId = imageExtId;
    }

    public void execute() {
        if (userAccountService.getLoggedInUser() == null) {
            Log.e(TAG, "User not logged in.");
            if (listener.get() != null) {
                listener.get().onImageDeleteFailed(
                        new Result.Error(new IOException("User not logged in.")));
            }
            return;
        }

        Interceptor authInterceptor = chain -> {
            okhttp3.Request request = chain.request();
            Headers headers = request.headers().newBuilder()
                    .add("Authorization", userAccountService.getAccessTokenType()
                            + " " + userAccountService.getAccessToken())
                    .build();
            request = request.newBuilder().headers(headers).build();
            return chain.proceed(request);
        };

        OkHttpClient client = Utils.getOkHttpClientBuilder()
                .addInterceptor(authInterceptor)
                .authenticator(new TokenAuthenticator(userAccountService))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(ImagesApiSecuredRESTInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ImagesApiSecuredRESTInterface api = retrofit.create(ImagesApiSecuredRESTInterface.class);
        Call<Void> call = api.deleteImage(objectExtId, imageExtId);

        Log.i(TAG, "Deleting image objectExtId=" + objectExtId + " imageExtId=" + imageExtId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Delete success for imageExtId=" + imageExtId);
                    if (listener.get() != null) {
                        listener.get().onImageDeleted(imageExtId);
                    }
                } else {
                    String errorMsg = "Delete failed. HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, errorMsg);
                    if (listener.get() != null) {
                        listener.get().onImageDeleteFailed(
                                new Result.Error(new IOException(errorMsg)));
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Delete error: " + t.getMessage());
                if (listener.get() != null) {
                    listener.get().onImageDeleteFailed(
                            new Result.Error(new IOException("Error deleting image.", t)));
                }
                if (t.getMessage() != null && t.getMessage().startsWith("Refreshing access token failed")) {
                    userAccountService.clearLoggedInUser();
                    Utils.openLoginActivityOnRefreshTokenFailed((Context) userAccountService);
                }
            }
        });
    }
}
