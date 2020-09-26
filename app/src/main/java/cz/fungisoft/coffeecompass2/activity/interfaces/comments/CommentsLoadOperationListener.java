package cz.fungisoft.coffeecompass2.activity.interfaces.comments;


import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.Comment;

/**
 * Listener interface to be implemented if Activity or Service needs to know, that REST loading
 * of all Comments finished.
 */
public interface CommentsLoadOperationListener {
    
    default void onCommentsLoaded(List<Comment> comments) {}

    default void onCommentsForCoffeeSiteLoaded(List<Comment> comments, CoffeeSite coffeeSite) {}

    default void showRESTCallError(Result.Error error) {};
}
