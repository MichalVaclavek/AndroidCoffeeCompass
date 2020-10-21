package cz.fungisoft.coffeecompass2.activity.interfaces.comments;

import java.util.List;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentAndStars;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentsPageEnvelope;
import cz.fungisoft.coffeecompass2.asynctask.comment.SaveCommentAndStarsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.Comment;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit interface for REST requests related to user's Comments and Rating(Stars)
 * entered to CoffeeSite.
 * Used by {@link SaveCommentAndStarsAsyncTask} and {@link cz.fungisoft.coffeecompass2.asynctask.comment.DeleteCommentAsyncTask}.
 */
public interface CommentsAndStarsRESTInterface {

    String SAVE_COMMENT_URL = BuildConfig.STARS_AND_COMMENTS_API_SECURED_URL;
    String GET_COMMENT_URL = BuildConfig.STARS_AND_COMMENTS_API_PUBLIC_URL;
    String DELETE_COMMENT_URL = BuildConfig.STARS_AND_COMMENTS_API_SECURED_URL;

    /**
     * REST call for obtaining all Comments from server. Used when activating OFFLINE mode.
     *
     * @return - list of all Comments saved on server.
     */
    @GET("comments/all")
    Call<List<Comment>> getAllComments();

    /**
     * REST call for obtaining all Comments paginated from server. Used when activating OFFLINE mode.
     * https://coffeecompass.cz/rest/public/starsAndComments/comments/allPaginated?orderBy=created&direction=desc
     *
     * @return
     */
    @GET("comments/allPaginated")
    Call<CommentsPageEnvelope> getAllCommentsPaginated(@Query("page") int page, @Query("size") int size);

    /**
     * Calls saving of Comment and Stars for CoffeeSiteID=siteID. Returns list of all
     * Comments for this CoffeeSite.
     * Requires Authorization header.
     *
     * @param siteID
     * @param commentAndStarsToSave
     * @return
     */
    @POST("saveStarsAndComment/{siteID}")
    Call<List<Comment>> saveCommentAndStars(@Path("siteID") int siteID, @Body CommentAndStars commentAndStarsToSave);

    /**
     * Calls REST updating of Comment and Stars for CoffeeSiteID=siteID. Returns updated Comment.
     * Requires Authorization header.
     *
     * @param commentAndStarsToUpdate
     * @return
     */
    @PUT("updateCommentAndStars")
    Call<Comment> updateCommentAndStars(@Body Comment commentAndStarsToUpdate);

    /**
     * REST call for obtaining number of Comments for the CoffeeSite with id=siteID
     * @param siteID
     * @return
     */
    @GET("comments/number/{siteID}")
    Call<Integer> getNumberOfComments(@Path("siteID") int siteID);

    /**
     * REST call for obtaining Comments for the CoffeeSite with id=siteID
     *
     * @param siteID
     * @return
     */
    @GET("comments/{siteID}")
    Call<List<Comment>> getCommentsForCoffeeSite(@Path("siteID") long siteID);

    /**
     * Deletes comment of commentID. Return commentID back or 0? if the delete request failed
     * Requires Authorization header.
     *
     * @param commentID
     * @return
     */
    @DELETE("deleteComment/{commentID}")
    Call<Integer> deleteComment(@Path("commentID") int commentID);
}
