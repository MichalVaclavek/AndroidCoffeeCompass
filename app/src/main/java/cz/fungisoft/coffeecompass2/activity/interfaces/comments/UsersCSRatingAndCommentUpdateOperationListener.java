package cz.fungisoft.coffeecompass2.activity.interfaces.comments;


import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.entity.Comment;

/**
 * Listener interface to be implemented, if Activity or Fragment needs to know, that REST call
 * to update user's rating of coffee site and or coffee site's comment, has finished with result.
 */
public interface UsersCSRatingAndCommentUpdateOperationListener {

    /**
     * Method to be called from async task after the REST call updating comment for this CoffeeSite and User
     * is returned from server.
     *
     * @param stars
     */
    default void processUpdatedComment(Comment comment) {}

    /**
     * Method to be called from async task after the REST call updating the number of stars rating
     * for this CoffeeSite and User is returned from server.
     *
     * @param stars
     */
    default void processUpdatedStarsRating(Integer result) {}

    /**
     * Method to be called from Async task after failed request for the updating Comment or Stars rating
     * for this Coffee site is returned from server.
     */
    default void processFailedCommentUpdate(Result.Error error) {}
}
