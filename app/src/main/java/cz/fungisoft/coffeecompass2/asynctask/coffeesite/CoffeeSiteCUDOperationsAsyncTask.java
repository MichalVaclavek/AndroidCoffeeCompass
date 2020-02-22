package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * AsyncTasky pro Create, Update a Delete operace s CoffeeSit
 */
public class CoffeeSiteCUDOperationsAsyncTask extends AsyncTask<Void, Void, Void> {


    public enum SITE_ASYNC_REST_OPERATION {
        CREATE,
        UPDATE,
        DELETE
    }

    private final SITE_ASYNC_REST_OPERATION requestedOperation;

    private final CoffeeSite newOrModifiedCoffeeSite;

    /**
     * Current logged-in user
     */
    private final LoggedInUser currentUser;

    private final CoffeeSiteService callingService;

    private String operationResult = "";
    private String operationError = "";

    private final String tag;

    public CoffeeSiteCUDOperationsAsyncTask(SITE_ASYNC_REST_OPERATION operation, CoffeeSite coffeeSite, LoggedInUser currentUser, CoffeeSiteService callingService) {
        this.newOrModifiedCoffeeSite = coffeeSite;
        this.currentUser = currentUser;
        this.callingService = callingService;
        this.requestedOperation = operation;
        tag = "SiteOpAsynTask_" + this.requestedOperation.toString();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i(tag, "start");
        operationResult = "";
        operationError = "";

        Log.i(tag, "currentUSer is null? " + String.valueOf(currentUser == null));
        if (currentUser != null) {

            // Inserts user authorization token to Authorization header
            Interceptor headerAuthorizationInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request request = chain.request();
                    Headers headers = request.headers().newBuilder().add("Authorization", currentUser.getLoginToken().getTokenType() + " " + currentUser.getLoginToken().getAccessToken()).build();
                    request = request.newBuilder().headers(headers).build();
                    return chain.proceed(request);
                }
            };

            //HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            //logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            //Add the interceptor to the client builder.
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerAuthorizationInterceptor)
                    //.addInterceptor(logging)
                    .build();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat("dd. MM. yyyy HH:mm")
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(CoffeeSiteRESTInterface.COFFEE_SITE_SECURED_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            CoffeeSiteRESTInterface api = retrofit.create(CoffeeSiteRESTInterface.class);

            Call<CoffeeSite> call = null;

            switch (this.requestedOperation) {
                case CREATE:
                    call = api.createCoffeeSite(newOrModifiedCoffeeSite);
                    break;
                case UPDATE:
                    call = api.updateCoffeeSite(newOrModifiedCoffeeSite.getId(), newOrModifiedCoffeeSite);
                    break;
                case DELETE: // TODO to be implemented returning Long
                    //call = api.deleteCoffeeSite(newOrModifiedCoffeeSite.getId());
                    break;
            }

            Log.i(tag, "start call");

            call.enqueue(new Callback<CoffeeSite>() {
                @Override
                public void onResponse(Call<CoffeeSite> call, Response<CoffeeSite> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(tag, "onSuccess()");
                            operationResult = "OK";
                            CoffeeSite coffeeSite = response.body();
                            callingService.sendCoffeeSiteCUDOperationResultToClient(coffeeSite, requestedOperation, operationResult, "");
                        } else {
                            Log.i(tag, "Returned empty response for saving CoffeeSite request.");
                            Result.Error error = new Result.Error(new IOException("Error saving CoffeeSite. Response empty."));
                            operationError = error.toString();
                            callingService.sendCoffeeSiteCUDOperationResultToClient(null, requestedOperation,"", operationError);
                        }
                    } else {
                        try {
                            operationError = Utils.getRestError(response.errorBody().string()).getDetail();
                        } catch (IOException e) {
                            Log.e(tag, e.getMessage());
                            operationError = callingService.getResources().getString(R.string.coffeesiteservice_error_message_not_available);
                        }
                        callingService.sendCoffeeSiteCUDOperationResultToClient(null, requestedOperation,"", operationError);
                    }
                }

                @Override
                public void onFailure(Call<CoffeeSite> call, Throwable t) {
                    Log.e(tag, "Error saving CoffeeSite REST request." + t.getMessage());
                    Result.Error error = new Result.Error(new IOException("Error saving CoffeeSite.", t));
                    operationError = error.toString();
                    callingService.sendCoffeeSiteCUDOperationResultToClient(null, requestedOperation,"", operationError);
                }
            });
        }
        return null;
    }
}
