package cz.fungisoft.coffeecompass2.entity.repository.dao.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.Comment;

public class CoffeeSiteWithComments {

    @Embedded
    public CoffeeSite coffeeSite;

    @Relation(
            parentColumn = "id",
            entityColumn = "coffeeSiteID"
    )
    public List<Comment> comments;

    public CoffeeSiteWithComments(CoffeeSite coffeeSite, List<Comment> comments) {
        this.coffeeSite = coffeeSite;
        this.comments = comments;
    }

}
