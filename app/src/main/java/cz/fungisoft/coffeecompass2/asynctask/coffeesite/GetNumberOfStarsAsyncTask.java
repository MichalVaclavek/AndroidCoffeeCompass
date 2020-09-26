package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Runs async task for REST call to get rating for one site ID from one user
 */
public class GetNumberOfStarsAsyncTask extends AsyncTask<Void, Void, Integer> {

    static final String REQ_TAG = "GetNumberOfStarsAsyncT";

    private long userID;
    private int coffeeSiteId;

    private WeakReference<CommentsListActivity> parentActivity;

    public GetNumberOfStarsAsyncTask(long userID, int coffeeSiteId, CommentsListActivity parentActivity) {
        this.userID = userID;
        this.coffeeSiteId = coffeeSiteId;
        this.parentActivity = new WeakReference<>(parentActivity);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        Log.d(REQ_TAG, "GetNumberOfStarsAsyncTask REST request initiated");

        //Add the interceptor to the client builder.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CoffeeSiteRESTInterface.GET_NUMBER_OF_STARS_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        CoffeeSiteRESTInterface api = retrofit.create(CoffeeSiteRESTInterface.class);

        Call<Integer> call = api.getNumberOfStars(this.coffeeSiteId, this.userID);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, "onResponse()");
                        if (parentActivity.get() != null) {
                            parentActivity.get().processNumberOfStarsForSiteAndUser((Integer.parseInt(response.body().toString())));
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for obtaining number of Stars for CoffeeSite and User request.");
                        Result.Error error = new Result.Error(new IOException("Error obtaining number of Stars for CoffeeSite and User. Response empty."));
                        if (parentActivity.get() != null) {
                            parentActivity.get().processFailedNumberOfStarsForSiteAndUser(error);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (parentActivity.get() != null) {
                            parentActivity.get().showRESTCallError(new Result.Error(Utils.getRestError(errorBody)));
                        }
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error obtaining number of Stars for CoffeeSite and User." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error obtaining number of Stars for CoffeeSite and User.", e));
                        if (parentActivity.get() != null) {
                            parentActivity.get().processFailedNumberOfStarsForSiteAndUser(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(REQ_TAG, "Error obtaining number of Stars for CoffeeSite and User REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error obtaining number of Stars for CoffeeSite and User", t));
                if (parentActivity.get() != null) {
                    parentActivity.get().processFailedNumberOfStarsForSiteAndUser(error);
                }
            }
        });
        return null;
    }

}
