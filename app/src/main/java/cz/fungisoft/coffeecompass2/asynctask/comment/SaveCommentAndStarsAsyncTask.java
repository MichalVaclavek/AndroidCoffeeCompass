package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentAndStars;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.entity.Comment;
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
public class SaveCommentAndStarsAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_TAG = "SaveCommentAsyncREST";

    private int coffeeSiteId;

    private final LoggedInUser user;


    private final WeakReference<CommentsListActivity> commentsActivity;

    private CommentAndStars commentAndStarsToSave;

    public SaveCommentAndStarsAsyncTask(int coffeeSiteId, LoggedInUser user, CommentsListActivity commentsActivity, CommentAndStars commentAndStarsToSave) {
        this.coffeeSiteId = coffeeSiteId;
        this.commentsActivity = new WeakReference<>(commentsActivity);
        this.commentAndStarsToSave = commentAndStarsToSave;
        this.user = user;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(REQ_TAG, "SaveCommentAndStarsAsyncTask REST request initiated");

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
                                         .create();

            Retrofit retrofit = new Retrofit.Builder()
                                            .client(client)
                                            .baseUrl(CommentsAndStarsRESTInterface.SAVE_COMMENT_URL)
                                            .addConverterFactory(ScalarsConverterFactory.create())
                                            .addConverterFactory(GsonConverterFactory.create(gson))
                                            .build();

            CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

            Call<List<Comment>> call = api.saveCommentAndStars(coffeeSiteId, commentAndStarsToSave);

            call.enqueue(new Callback<List<Comment>>() {
                @Override
                public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(REQ_TAG, "onResponse() success");
                            if (commentsActivity.get() != null) {
                                commentsActivity.get().processComments(response.body());
                            }
                        } else {
                            Log.i(REQ_TAG, "Returned empty response for saving comment request.");
                            Result.Error error = new Result.Error(new IOException("Error saving comment. Response empty."));
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
                            Log.e(REQ_TAG, "Error saving comment." + e.getMessage());
                            Result.Error error = new Result.Error(new IOException("Error saving comment.", e));
                            if (commentsActivity.get() != null) {
                                commentsActivity.get().showRESTCallError(error);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Comment>> call, Throwable t) {
                    Log.e(REQ_TAG, "Error saving comment REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error saving comment.", t));
                    if (commentsActivity.get() != null) {
                        commentsActivity.get().showRESTCallError(error);
                    }
                }
            });
        }
        return null;
    }

}
