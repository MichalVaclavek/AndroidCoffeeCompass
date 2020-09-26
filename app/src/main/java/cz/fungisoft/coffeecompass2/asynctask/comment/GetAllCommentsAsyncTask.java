package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsLoadOperationListener;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.utils.Utils;
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
public class GetAllCommentsAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_TAG = "GetAllCommentsAsyncTask";

    private final WeakReference<CommentsLoadOperationListener> resultListener;

    public GetAllCommentsAsyncTask(CommentsLoadOperationListener resultListener) {
        this.resultListener = new WeakReference<>(resultListener);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(REQ_TAG, "GetAllCommentsAsyncTask REST request initiated");

        OkHttpClient client = new OkHttpClient.Builder().build();

        Gson gson = new GsonBuilder().setDateFormat("dd.MM. yyyy HH:mm")
                                     .create();

        Retrofit retrofit = new Retrofit.Builder()
                                        .client(client)
                                        .baseUrl(CommentsAndStarsRESTInterface.GET_COMMENT_URL)
                                        .addConverterFactory(ScalarsConverterFactory.create())
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build();

        CommentsAndStarsRESTInterface api = retrofit.create(CommentsAndStarsRESTInterface.class);

        Call<List<Comment>> call = api.getAllComments();

        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i(REQ_TAG, "onResponse() success");
                        if (resultListener.get() != null) {
                            resultListener.get().onCommentsLoaded(response.body());
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for loading comment request.");
                        Result.Error error = new Result.Error(new IOException("Error loading comment. Response empty."));
                        if (resultListener.get() != null) {
                            resultListener.get().showRESTCallError(error);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (resultListener.get() != null) {
                            resultListener.get().showRESTCallError(new Result.Error(Utils.getRestError(errorBody)));
                        }
                    } catch (IOException e) {
                        Log.e(REQ_TAG, "Error loading comment." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error loading comment.", e));
                        if (resultListener.get() != null) {
                            resultListener.get().showRESTCallError(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Log.e(REQ_TAG, "Error loading comment REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error loading comment.", t));
                if (resultListener.get() != null) {
                    resultListener.get().showRESTCallError(error);
                }
            }
        });

        return null;
    }

}
