package cz.fungisoft.coffeecompass2.entity.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.entity.repository.dao.CommentDao;
import io.reactivex.Flowable;

public class CommentRepository {

    private CommentDao commentDao;
    private LiveData<List<Comment>> mAllComments;

    CommentRepository(Context context) {
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(context);
        commentDao = db.commentDao();
        mAllComments = commentDao.getAllComments();
    }

    public LiveData<List<Comment>> getAllComments() {
        return mAllComments;
    }

    public Flowable<Comment> getComment(String commentText) {
        return commentDao.getComment(commentText);
    }

    public Flowable<Comment> getCommentById(int commentId) {
        return commentDao.getCommentById(commentId);
    }

    public void insert (Comment comment) {
        new CommentRepository.insertAsyncTask(commentDao).execute(comment);
    }

    private static class insertAsyncTask extends AsyncTask<Comment, Void, Void> {

        private CommentDao mAsyncTaskDao;

        insertAsyncTask(CommentDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Comment... params) {
            mAsyncTaskDao.insertComment(params[0]);
            return null;
        }
    }

    public void insertAll (List<Comment> Comments) {
        new InsertAllAsyncTask(commentDao).execute(Comments);
    }

    private static class InsertAllAsyncTask extends AsyncTask<List<Comment>, Void, Void> {

        private CommentDao mAsyncTaskDao;

        InsertAllAsyncTask(CommentDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(List<Comment>... lists) {
            mAsyncTaskDao.insertAll(lists[0]);
            return null;
        }
    }

}
