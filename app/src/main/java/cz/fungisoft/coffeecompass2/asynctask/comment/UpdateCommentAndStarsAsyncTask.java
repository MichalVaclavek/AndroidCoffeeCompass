package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
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
 * AsyncTask to call REST methods/interface to save or modify Comment and Stars for CoffeeSite
 * by loged-in user.
 */
public class UpdateCommentAndStarsAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_TAG = "UpdateCommentAsyncREST";

    private final LoggedInUser user;

    private final WeakReference<CommentsListActivity> commentsActivity;

    private Comment commentAndStarsToUpdate;

    public UpdateCommentAndStarsAsyncTask(LoggedInUser user, CommentsListActivity commentsActivity, Comment commentAndStarsToUpdate) {
        this.commentsActivity = new WeakReference<>(commentsActivity);
        this.commentAndStarsToUpdate = commentAndStarsToUpdate;
        this.user = user;
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

            Call<Comment> call = api.updateCommentAndStars(commentAndStarsToUpdate);

            call.enqueue(new Callback<Comment>() {
                @Override
                public void onResponse(Call<Comment> call, Response<Comment> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(REQ_TAG, "onResponse() success");
                            if (commentsActivity.get() != null) {
                                commentsActivity.get().processUpdatedComment(response.body());
                            }
                        } else {
                            Log.i(REQ_TAG, "Returned empty response for updating comment request.");
                            Result.Error error = new Result.Error(new IOException("Error updating comment. Response empty."));
                            if (commentsActivity.get() != null) {
                                commentsActivity.get().showRESTCallError(error);
                            }
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            if (commentsActivity.get() != null) {
                                commentsActivity.get().showRESTCallError(new Result.Error(Utils.getRestError(errorBody)));
                            }
                        } catch (IOException e) {
                            Log.e(REQ_TAG, "Error updating comment." + e.getMessage());
                            Result.Error error = new Result.Error(new IOException("Error updating comment.", e));
                            if (commentsActivity.get() != null) {
                                commentsActivity.get().showRESTCallError(error);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Comment> call, Throwable t) {
                    Log.e(REQ_TAG, "Error updating comment REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error updating comment.", t));
                    if (commentsActivity.get() != null) {
                        commentsActivity.get().showRESTCallError(error);
                    }
                }
            });
        }
        //return retVal;
        return null;
    }

}
