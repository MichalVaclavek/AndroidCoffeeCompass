package cz.fungisoft.coffeecompass2.entity.repository;

/**
 * Base class for all coffeeSite sub-entities Repositories with 'id' field required for saving into DB.
 */
public abstract class CoffeeSiteRepositoryBase {

    protected CoffeeSiteDatabase db;

    CoffeeSiteRepositoryBase(CoffeeSiteDatabase db) {
        this.db = db;
    }
}
