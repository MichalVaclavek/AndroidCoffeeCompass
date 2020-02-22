package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.comments.CommentsAndStarsRESTInterface;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Calls REST for deleting comment of the CommentID
 */
public class DeleteCommentAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_TAG = "DeleteCommentAsyncTask";

    private final LoggedInUser user;

    private int commentID;

    private CommentsListActivity commentsActivity;

    public DeleteCommentAsyncTask(int commentID, LoggedInUser user, CommentsListActivity parentActivity) {
        this.user = user;
        this.commentID = commentID;
        this.commentsActivity = parentActivity;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(REQ_TAG, "DeleteCommentAsyncTask REST request initiated");

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

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(CommentsAndStarsRESTInterface.DELETE_COMMENT_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

        Call<Integer> call = api.deleteComment(commentID);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, "onResponse() success");
                        commentsActivity.processNumberOfComments(Integer.parseInt(response.body().toString()));
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for delete comment request.");
                        Result.Error error = new Result.Error(new IOException("Error deleting comment. Response empty."));
                        commentsActivity.showRESTCallError(error);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        commentsActivity.showRESTCallError(new Result.Error(Utils.getRestError(errorBody)));
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error deleting comment." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error deleting comment.", e));
                        commentsActivity.showRESTCallError(error);
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(REQ_TAG, "Error deleting comment REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error deleting comment.", t));
                commentsActivity.showRESTCallError(error);
            }
        });
        return null;
    }
}
