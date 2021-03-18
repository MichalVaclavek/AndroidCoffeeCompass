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


    private CommentRepository commentRepository;

    /**
     * Actual list of all Comments
     */
    private final LiveData<List<Comment>> allComments;

    public CommentsViewModel(@NonNull Application application) {
        super(application);
        CoffeeSiteDatabase db = CoffeeSiteDatabase.getDatabase(application.getApplicationContext());
        commentRepository = CommentRepository.getInstance(db);
        allComments  = commentRepository.getAllComments();
    }

    private final MutableLiveData<CommentRepository.CommentsLiveDataInput> commentsInput = new MutableLiveData<>();

    private void setInput(boolean offlineModeOn, CoffeeSite coffeeSite) {
        commentsInput.setValue(new CommentRepository.CommentsLiveDataInput(offlineModeOn, coffeeSite));
    }

    /**
     * Actual list of all Comments belonging to one CoffeeSite
     */
    private final LiveData<List<CoffeeSiteWithComments>> commentsOfCoffeeSite =
            Transformations.switchMap(commentsInput, (ci) -> commentRepository.getCommentsForCoffeeSite(ci.getCoffeeSite(), ci.isOfflineModeOn()));


    public LiveData<List<Comment>> getAllComments(boolean offlineModeOn) {
        setInput(offlineModeOn, null);
        return allComments;
    }


    public LiveData<List<CoffeeSiteWithComments>> getCommentsForCoffeeSite(CoffeeSite coffeeSite, boolean offlineModeOn) {
        setInput(offlineModeOn, coffeeSite);
        return commentsOfCoffeeSite;
    }

}
