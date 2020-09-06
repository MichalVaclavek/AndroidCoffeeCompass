package cz.fungisoft.coffeecompass2.entity.repository.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;

public class CoffeeSiteWithCsType {

    @Embedded
    public CoffeeSite coffeeSite;

    @Relation(parentColumn = "id", entityColumn = "coffeeSiteId", entity = CoffeeSiteType.class)
    public List<CoffeeSiteType> coffeeSiteTypes;
}
