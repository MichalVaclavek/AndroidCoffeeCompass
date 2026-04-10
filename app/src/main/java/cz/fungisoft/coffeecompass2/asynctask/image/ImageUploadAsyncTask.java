package cz.fungisoft.coffeecompass2.asynctask.image;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.ImageRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteImageService;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ImageUploadAsyncTask {

    private final UserAccountActionsProvider userAccountService;

    private final File imageFile;

    private final CoffeeSite coffeeSite;
    private final String coffeeSiteId;

    private final WeakReference<CoffeeSiteImageService> callingService;

    private String operationError = "";

    private static final String TAG = "ImageUploadAsyncTask";


    public ImageUploadAsyncTask(CoffeeSiteImageService imageService, UserAccountActionsProvider userAccountService, File imageFile, CoffeeSite coffeeSite) {
        this.callingService = new WeakReference<>(imageService);
        this.userAccountService = userAccountService;
        if (!imageFile.exists()) {
            throw new IllegalArgumentException();
        }
        this.imageFile = imageFile;
        this.coffeeSite = coffeeSite;
        this.coffeeSiteId = coffeeSite.getId();
    }


    public void execute() {
        Log.i(TAG, "start");
        operationError = "";

        Log.i(TAG, "currentUSer is null? " + (userAccountService.getLoggedInUser() == null));
        if (userAccountService.getLoggedInUser() != null && imageFile != null) {

            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", userAccountService.getAccessTokenType() + " " + userAccountService.getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

            Retrofit retrofit = RetrofitClientProvider.getInstance()
                    .getRetrofitWithAuth(ImageRESTInterface.UPLOAD_IMAGE_URL, headerAuthorizationInterceptor, new TokenAuthenticator(userAccountService));

            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(imageFile, MediaType.parse("image/jpg"));

            // Create MultipartBody.Part using file request-body, file name and part name
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", imageFile.getName(), fileReqBody);

            ImageRESTInterface api = retrofit.create(ImageRESTInterface .class);

            Call<String> call = api.uploadImage(part, coffeeSiteId);

            Log.i(TAG, "start call");

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            String imageURL = response.body();
                            if (callingService.get() != null) {
                                callingService.get().evaluateImageSaveResult(coffeeSite, new Result.Success<>(imageURL.trim()));
                            }
                        } else {
                            Log.i(TAG, "Returned empty response for uploading image request.");
                            Result.Error error = new Result.Error(new IOException("Error uploading image. Response empty."));
                            if (callingService.get() != null) {
                                callingService.get().evaluateImageSaveResult(coffeeSite, error);
                            }
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                            if (callingService.get() != null) {
                                operationError = callingService.get().getResources().getString(R.string.coffeesiteservice_error_message_not_available);
                            }
                        }
                        Result.Error error = new Result.Error(new IOException(operationError));
                        if (callingService.get() != null) {
                            callingService.get().evaluateImageSaveResult(coffeeSite, error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "Error uploading image REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error uploading image.", t));
                    //operationError = error.toString();
                    if (callingService.get() != null) {
                        callingService.get().evaluateImageSaveResult(coffeeSite, error);
                    }
                    if (t.getMessage().startsWith("Refreshing access token failed")) {
                        userAccountService.clearLoggedInUser();
                        // go to login activity
                        Utils.openLoginActivityOnRefreshTokenFailed((Context) userAccountService);
                    }
                }
            });
        }
    }

}
