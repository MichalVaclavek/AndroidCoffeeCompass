package cz.fungisoft.coffeecompass2.asynctask.image;

import android.content.Context;
import android.util.Log;

import java.io.File;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Uploads a new image to the Images API using the new multipart upload endpoint.
 * <p>
 * Endpoint: {@code POST /upload} with multipart form-data fields:
 * objectExtId, description, imageType, file.
 * <p>
 * Returns the external ID (UUID) of the newly created image.
 */
public class ImageUploadNewApiAsyncTask {

    private static final String TAG = "ImageUploadNewApi";

    private final WeakReference<CoffeeSiteImageManageListener> listener;
    private final UserAccountActionsProvider userAccountService;
    private final File imageFile;
    private final String objectExtId;

    public ImageUploadNewApiAsyncTask(CoffeeSiteImageManageListener listener,
                                      UserAccountActionsProvider userAccountService,
                                      File imageFile,
                                      String objectExtId) {
        this.listener = new WeakReference<>(listener);
        this.userAccountService = userAccountService;
        this.imageFile = imageFile;
        this.objectExtId = objectExtId;
    }

    public void execute() {
        if (userAccountService.getLoggedInUser() == null) {
            Log.e(TAG, "User not logged in.");
            if (listener.get() != null) {
                listener.get().onImageUploadFailed(
                        new Result.Error(new IOException("User not logged in.")));
            }
            return;
        }
        if (imageFile == null || !imageFile.exists()) {
            Log.e(TAG, "Image file does not exist.");
            if (listener.get() != null) {
                listener.get().onImageUploadFailed(
                        new Result.Error(new IOException("Image file does not exist.")));
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

//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = Utils.getOkHttpClientBuilder()
                .addInterceptor(authInterceptor)
                .authenticator(new TokenAuthenticator(userAccountService))
//                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(ImagesApiSecuredRESTInterface.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        // Build multipart parts
        RequestBody objectExtIdBody = RequestBody.create(objectExtId, MediaType.parse("text/plain"));
        RequestBody descriptionBody = RequestBody.create("Popis", MediaType.parse("text/plain"));
        RequestBody imageTypeBody = RequestBody.create("main", MediaType.parse("text/plain"));
        RequestBody typeBody = RequestBody.create("main", MediaType.parse("text/plain"));

        RequestBody fileBody = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), fileBody);

        ImagesApiSecuredRESTInterface api = retrofit.create(ImagesApiSecuredRESTInterface.class);
        Call<String> call = api.uploadImage(objectExtIdBody, descriptionBody, imageTypeBody, typeBody, filePart);

        Log.i(TAG, "Starting upload for objectExtId=" + objectExtId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageExtId = response.body().trim();
                    Log.i(TAG, "Upload success, imageExtId=" + imageExtId);
                    if (listener.get() != null) {
                        listener.get().onImageUploaded(imageExtId);
                    }
                } else {
                    String errorMsg = "Upload failed. HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, errorMsg);
                    if (listener.get() != null) {
                        listener.get().onImageUploadFailed(
                                new Result.Error(new IOException(errorMsg)));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Upload error: " + t.getMessage());
                if (listener.get() != null) {
                    listener.get().onImageUploadFailed(
                            new Result.Error(new IOException("Error uploading image.", t)));
                }
                if (t.getMessage() != null && t.getMessage().startsWith("Refreshing access token failed")) {
                    userAccountService.clearLoggedInUser();
                    Utils.openLoginActivityOnRefreshTokenFailed((Context) userAccountService);
                }
            }
        });
    }
}
