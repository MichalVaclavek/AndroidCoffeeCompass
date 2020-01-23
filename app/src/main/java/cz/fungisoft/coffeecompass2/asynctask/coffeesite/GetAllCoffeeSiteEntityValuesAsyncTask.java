package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.os.AsyncTask;

import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository;

/**
 * Async task to be started by CoffeeSiteService to read all available values for
 * CoffeeSiteEntities which are then saved into CoffeeSiteEntitiesRepository
 */
public class GetAllCoffeeSiteEntityValuesAsyncTask extends AsyncTask<Void, Void, Integer> {

    /**
     * Repository to save CoffeeSiteEntity values read from server.
     */
    private CoffeeSiteEntitiesRepository enetitiesRepository;

    public GetAllCoffeeSiteEntityValuesAsyncTask(CoffeeSiteEntitiesRepository enetitiesRepository) {
        this.enetitiesRepository = enetitiesRepository;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        return null;
    }
}
