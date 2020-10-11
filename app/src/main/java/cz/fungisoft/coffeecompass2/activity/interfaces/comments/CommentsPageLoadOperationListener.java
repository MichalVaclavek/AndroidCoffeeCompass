package cz.fungisoft.coffeecompass2.activity.interfaces.comments;


import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentsPageEnvelope;

/**
 * Listener interface to be implemented by Activity or Service, which needs to know, that REST loading
 * of Comments Page finished or failed.
 */
public interface CommentsPageLoadOperationListener {
    
    default void onCommentsPageLoaded(CommentsPageEnvelope comments) {}

    default void onRESTCallError(Result.Error error) {}
}
