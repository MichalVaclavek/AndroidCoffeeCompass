package cz.fungisoft.coffeecompass2.entity.repository.relations.crossreferenceentities;

import androidx.room.Entity;

/**
 * ManyToMany
 */
@Entity(primaryKeys = {"coffeeSiteId", "nextToMachineTypeId"})
public class CoffeeSiteNextToMachineTypeCrossRef {

    public long coffeeSiteId;
    public long nextToMachineTypeId;

}
