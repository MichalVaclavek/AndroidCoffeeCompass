package cz.fungisoft.coffeecompass2.activity.interfaces.comments;


import cz.fungisoft.coffeecompass2.activity.data.Result;

/**
 * Listener interface to be implemented, if Activity or Fragment needs to know, that REST loading
 * user's rating of coffee site result is available.
 */
public interface UsersCSRatingLoadOperationListener {

    /**
     * Method to be called from async task after the number of stars for this CoffeeSite and User
     * is returned from server.
     *
     * @param stars
     */
    default void processNumberOfStarsForSiteAndUser(int stars) {}

    /**
     * Method to be called from Async task after failed request for the number<br>
     * of stars for this CoffeeSite and User is returned from server.<br>
     */
    default void processFailedNumberOfStarsForSiteAndUser(Result.Error error) {}
}
