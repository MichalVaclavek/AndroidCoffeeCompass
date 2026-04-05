package cz.fungisoft.coffeecompass2.activity.interfaces.comments;


import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.entity.Comment;

/**
 * Listener interface to be implemented, if Activity or Fragment needs to know, that REST call
 * to save user's rating of coffee site and or coffee site's comment, has finished with result.
 */
public interface UsersCSRatingAndCommentSaveOperationListener {

    /**
     * Method to be called from async task after the REST call saving comment for this CoffeeSite and User
     * is returned from server.
     *
     * @param stars
     */
    default void processSaveComments(List<Comment> comment) {}

    /**
     * Method to be called from Async task after failed request for the saving Comment or Stars rating
     * for this Coffee site is returned from server.
     */
    default void processFailedCommentSave(Result.Error error) {}
}
