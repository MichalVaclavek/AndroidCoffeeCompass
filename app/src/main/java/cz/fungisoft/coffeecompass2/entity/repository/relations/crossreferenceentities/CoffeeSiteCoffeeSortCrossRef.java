package cz.fungisoft.coffeecompass2.entity.repository.relations.crossreferenceentities;

import androidx.room.Entity;


/**
 * ManyToMany
 */
@Entity(primaryKeys = {"coffeeSiteId", "coffeeSortId"})
public class CoffeeSiteCoffeeSortCrossRef {

    public long coffeeSiteId;
    public long coffeeSortId;
}
