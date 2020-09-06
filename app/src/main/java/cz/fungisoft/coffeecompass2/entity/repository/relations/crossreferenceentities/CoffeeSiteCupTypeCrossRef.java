package cz.fungisoft.coffeecompass2.entity.repository.relations.crossreferenceentities;

import androidx.room.Entity;

/**
 * ManyToMany
 */
@Entity(primaryKeys = {"coffeeSiteId", "cupTypeId"})
public class CoffeeSiteCupTypeCrossRef {

    public long coffeeSiteId;
    public long cupTypeId;

}
