package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import cz.fungisoft.coffeecompass2.services.CoffeeSiteWithUserAccountService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteEntitiesLoadRESTResultListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.coffeesite.CoffeeSiteEntitiesRESTInterface;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteEntity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository.COFFEE_SITE_ENTITY_CLASSES;

public class ReadCoffeeSiteEntitiesAsyncTask extends AsyncTask<Void, Void, Void> {

    static final String REQ_ENTITIES_TAG = "GetCoffeeSiteEntities";

    private CoffeeSiteEntitiesRepository entitiesRepository;

    private String operationResult = "";
    private String operationError = "";

    private final CoffeeSiteEntitiesLoadRESTResultListener callingListenerService;

    private final CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode;

    private Result.Error error;

    private int entitiesCallCounter = 0;

    private synchronized void incrementEntitiesCallCounter() {
        entitiesCallCounter = entitiesCallCounter + 1;
    }
    private synchronized void resetEntitiesCallCounter() {
        entitiesCallCounter = 0;
    }
    private synchronized int getEntitiesCallCounter() {
        return entitiesCallCounter;
    }


    public ReadCoffeeSiteEntitiesAsyncTask(CoffeeSiteWithUserAccountService.CoffeeSiteRESTOper requestedRESTOperationCode,
                                           CoffeeSiteEntitiesLoadRESTResultListener callingListenerService,
                                           CoffeeSiteEntitiesRepository entitiesRepository) {
        this.entitiesRepository = entitiesRepository;
        this.callingListenerService = callingListenerService;
        this.requestedRESTOperationCode = requestedRESTOperationCode;
    }

    /** Starts Retrofit requestedOperation to load all instancies of
     * all CoffeeSiteEntity class and save them to CoffeeSiteEntitiesRepository
     */
    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(REQ_ENTITIES_TAG, "GetAllCoffeeSiteEntityValuesAsyncTask REST request initiated");

        Gson gson = new GsonBuilder().setLenient().create();

        //Add the interceptor to the client builder.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CoffeeSiteEntitiesRESTInterface.GET_ENTITY_BASE)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CoffeeSiteEntitiesRESTInterface api = retrofit.create(CoffeeSiteEntitiesRESTInterface.class);

        resetEntitiesCallCounter();
        for (Class<? extends CoffeeSiteEntity> entityClass : COFFEE_SITE_ENTITY_CLASSES) {
            readAndSaveEntitiesFromServer(entityClass, api);
        }

        return null;
    }

    /**
     *
     *  CoffeeSiteRecordStatus.class, CoffeeSiteStatus.class, CoffeeSiteType.class,
     *  CoffeeSort.class, CupType.class, NextToMachineType.class, OtherOffer.class, PriceRange.class,
     *  SiteLocationType.class, StarsQualityDescription.class};
     *
     * @param entityClass
     * @param api
     * @param <T>
     */
    private <T extends List<? extends CoffeeSiteEntity>> void readAndSaveEntitiesFromServer(Class<? extends CoffeeSiteEntity> entityClass,
                                                                                            CoffeeSiteEntitiesRESTInterface api) {
        operationResult = "";
        operationError = "";

        Call<T> call = null;
        //1. Get all CoffeeSiteStatus
        if (entityClass == CoffeeSiteStatus.class) {
            call = (Call<T>) api.getAllCoffeeSiteSiteStatuses();
        }
        //2. Get all CoffeeSiteRecordStatus
        if (entityClass == CoffeeSiteRecordStatus.class) {
            call = (Call<T>) api.getAllCoffeeSiteRecordStatuses();
        }
        //3. Get all CoffeeSiteType
        if (entityClass == CoffeeSiteType.class) {
            call = (Call<T>) api.getAllCoffeeSiteTypes();
        }
        //4. Get all CoffeeSort
        if (entityClass == CoffeeSort.class) {
            call = (Call<T>) api.getAllCoffeeSorts();
        }
        //5. Get all CupType
        if (entityClass == CupType.class) {
            call = (Call<T>) api.getAllCupTypes();
        }
        //6. Get all NextToMachineType
        if (entityClass == NextToMachineType.class) {
            call = (Call<T>) api.getAllNextToMachineTypes();
        }
        //7. Get all OtherOffer
        if (entityClass == OtherOffer.class) {
            call = (Call<T>) api.getAllOtherOffers();
        }
        //8. Get all SiteLocationType
        if (entityClass == SiteLocationType.class) {
            call = (Call<T>) api.getAllSiteLocationTypes();
        }
        //9. Get all StarsQualityDescription
        if (entityClass == StarsQualityDescription.class) {
            call = (Call<T>) api.getAllStarsQualityDescriptions();
        }
        //10. Get all PriceRange
        if (entityClass == PriceRange.class) {
            call = (Call<T>) api.getAllPriceRanges();
        }

        if (call != null) {
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    incrementEntitiesCallCounter();
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.i(REQ_ENTITIES_TAG, "onResponse() success");
                            entitiesRepository.setEntities(response.body());

                            if (getEntitiesCallCounter() == COFFEE_SITE_ENTITY_CLASSES.length) {
                                operationResult = "OK";
                                entitiesRepository.setDataReadedFromServer(true);
                                Result.Success<Boolean> result = new Result.Success<>(true);
                                if (callingListenerService != null) {
                                    callingListenerService.onCoffeeSiteEntitiesLoaded(result);
                                }
                            }
                        } else {
                            Log.i(REQ_ENTITIES_TAG, "Returned empty response retrieving info about CoffeeSite entities REST request.");
                            error = new Result.Error(new IOException("Error retrieving info about CoffeeSite entities REST request."));
                            operationError = "ERROR";
                            if (callingListenerService != null) {
                                callingListenerService.onCoffeeSiteEntitiesLoaded(error);
                            }
                        }
                    } else {
                        try {
                            error = new Result.Error(Utils.getRestError(response.errorBody().string()));
                        } catch (IOException e) {
                            Log.e(REQ_ENTITIES_TAG, e.getMessage());
                            operationError = "Chyba komunikace se serverem.";
                        }
                        if (error == null) {
                            error = new Result.Error(operationError);
                        }
                        if (getEntitiesCallCounter() == COFFEE_SITE_ENTITY_CLASSES.length
                            && callingListenerService != null) {
                            callingListenerService.onCoffeeSiteEntitiesLoaded(error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    incrementEntitiesCallCounter();
                    Log.e(REQ_ENTITIES_TAG, "Error retrieving info about CoffeeSite entities REST request." + t.getMessage());
                    error = new Result.Error(new IOException("Error retrieving info about CoffeeSite entities REST request.", t));
                    operationError = error.getDetail();
                    if (getEntitiesCallCounter() == COFFEE_SITE_ENTITY_CLASSES.length
                       && callingListenerService != null) {
                        callingListenerService.onCoffeeSiteEntitiesLoaded(error);
                    }
                }
            });
        }
    }

}
