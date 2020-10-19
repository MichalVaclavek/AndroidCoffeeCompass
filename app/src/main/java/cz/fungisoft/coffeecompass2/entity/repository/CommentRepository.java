package cz.fungisoft.coffeecompass2.entity.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

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
     * DAO object of the Room DB for the {@link Comment} class
     */
    private CommentDao commentDao;

    /**
     * LiveData input holder for coffeeSite for whom the Comments are to be returned
     */
    private final MutableLiveData<CoffeeSite> commentsInput = new MutableLiveData<>();

    private LiveData<List<Comment>> mAllComments;

    private LiveData<List<CoffeeSiteWithComments>> commentsOfCoffeeSite =
            Transformations.switchMap(commentsInput, (cs) -> commentDao.getCoffeeSiteWithComments(cs.getId()));


    private void setInput(CoffeeSite coffeeSite) {
        commentsInput.setValue(coffeeSite);
    }

    private static CommentRepository repository;

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

    public LiveData<List<Comment>> getAllComments() {
        return mAllComments;
    }

    public LiveData<List<CoffeeSiteWithComments>> getCommentsForCoffeeSite(CoffeeSite coffeeSite, boolean offlineModeOn) {
        if (!offlineModeOn) {
            new GetCommentsOfCoffeeSiteAsyncTask(this, coffeeSite).execute();
        }
        setInput(coffeeSite);
        return commentsOfCoffeeSite;
    }

    public void insert (Comment comment) {
        new InsertCommentAsyncTask(commentDao).execute(comment);
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

    private final MutableLiveData<List<CoffeeSiteWithComments>> commentsOfCoffeeSiteFromServer = new MutableLiveData<>();

    /**
     * Processes the list of Comments belonging to CoffeeSite as returned from server.
     * Comments are not saved into DB here, but returned as LiveData<List<CoffeeSiteWithComments>>> field.
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
        commentsOfCoffeeSite = commentsOfCoffeeSiteFromServer;
    }

    @Override
    public void onRESTCallError(Result.Error error) {

    }

    /** Helper inner classes to create AsyncTasks for inserting Comments */

    private static class InsertCommentAsyncTask extends AsyncTask<Comment, Void, Void> {

        private CommentDao mAsyncTaskDao;

        InsertCommentAsyncTask(CommentDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Comment... params) {
            mAsyncTaskDao.insertComment(params[0]);
            return null;
        }
    }

    public void insertAll (List<Comment> comments) {
        new InsertAllCommentsAsyncTask(commentDao).execute(comments);
    }


    private static class InsertAllCommentsAsyncTask extends AsyncTask<List<Comment>, Void, Void> {

        private CommentDao mAsyncTaskDao;

        InsertAllCommentsAsyncTask(CommentDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<Comment>... lists) {
            mAsyncTaskDao.insertAllComments(lists[0]);
            return null;
        }
    }

}
