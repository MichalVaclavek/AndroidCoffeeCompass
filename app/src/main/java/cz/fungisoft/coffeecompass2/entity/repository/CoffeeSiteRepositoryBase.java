package cz.fungisoft.coffeecompass2.entity.repository;

public abstract class CoffeeSiteRepositoryBase {

    protected CoffeeSiteDatabase db;

    CoffeeSiteRepositoryBase(CoffeeSiteDatabase db) {
        this.db = db;
    }
}
