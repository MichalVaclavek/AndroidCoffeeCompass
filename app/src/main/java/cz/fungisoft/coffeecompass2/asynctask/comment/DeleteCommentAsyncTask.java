package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Calls REST for deleting comment of the CommentID
 */
public class DeleteCommentAsyncTask {

    static final String REQ_TAG = "DeleteCommentAsyncTask";

    private final String commentID;

    private final WeakReference<CommentsListActivity> commentsActivity;

    private final UserAccountActionsProvider userAccountService;

    public DeleteCommentAsyncTask(String commentID, UserAccountActionsProvider userAccountService, CommentsListActivity parentActivity) {
        this.userAccountService = userAccountService;
        this.commentID = commentID;
        this.commentsActivity = new WeakReference<>(parentActivity);
    }

    public void execute() {
        Log.d(REQ_TAG, "DeleteCommentAsyncTask REST request initiated");

        // Inserts user authorization token to Authorization header
        Interceptor headerAuthorizationInterceptor = chain -> {
            okhttp3.Request request = chain.request();
            Headers headers = request.headers().newBuilder().add("Authorization", userAccountService.getAccessTokenType() + " " + userAccountService.getAccessToken()).build();
            request = request.newBuilder().headers(headers).build();
            return chain.proceed(request);
        };

        Retrofit retrofit = RetrofitClientProvider.getInstance().getRetrofitWithAuth(CommentsAndStarsRESTInterface.DELETE_COMMENT_URL, headerAuthorizationInterceptor, new TokenAuthenticator(userAccountService));

        CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

        Call<Integer> call = api.deleteComment(commentID);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, "onResponse() success");
                        if (commentsActivity.get() != null) {
                            commentsActivity.get().processNumberOfComments(Integer.parseInt(response.body().toString()));
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for deleteUser comment request.");
                        Result.Error error = new Result.Error(new IOException("Error deleting comment. Response empty."));
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
                        Log.e(REQ_TAG, "Error deleting comment." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error deleting comment.", e));
                        if (commentsActivity.get() != null) {
                            commentsActivity.get().showRESTCallError(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(REQ_TAG, "Error deleting comment REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error deleting comment.", t));
                if (commentsActivity.get() != null) {
                    commentsActivity.get().showRESTCallError(error);
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
