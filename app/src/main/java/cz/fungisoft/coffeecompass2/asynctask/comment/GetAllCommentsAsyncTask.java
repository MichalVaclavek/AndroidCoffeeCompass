package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsLoadOperationListener;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * AsyncTask to call REST methods/interface to save or modify Comment and Stars for CoffeeSite
 * by loged-in user.
 */
public class GetAllCommentsAsyncTask {

    static final String REQ_TAG = "GetAllCommentsAsyncTask";

    private final WeakReference<CommentsLoadOperationListener> resultListener;

    public GetAllCommentsAsyncTask(CommentsLoadOperationListener resultListener) {
        this.resultListener = new WeakReference<>(resultListener);
    }

    public void execute() {
        Log.d(REQ_TAG, "GetAllCommentsAsyncTask REST request initiated");

        Retrofit retrofit = RetrofitClientProvider.getInstance().getRetrofit(CommentsAndStarsRESTInterface.GET_COMMENT_URL);

        CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

        Call<List<Comment>> call = api.getAllComments();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, "onResponse() success");
                        if (resultListener.get() != null) {
                            resultListener.get().onCommentsLoaded(response.body());
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for loading comments request.");
                        Result.Error error = new Result.Error(new IOException("Error loading comments. Response empty."));
                        if (resultListener.get() != null) {
                            resultListener.get().onRESTCallError(error);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (resultListener.get() != null) {
                            resultListener.get().onRESTCallError(new Result.Error(Utils.getRestError(errorBody)));
                        }
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error loading comments." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error loading comment.", e));
                        if (resultListener.get() != null) {
                            resultListener.get().onRESTCallError(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Log.e(REQ_TAG, "Error loading comments REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error loading comments.", t));
                if (resultListener.get() != null) {
                    resultListener.get().onRESTCallError(error);
                }
            }
        });

    }

}
