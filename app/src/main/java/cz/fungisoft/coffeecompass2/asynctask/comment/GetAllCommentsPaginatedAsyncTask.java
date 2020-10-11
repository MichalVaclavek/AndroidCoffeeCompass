package cz.fungisoft.coffeecompass2.asynctask.comment;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentsPageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsAndStarsRESTInterface;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsPageLoadOperationListener;
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
public class GetAllCommentsPaginatedAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_TAG = "GetAllCommentsPageAsnT";

    private final WeakReference<CommentsPageLoadOperationListener> resultListener;

    private final int requestedPage;

    private final int pageSize;

    public GetAllCommentsPaginatedAsyncTask(CommentsPageLoadOperationListener resultListener,
                                            int requestedPage,
                                            int pageSize) {
        this.resultListener = new WeakReference<>(resultListener);
        this.requestedPage = requestedPage;
        this.pageSize = pageSize;
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

        Call<CommentsPageEnvelope> call = api.getAllCommentsPaginated(requestedPage, pageSize);

        call.enqueue(new Callback<CommentsPageEnvelope>() {
            @Override
            public void onResponse(Call<CommentsPageEnvelope> call, Response<CommentsPageEnvelope> response) {
                if (response.isSuccessful()) {
                    int responseCode = response.code();
                    if (responseCode == 504) {return;}

                    if (response.body() != null) {
                        Log.i(REQ_TAG, "onResponse() success");

                        CommentsPageEnvelope commentsPage = response.body();
                        if (resultListener.get() != null) {
                            resultListener.get().onCommentsPageLoaded(commentsPage);
                        }
                    } else {
                        Log.i(REQ_TAG, "Returned empty response for loading comments page request.");
                        Result.Error error = new Result.Error(new IOException("Error loading comments page. Response empty."));
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
                        Log.e(REQ_TAG, "Error loading comments page." + e.getMessage());
                        Result.Error error = new Result.Error(new IOException("Error loading comments page.", e));
                        if (resultListener.get() != null) {
                            resultListener.get().onRESTCallError(error);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<CommentsPageEnvelope> call, Throwable t) {
                Log.e(REQ_TAG, "Error loading comment REST request." + t.getMessage());
                Result.Error error = new Result.Error(new IOException("Error loading comment.", t));
                if (resultListener.get() != null) {
                    resultListener.get().onRESTCallError(error);
                }
            }
        });

        return null;
    }

}
