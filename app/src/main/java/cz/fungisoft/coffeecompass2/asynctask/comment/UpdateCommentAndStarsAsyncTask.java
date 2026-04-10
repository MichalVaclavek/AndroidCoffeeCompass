package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.UsersCSRatingAndCommentUpdateOperationListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.TokenAuthenticator;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * AsyncTask to call REST methods/interface to save or modify Comment and Stars for CoffeeSite
 * by loged-in user.
 */
public class UpdateCommentAndStarsAsyncTask {

    static final String REQ_TAG = "UpdateCommentAsyncREST";

    private final UserAccountActionsProvider userAccountService;

    private final UsersCSRatingAndCommentUpdateOperationListener callingActivity;

    private final Comment commentAndStarsToUpdate;

    public UpdateCommentAndStarsAsyncTask(UserAccountActionsProvider userAccountService, UsersCSRatingAndCommentUpdateOperationListener callingActivity, Comment commentAndStarsToUpdate) {
        this.callingActivity = callingActivity;
        this.commentAndStarsToUpdate = commentAndStarsToUpdate;
        this.userAccountService = userAccountService;
    }

    public void execute() {
        Log.d(REQ_TAG, "UpdateCommentAndStarsAsyncTask REST request initiated");

        if (userAccountService.getLoggedInUser() != null) {
            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = chain -> {
                okhttp3.Request request = chain.request();
                Headers headers = request.headers().newBuilder().add("Authorization", userAccountService.getAccessTokenType() + " " + userAccountService.getAccessToken()).build();
                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            };

            Retrofit retrofit = RetrofitClientProvider.getInstance().getRetrofitWithAuth(CommentsAndStarsRESTInterface.SAVE_COMMENT_URL, headerAuthorizationInterceptor, new TokenAuthenticator(userAccountService));

            CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

            Call<Comment> call = api.updateCommentAndStars(commentAndStarsToUpdate);

            call.enqueue(new Callback<Comment>() {
                @Override
                public void onResponse(Call<Comment> call, Response<Comment> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(REQ_TAG, "onResponse() success");
                            if (callingActivity != null) {
                                callingActivity.processUpdatedComment(response.body());
                            }
                        } else {
                            Log.i(REQ_TAG, "Returned empty response for updating comment request.");
                            Result.Error error = new Result.Error(new IOException("Error updating comment. Response empty."));
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
                            Log.e(REQ_TAG, "Error updating comment." + e.getMessage());
                            Result.Error error = new Result.Error(new IOException("Error updating comment.", e));
                            if (callingActivity != null) {
                                callingActivity.processFailedCommentUpdate(error);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Comment> call, Throwable t) {
                    Log.e(REQ_TAG, "Error updating comment REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error updating comment.", t));
                    if (callingActivity != null) {
                        callingActivity.processFailedCommentUpdate(error);
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
