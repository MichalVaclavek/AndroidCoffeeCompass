package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.UsersCSRatingAndCommentUpdateOperationListener;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * AsyncTask to call REST methods/interface to modify Stars rating for the CoffeeSite
 * by logged-in user.
 */
public class UpdateStarsAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_TAG = "UpdateCommentAsyncREST";

    private final LoggedInUser user;

    private final UsersCSRatingAndCommentUpdateOperationListener callingActivity;

    private final long coffeeSiteID;

    private final int numOfStars;

    public UpdateStarsAsyncTask(LoggedInUser user, UsersCSRatingAndCommentUpdateOperationListener callingActivity, long siteId, int stars) {
        this.callingActivity = callingActivity;
        this.user = user;
        this.coffeeSiteID = siteId;
        this.numOfStars = stars;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(REQ_TAG, "UpdateCommentAndStarsAsyncTask REST request initiated");

        if (user != null) {

            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", user.getLoginToken().getTokenType() + " " + user.getLoginToken().getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

            //Add the interceptor to the client builder.
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(headerAuthorizationInterceptor).build();

            Gson gson = new GsonBuilder().setDateFormat("dd.MM. yyyy HH:mm")
                                         .excludeFieldsWithoutExposeAnnotation()
                                         .create();

            Retrofit retrofit = new Retrofit.Builder()
                                            .client(client)
                                            .baseUrl(CommentsAndStarsRESTInterface.SAVE_COMMENT_URL)
                                            .addConverterFactory(ScalarsConverterFactory.create())
                                            .addConverterFactory(GsonConverterFactory.create(gson))
                                            .build();

            CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

            Call<Integer> call = api.updateStars(coffeeSiteID, user.getUserId(), numOfStars);

            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(REQ_TAG, "onResponse() success");
                            if (callingActivity != null) {
                                callingActivity.processUpdatedStarsRating(response.body());
                            }
                        } else {
                            Log.i(REQ_TAG, "Returned empty response for updating stars rating request.");
                            Result.Error error = new Result.Error(new IOException("Error updating stars rating. Response empty."));
                            if (callingActivity != null) {
                                callingActivity.processFailedCommentUpdate(error);
                            }
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            if (callingActivity != null) {
                                callingActivity.processFailedCommentUpdate(new Result.Error(Utils.getRestError(errorBody)));
                            }
                        } catch (IOException e) {
                            Log.e(REQ_TAG, "Error updating stars rating." + e.getMessage());
                            Result.Error error = new Result.Error(new IOException("Error updating stars rating.", e));
                            if (callingActivity != null) {
                                callingActivity.processFailedCommentUpdate(error);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e(REQ_TAG, "Error updating stars rating REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error updating stars rating.", t));
                    if (callingActivity != null) {
                        callingActivity.processFailedCommentUpdate(error);
                    }
                }
            });
        }

        return null;
    }

}
