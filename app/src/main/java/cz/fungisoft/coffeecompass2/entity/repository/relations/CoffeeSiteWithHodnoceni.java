package cz.fungisoft.coffeecompass2.entity.repository.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfRatings;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;

public class CoffeeSiteWithHodnoceni {

    @Embedded
    public CoffeeSite coffeeSite;

    @Relation(parentColumn = "id", entityColumn = "coffeeSiteId", entity = AverageStarsWithNumOfRatings.class)
    public List<AverageStarsWithNumOfRatings> cofeeSiteHodnoceni;

}
