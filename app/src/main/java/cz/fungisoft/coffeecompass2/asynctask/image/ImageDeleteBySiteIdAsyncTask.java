package cz.fungisoft.coffeecompass2.asynctask.image;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImageManageListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.ImageRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;

import okhttp3.Headers;
import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Deletes a legacy single CoffeeSite image by CoffeeSite id.
 */
public class ImageDeleteBySiteIdAsyncTask {

    private static final String TAG = "ImageDeleteBySiteId";

    private final WeakReference<CoffeeSiteImageManageListener> listener;
    private final UserAccountActionsProvider userAccountService;
    private final CoffeeSite coffeeSite;

    public ImageDeleteBySiteIdAsyncTask(CoffeeSiteImageManageListener listener,
                                        UserAccountActionsProvider userAccountService,
                                        CoffeeSite coffeeSite) {
        this.listener = new WeakReference<>(listener);
        this.userAccountService = userAccountService;
        this.coffeeSite = coffeeSite;
    }

    public void execute() {
        if (userAccountService.getLoggedInUser() == null) {
            Log.e(TAG, "User not logged in.");
            notifyFailure(new IOException("User not logged in."));
            return;
        }
        if (coffeeSite == null || coffeeSite.getId().isEmpty()) {
            notifyFailure(new IOException("CoffeeSite id is empty."));
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

        Retrofit retrofit = RetrofitClientProvider.getInstance()
                .getRetrofitWithAuth(ImageRESTInterface.DELETE_IMAGE_URL, authInterceptor,
                        new TokenAuthenticator(userAccountService));

        ImageRESTInterface api = retrofit.create(ImageRESTInterface.class);
        Call<String> call = api.deleteImageBySiteId(coffeeSite.getId());

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    CoffeeSiteImageManageListener currentListener = listener.get();
                    if (currentListener != null) {
                        currentListener.onImageDeleted(response.body() != null
                                ? response.body().toString()
                                : coffeeSite.getId());
                    }
                    return;
                }

                String errorMsg = "Delete failed. HTTP " + response.code();
                try {
                    if (response.errorBody() != null) {
                        errorMsg = response.errorBody().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading error body", e);
                }
                notifyFailure(new IOException(errorMsg));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Delete error: " + t.getMessage());
                notifyFailure(new IOException("Error deleting image.", t));
                if (t.getMessage() != null && t.getMessage().startsWith("Refreshing access token failed")) {
                    userAccountService.clearLoggedInUser();
                    Utils.openLoginActivityOnRefreshTokenFailed((Context) userAccountService);
                }
            }
        });
    }

    private void notifyFailure(IOException exception) {
        CoffeeSiteImageManageListener currentListener = listener.get();
        if (currentListener != null) {
            currentListener.onImageDeleteFailed(new Result.Error(exception));
        }
    }
}
