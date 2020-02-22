package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.images.ImageRESTInterface;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteImageService;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ImageUploadAsyncTask extends AsyncTask<Void, Void, Void> {

    private final LoggedInUser currentUser;

    private File imageFile;

    private long coffeeSiteId;

    private CoffeeSiteImageService callingService;

    private String operationResult = "";
    private String operationError = "";

    private static final String TAG = "ImageUploadAsyncTask";


    public ImageUploadAsyncTask(CoffeeSiteImageService imageService, LoggedInUser currentUser, File imageFile, long coffeeSiteId) {
        this.callingService = imageService;
        this.currentUser = currentUser;
        this.imageFile = imageFile;
        this.coffeeSiteId = coffeeSiteId;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(TAG, "start");
        operationResult = "";
        operationError = "";

        Log.i(TAG, "currentUSer is null? " + String.valueOf(currentUser == null));
        if (currentUser != null && imageFile != null) {

            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", currentUser.getLoginToken().getTokenType() + " " + currentUser.getLoginToken().getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

            //HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            //logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            //Add the interceptor to the client builder.
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerAuthorizationInterceptor)
                    //.addInterceptor(logging)
                    .build();

            //Gson gson = new GsonBuilder().create();

            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(ImageRESTInterface.UPLOAD_IMAGE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    //.addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            // Create a request body with file and image media type
            RequestBody fileReqBody = RequestBody.create(imageFile, MediaType.parse("image/jpg"));

            // Create MultipartBody.Part using file request-body, file name and part name
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", imageFile.getName(), fileReqBody);

            ImageRESTInterface api = retrofit.create(ImageRESTInterface .class);

            Call<String> call = api.uploadImage(part, coffeeSiteId);

            Log.i(TAG, "start call");

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(TAG, "onSuccess()");
                            operationResult = "OK";
                            String imageURL = response.body();
                            callingService.evaluateImageSaveResult(new Result.Success<>(imageURL.trim()));
                        } else {
                            Log.i(TAG, "Returned empty response for uploading image request.");
                            Result.Error error = new Result.Error(new IOException("Error uploading image. Response empty."));
                            callingService.evaluateImageSaveResult(error);
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                            operationError = callingService.getResources().getString(R.string.coffeesiteservice_error_message_not_available);
                        }
                        Result.Error error = new Result.Error(new IOException(operationError));
                        callingService.evaluateImageSaveResult(error);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "Error uploading image REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error uploading image.", t));
                    //operationError = error.toString();
                    callingService.evaluateImageSaveResult(error);
                }
            });
        }
        return null;
    }

}
