package cz.fungisoft.coffeecompass2.entity.repository.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;

public class CoffeeSiteWithLocationType {

    @Embedded
    public CoffeeSite coffeeSite;

    @Relation(parentColumn = "id", entityColumn = "coffeeSiteId", entity = SiteLocationType.class)
    public List<SiteLocationType> coffeeSiteLocationTypes;
}
