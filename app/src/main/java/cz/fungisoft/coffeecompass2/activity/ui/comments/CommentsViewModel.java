package cz.fungisoft.coffeecompass2.activity.ui.comments;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteDatabase;
import cz.fungisoft.coffeecompass2.entity.repository.CommentRepository;
import cz.fungisoft.coffeecompass2.entity.repository.dao.relations.CoffeeSiteWithComments;

/**
 * Data Model to be hold by {@link CommentsListActivity}.
 * Represents all Comments and/or CoffeeSite's comments to be displayed by the activity's RecyclerViewAdapter
 */
public class CommentsViewModel extends AndroidViewModel {

    private static final String TAG = "CommentsViewModel";

    /**
     * Inner class to hold search parameters for comments
     */
    static class CommentsLiveDataInput {

        public boolean isOfflineModeOn() {
            return offlineModeOn;
        }

        public CoffeeSite getCoffeeSite() {
            return coffeeSite;
        }

        private boolean offlineModeOn;
        private CoffeeSite coffeeSite;

        public CommentsLiveDataInput(boolean offlineModeOn, CoffeeSite coffeeSite) {
            this.offlineModeOn = offlineModeOn;
            this.coffeeSite = coffeeSite;
        }
    }

    private final MutableLiveData<CommentsLiveDataInput> commentsInput = new MutableLiveData<>();

    private void setInput(boolean offlineModeOn, CoffeeSite coffeeSite) {
        commentsInput.setValue(new CommentsLiveDataInput(offlineModeOn, coffeeSite));
    }

    private CommentRepository commentRepository;

    /**
     * Actual list of all Comments belonging to one CoffeeSite
     */
    private LiveData<List<CoffeeSiteWithComments>> commentsOfCoffeeSite =
            Transformations.switchMap(commentsInput, (ci) -> commentRepository.getCommentsForCoffeeSite(ci.coffeeSite, ci.offlineModeOn));

    /**
     * Actual list of all Comments
     */
    private LiveData<List<Comment>> allComments;


    public CommentsViewModel(@NonNull Application application) {
        super(application);
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(application.getApplicationContext());
        commentRepository = CommentRepository.getInstance(db);
        allComments  = commentRepository.getAllComments();
    }


    public LiveData<List<Comment>> getAllComments(boolean offlineModeOn) {
        setInput(offlineModeOn, null);
        return allComments;
    }


    public LiveData<List<CoffeeSiteWithComments>> getCommentsForCoffeeSite(CoffeeSite coffeeSite, boolean offlineModeOn) {
        setInput(offlineModeOn, coffeeSite);
        return commentsOfCoffeeSite;
    }

}
