package cz.fungisoft.coffeecompass2.entity.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.utils.AsyncRunner;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentsPageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsLoadOperationListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.CommentsPageLoadOperationListener;
import cz.fungisoft.coffeecompass2.asynctask.comment.GetAllCommentsAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.GetAllCommentsPaginatedAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.GetCommentsOfCoffeeSiteAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CommentDao;
import cz.fungisoft.coffeecompass2.entity.repository.dao.relations.CoffeeSiteWithComments;

/**
 * Comments repository. Reads Comments from DB or from server. Saves Comments to DB.<br>
 * Provides LiveData<Comment> objects to be used in data model.
 */
public class CommentRepository extends CoffeeSiteRepositoryBase implements CommentsLoadOperationListener,
                                                                           CommentsPageLoadOperationListener,
                                                                           CoffeeSiteDatabase.DbDeleteEndListener {

    /**
     * Inner help class to hold search parameters for comments
     */
    public static class CommentsLiveDataInput {

        public boolean isOfflineModeOn() {
            return offlineModeOn;
        }

        public CoffeeSite getCoffeeSite() {
            return coffeeSite;
        }

        private final boolean offlineModeOn;
        private final CoffeeSite coffeeSite;

        public CommentsLiveDataInput(boolean offlineModeOn, CoffeeSite coffeeSite) {
            this.offlineModeOn = offlineModeOn;
            this.coffeeSite = coffeeSite;
        }
    }

    /* ======== Start of fields and Constructors of CommentRepository ============ */

    /**
     * DAO object of the Room DB for the {@link Comment} class
     */
    private CommentDao commentDao;

    private static CommentRepository repository;

    private final LiveData<List<Comment>> mAllComments;

    private CommentRepository(CoffeeSiteDatabase db) {
        super(db);
        commentDao = db.commentDao();
        db.addDbDeleteEndListener(this);
        mAllComments = commentDao.getAllComments();
    }

    public static CommentRepository getInstance(CoffeeSiteDatabase db) {
        if (repository == null) {
            repository = new CommentRepository(db);
        }
        return repository;
    }

    public LiveData<List<Comment>> getAllComments() {
        return mAllComments;
    }


    /**
     * Deletes current Comments data from DB and loads and saves new ones
     */
    public void populateComments() {
        db.deleteCommentsAsync();
    }

    boolean loadAllByPages = false;

    int pageSize = 20;
    int requestedPage = 1;

    private CommentsPageLoadOperationListener commentsPageLoadResultListener;

    /**
     * Deletes current Comments data from DB and loads and saves new ones
     */
    public void populateCommentsByPages(CommentsPageLoadOperationListener resultListener, int pageSize) {
        this.loadAllByPages = true;
        this.pageSize = pageSize;
        this.commentsPageLoadResultListener = resultListener;
        db.deleteCommentsAsync();
    }


    /**
     * Starts Async task loading Comments from server, after the current Comments
     * are deleted from DB.
     */
    @Override
    public void onCommentsDeletedEnd() {
        if (!this.loadAllByPages) {
            new GetAllCommentsAsyncTask(this).execute();
        } else {
            requestedPage = 1;
            new GetAllCommentsPaginatedAsyncTask(this, requestedPage, this.pageSize).execute();
        }
    }

    @Override
    public void onCommentsLoaded(List<Comment> comments) {
        insertAll(comments);
    }

    /**
     * Called by AsyncTask
     *
     * @param comments
     */
    @Override
    public void onCommentsPageLoaded(CommentsPageEnvelope comments) {
        insertAll(comments.getContent());
        this.commentsPageLoadResultListener.onCommentsPageLoaded(comments);

        if (!comments.getLast()) {
            requestedPage++;
            new GetAllCommentsPaginatedAsyncTask(this, requestedPage, this.pageSize).execute();
        }
    }

    /**
     * MutableLiveData input to select either DB LiveData or LiveData created from server response.
     */
    private final MutableLiveData<CommentRepository.CommentsLiveDataInput> commentsInput = new MutableLiveData<>();

    private void setInput(boolean offlineModeOn, CoffeeSite coffeeSite) {
        commentsInput.setValue(new CommentRepository.CommentsLiveDataInput(offlineModeOn, coffeeSite));
    }

    /**
     * Comments for one CoffeeSite returned from server as MutableLiveData<>. Can be returned
     * as LiveData<> if app. is Online.
     *
     */
    private final MutableLiveData<List<CoffeeSiteWithComments>> commentsOfCoffeeSiteFromServer = new MutableLiveData<>();

    /**
     * Comments for one CoffeeSite as LiveData<>. Can be data from DB or data returned from server {@code commentsOfCoffeeSiteFromServer}
     */
    private final LiveData<List<CoffeeSiteWithComments>> commentsOfCoffeeSite =
            Transformations.switchMap(commentsInput, (cs) -> (cs.offlineModeOn) ? commentDao.getCoffeeSiteWithComments(cs.coffeeSite.getId())
                                                                                : commentsOfCoffeeSiteFromServer);

    /**
     * Starts loading Comments for CoffeeSite from server or gets data from DB
     *
     * @param coffeeSite - CoffeeSite for which the Comments are required
     * @param offlineModeOn - flag to indicate if Offline mode is Off or On. Leads to either AsyncTask requesting Comments from server or to invoke
     *                      LiveData setInput(true, coffeeSite), which leads to LiveData from DB change/request
     * @return
     */
    public LiveData<List<CoffeeSiteWithComments>> getCommentsForCoffeeSite(CoffeeSite coffeeSite, boolean offlineModeOn) {
        if (!offlineModeOn) {
            new GetCommentsOfCoffeeSiteAsyncTask(this, coffeeSite).execute();
        } else {
            setInput(true, coffeeSite);
        }
        return commentsOfCoffeeSite;
    }


    /**
     * Processes the list of Comments belonging to CoffeeSite as returned from server.<br>
     * Comments are not saved into DB here, but returned as LiveData<List<CoffeeSiteWithComments>> <br>
     * field {@code commentsOfCoffeeSiteFromServer}, followed by setInput(false, coffeeSite),<br>
     * which then leads to switch LiveData to a new source i.e {@code commentsOfCoffeeSiteFromServer}.
     * <p>
     * Note:
     * Comments read during this online REST call, cannot be inserted, because they can be present
     * already in DB because of previously activated OFFLINE mode.
     *
     * @param comments
     * @param coffeeSite
     */
    @Override
    public void onCommentsForCoffeeSiteLoaded(List<Comment> comments, CoffeeSite coffeeSite) {
        List<CoffeeSiteWithComments> listOfCommentsForCoffeeSite = new ArrayList<>();
        CoffeeSiteWithComments coffeeSiteWithComments = new CoffeeSiteWithComments(coffeeSite, comments);
        listOfCommentsForCoffeeSite.add(coffeeSiteWithComments);

        commentsOfCoffeeSiteFromServer.setValue(listOfCommentsForCoffeeSite);
        setInput(false, coffeeSite);
    }

    @Override
    public void onRESTCallError(Result.Error error) {
    }

    /**
     * Creates and calls AsyncTask execute() inserting Comments to DB.
     *
     * @param comment
     */
    public void insert (Comment comment) {
        new InsertCommentAsyncTask(commentDao).execute(comment);
    }

    /** Helper inner classes to create AsyncTasks for inserting Comments */

    private static class InsertCommentAsyncTask {

        private final CommentDao mAsyncTaskDao;

        InsertCommentAsyncTask(CommentDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(final Comment... params) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertComment(params[0]));
        }
    }

    public void insertAll (List<Comment> comments) {
        new InsertAllCommentsAsyncTask(commentDao).execute(comments);
    }


    private static class InsertAllCommentsAsyncTask {

        private final CommentDao mAsyncTaskDao;

        InsertAllCommentsAsyncTask(CommentDao dao) {
            mAsyncTaskDao = dao;
        }

        public void execute(List<Comment>... lists) {
            AsyncRunner.runInBackground(() -> mAsyncTaskDao.insertAllComments(lists[0]));
        }
    }

}
