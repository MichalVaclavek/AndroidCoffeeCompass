package cz.fungisoft.coffeecompass2.entity.repository.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteRecordStatus;

public class CoffeeSiteWithRecordStatus {

    @Embedded
    public CoffeeSite coffeeSite;

    @Relation(parentColumn = "id", entityColumn = "coffeeSiteId", entity = CoffeeSiteRecordStatus.class)
    public List<CoffeeSiteRecordStatus> coffeeSiteRecordStatusess;
}