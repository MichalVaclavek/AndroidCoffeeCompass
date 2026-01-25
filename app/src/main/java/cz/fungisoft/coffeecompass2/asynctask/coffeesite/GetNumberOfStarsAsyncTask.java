package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.UsersCSRatingLoadOperationListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
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

    private final String userID;
    private final String coffeeSiteId;

    UsersCSRatingLoadOperationListener parentActivity;

    public GetNumberOfStarsAsyncTask(String userID, String coffeeSiteId, UsersCSRatingLoadOperationListener parentActivity) {
        this.userID = userID;
        this.coffeeSiteId = coffeeSiteId;
        this.parentActivity = parentActivity;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        Log.d(REQ_TAG, "GetNumberOfStarsAsyncTask REST request initiated");

        //Add the interceptor to the client builder.
        Retrofit retrofit = new Retrofit.Builder()
                .client(Utils.getOkHttpClientBuilder().build())
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
                        if (parentActivity != null) {
                            parentActivity.processNumberOfStarsForSiteAndUser((Integer.parseInt(response.body().toString())));
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for obtaining number of Stars for CoffeeSite and User request.");
                        Result.Error error = new Result.Error(new IOException("Error obtaining number of Stars for CoffeeSite and User. Response empty."));
                        if (parentActivity != null) {
                            parentActivity.processFailedNumberOfStarsForSiteAndUser(error);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (parentActivity != null) {
                            parentActivity.processFailedNumberOfStarsForSiteAndUser(new Result.Error(Utils.getRestError(errorBody)));
                        }
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error obtaining number of Stars for CoffeeSite and User." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error obtaining number of Stars for CoffeeSite and User.", e));
                        if (parentActivity != null) {
                            parentActivity.processFailedNumberOfStarsForSiteAndUser(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(REQ_TAG, "Error obtaining number of Stars for CoffeeSite and User REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error obtaining number of Stars for CoffeeSite and User", t));
                if (parentActivity != null) {
                    parentActivity.processFailedNumberOfStarsForSiteAndUser(error);
                }
            }
        });
        return null;
    }

}
