package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.login.CommentsAndStarsRESTInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Calls REST for obtaining number of comments already created for CoffeeSite.
 */
public class GetNumberOfCommentsAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_TAG = "GetNumberOfCommentsAsyn";

    private int coffeeSiteId;

    private final CoffeeSiteDetailActivity coffeeSiteDetailActivity;


    public GetNumberOfCommentsAsyncTask(int coffeeSiteId, CoffeeSiteDetailActivity coffeeSiteDetailActivity) {
        this.coffeeSiteId = coffeeSiteId;
        this.coffeeSiteDetailActivity = coffeeSiteDetailActivity;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Log.d(REQ_TAG, "GetNumberOfCommentsAsyncTask REST request initiated");

        //Add the interceptor to the client builder.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CommentsAndStarsRESTInterface.GET_COMMENT_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

        Call<Integer> call = api.getNumberOfComments(coffeeSiteId);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        //Log.i("onSuccess", response.body());
                        coffeeSiteDetailActivity.processNumberOfComments(Integer.parseInt(response.body().toString()));
                    } else {
                        Log.i("onEmptyResponse", "Returned empty response for obtaining number of Comments request.");
                        Result.Error error = new Result.Error(new IOException("Error obtaining number of Comments."));
                        coffeeSiteDetailActivity.showRESTCallError(error);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        coffeeSiteDetailActivity.showRESTCallError(new Result.Error(Utils.getRestError(errorBody)));
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error obtaining number of Comments." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error obtaining number of Comments.", e));
                        coffeeSiteDetailActivity.showRESTCallError(error);
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(REQ_TAG, "Error obtaining number of Comments REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error obtaining number of Comments", t));
                coffeeSiteDetailActivity.showRESTCallError(error);
            }
        });
        return null;
    }
}
