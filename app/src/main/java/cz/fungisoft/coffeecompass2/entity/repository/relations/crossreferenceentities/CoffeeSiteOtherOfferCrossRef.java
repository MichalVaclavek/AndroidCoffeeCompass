package cz.fungisoft.coffeecompass2.entity.repository.relations.crossreferenceentities;

import androidx.room.Entity;

/**
 * ManyToMany
 */
@Entity(primaryKeys = {"coffeeSiteId", "otherOfferId"})
public class CoffeeSiteOtherOfferCrossRef {

    public long coffeeSiteId;
    public long otherOfferId;

}
