package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.utils.RetrofitClientProvider;
import cz.fungisoft.coffeecompass2.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Calls REST for obtaining number of comments already created for CoffeeSite.
 */
public class GetNumberOfCommentsAsyncTask {

    static final String REQ_TAG = "GetNumberOfCommentsAsyn";

    private final String coffeeSiteId;

    private final WeakReference<CoffeeSiteDetailActivity> coffeeSiteDetailActivity;


    public GetNumberOfCommentsAsyncTask(String coffeeSiteId, CoffeeSiteDetailActivity coffeeSiteDetailActivity) {
        this.coffeeSiteId = coffeeSiteId;
        this.coffeeSiteDetailActivity = new WeakReference<>(coffeeSiteDetailActivity);
    }

    public void execute() {

        Log.d(REQ_TAG, "GetNumberOfCommentsAsyncTask REST request initiated");

        Retrofit retrofit = RetrofitClientProvider.getInstance().getRetrofit(CommentsAndStarsRESTInterface.GET_COMMENT_URL);

        CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

        Call<Integer> call = api.getNumberOfComments(coffeeSiteId);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                       Log.i(REQ_TAG, "onResponse() success");
                       if (coffeeSiteDetailActivity.get() != null) {
                           coffeeSiteDetailActivity.get().processNumberOfComments(Integer.parseInt(response.body().toString()));
                       }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for obtaining number of Comments request.");
                        Result.Error error = new Result.Error(new IOException("Error obtaining number of Comments."));
                        if (coffeeSiteDetailActivity.get() != null) {
                            coffeeSiteDetailActivity.get().showRESTCallError(error);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (coffeeSiteDetailActivity.get() != null) {
                            coffeeSiteDetailActivity.get().showRESTCallError(new Result.Error(Utils.getRestError(errorBody)));
                        }
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error obtaining number of Comments." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error obtaining number of Comments.", e));
                        if (coffeeSiteDetailActivity.get() != null) {
                            coffeeSiteDetailActivity.get().showRESTCallError(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(REQ_TAG, "Error obtaining number of Comments REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error obtaining number of Comments", t));
                if (coffeeSiteDetailActivity.get() != null) {
                    coffeeSiteDetailActivity.get().showRESTCallError(error);
                }
            }
        });
    }

}
