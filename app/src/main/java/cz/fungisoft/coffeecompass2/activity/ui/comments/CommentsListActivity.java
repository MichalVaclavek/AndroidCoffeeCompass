package cz.fungisoft.coffeecompass2.activity.ui.comments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.asynctask.comment.UpdateCommentAndStarsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.repository.dao.relations.CoffeeSiteWithComments;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetNumberOfStarsAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.DeleteCommentAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.GetCommentsForCoffeeSiteAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.SaveCommentAndStarsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;

import static android.view.View.*;

/**
 * Activity to show list of CoffeeSite's Comments
 * It also allows to add a Comment for current logged-in user
 * using {@link EnterCommentAndRatingDialogFragment}.
 */
public class CommentsListActivity extends AppCompatActivity
                                  implements UserAccountServiceConnectionListener,
                                             EnterCommentAndRatingDialogFragment.CommentAndRatingDialogListener,
                                             DeleteCommentDialogFragment.DeleteCommentDialogListener {

    private static final String TAG = "CommentsListActivity";

    private CoffeeSite cs;

    // Comments of the CoffeeSite
    private List<Comment> shownComments;
    // The text of Comment user wants to modify
    private Comment selectedComment;
    private int starsFromCurrentUser = 0;

    private CommentsListActivity.CommentItemRecyclerViewAdapter recyclerViewAdapter;

    private ProgressBar commentActionsProgressBar;

    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userAccountServiceConnector;

    private enum CommentOperation {SAVE, UPDATE};

    private CommentOperation currentCommentOperation = CommentOperation.SAVE;

    private boolean offLineModeOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        offLineModeOn = Utils.isOfflineModeOn(getApplicationContext());

        setContentView(R.layout.activity_comments);

        CommentsViewModel viewModel = new CommentsViewModel(getApplication());

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            cs = bundle.getParcelable("site");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.comments_toolbar);
        setSupportActionBar(toolbar);

        commentActionsProgressBar = (ProgressBar) findViewById(R.id.comments_progressBar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout appBarLayout = findViewById(R.id.comments_toolbar_layout);

        if (appBarLayout != null) {
            appBarLayout.setTitle(cs.getName());
        }

        RecyclerView recyclerView = findViewById(R.id.commentsList);
        assert recyclerView != null;

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*
        * adds underline under every Comment in the list
         */
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        setupRecyclerView(recyclerView);

        // Adds Floating Action Button if a user is loged-in
        FloatingActionButton fab = findViewById(R.id.fab_new_comment);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCommentOperation = CommentOperation.SAVE;
                if (userAccountService != null && userAccountService.isUserLoggedIn()) {
                    startNumberOfStarsAsyncTask();
                } else {
                    Snackbar mySnackbar = Snackbar.make(view, R.string.comments_only_for_registered_user, Snackbar.LENGTH_LONG);
                    mySnackbar.show();
                }
            }
        });


        if (!offLineModeOn) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(GONE);
        }

        doBindUserAccountService();

        viewModel.getCommentsForCoffeeSite(cs, Utils.isOfflineModeOn(getApplicationContext())).observe(this, new Observer<List<CoffeeSiteWithComments>>() {
            @Override
            public void onChanged(@Nullable final List<CoffeeSiteWithComments> commentsLive) {
                // Update the cached copy of the Comments in the adapter.
                shownComments = commentsLive.get(0).comments;
                showComments(shownComments);
            }
        });
    }

    private void startNumberOfStarsAsyncTask() {
        if (Utils.isOnline()) {
            commentActionsProgressBar.setVisibility(View.VISIBLE);
            // Async task for loading current user's rating for this CoffeeSite
            // The EnterCommentAndRatingDialog dialog is opened after the Async task finishes
            new GetNumberOfStarsAsyncTask(userAccountService.getLoggedInUser().getUserId(), cs.getId(), this).execute();
        } else { // Dialog can be opened as there might be only temporary connection problem
            showEnterCommentAndRatingDialog();
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindUserAccountService();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerViewAdapter = new CommentsListActivity.CommentItemRecyclerViewAdapter( this, offLineModeOn);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    /**
     * Show the dialog
     */
    private void showEnterCommentAndRatingDialog() {
        // Create an instance of the dialog fragment and show it
        //Passes number of stars for CoffeeSite and User to EnterCommentAndRatingDialogFragment
        EnterCommentAndRatingDialogFragment dialog;
        if (currentCommentOperation == CommentOperation.UPDATE) {
            this.selectedComment = recyclerViewAdapter.getCommentAfterCommentTextTap();
            dialog = newInstance(this.starsFromCurrentUser, this.selectedComment != null ? this.selectedComment.getText() : "");
        } else {
            dialog = newInstance(this.starsFromCurrentUser, "");
        }

        dialog.show(getSupportFragmentManager(), "EnterCommentAndRatingDialogFragment");
    }

    public static EnterCommentAndRatingDialogFragment newInstance(int numOfStars, String currentCommentText) {
        EnterCommentAndRatingDialogFragment f = new EnterCommentAndRatingDialogFragment();

        // Supply num and text input as an argument.
        Bundle args = new Bundle();
        args.putInt("numOfStars", numOfStars);
        args.putString("commentText", currentCommentText);
        f.setArguments(args);
        return f;
    }

    // ** UserAccountService connection/disconnection ** //

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserAccountService;


    @Override
    public void onUserAccountServiceConnected() {
        userAccountService = userAccountServiceConnector.getUserLoginService();
        if (userAccountService != null && userAccountService.isUserLoggedIn()) {
            // Check if the RecyclerView is already active, if not activate here with AccountService available
            if (recyclerViewAdapter != null) {
                recyclerViewAdapter.setLoggedInUser(userAccountService.getLoggedInUser());
            }
        }
    }

    /**
     * Process positive response, i.e. try to save Comment and Star
     * @param dialog
     */
    @Override
    public void onSaveUpdateCommentDialogPositiveClick(EnterCommentAndRatingDialogFragment dialog) {
        if (Utils.isOnline()) {
            commentActionsProgressBar.setVisibility(View.VISIBLE);
            if (currentCommentOperation == CommentOperation.SAVE) {
                new SaveCommentAndStarsAsyncTask(cs.getId(), userAccountService.getLoggedInUser(), this, dialog.getCommentAndStars()).execute();
            }
            if (currentCommentOperation == CommentOperation.UPDATE
                   && this.selectedComment != null) {
                this.selectedComment.setText(dialog.getCommentAndStars().getComment());
                this.selectedComment.setStarsFromUser(dialog.getCommentAndStars().getStars().getNumOfStars());
                new UpdateCommentAndStarsAsyncTask(userAccountService.getLoggedInUser(), this, this.selectedComment).execute();
            }
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    /**
     * Process positive response, i.e. try to delete Comment
     * @param dialog
     */
    @Override
    public void onDeleteCommentDialogPositiveClick(DeleteCommentDialogFragment dialog) {
        if (Utils.isOnline()) {
            commentActionsProgressBar.setVisibility(View.VISIBLE);
            new DeleteCommentAsyncTask(recyclerViewAdapter.getCommentIdAfterDeleteIconTap(), userAccountService.getLoggedInUser(), this).execute();
        }
    }

    private void doBindUserAccountService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userAccountServiceConnector = new UserAccountServiceConnector();
        userAccountServiceConnector.addUserAccountServiceConnectionListener(this);

        if (bindService(new Intent(this, UserAccountService.class),
                userAccountServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserAccountService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserAccountService() {
        if (mShouldUnbindUserAccountService) {
            // Release information about the service's state.
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserAccountService = false;
        }
    }

    /**
     * Method to be called from async task after the number of stars for this CoffeeSite and User
     * is returned from server.
     * This method can be called only before a current user wants to open EnterCommentAndRatingDialogFragment
     * to enter new Comment and Stars for the CoffeeSite or before updating current Comment and Stars for CoffeeSite.
     *
     * @param stars
     */
    public void processNumberOfStarsForSiteAndUser(int stars) {
        this.starsFromCurrentUser = stars;
        commentActionsProgressBar.setVisibility(View.GONE);
        showEnterCommentAndRatingDialog();
    }

    /**
     * Method to be called from Async task after failed request for the number<br>
     * of stars for this CoffeeSite and User is returned from server.<br>
     * This method can be called only before a current user wants to open<br>
     * EnterCommentAndRatingDialogFragment to enter Comment and Stars for the CoffeeSite.
     */
    public void processFailedNumberOfStarsForSiteAndUser(Result.Error error) {
        commentActionsProgressBar.setVisibility(View.GONE);
        showRESTCallError(error);
        // Open EnterCommentAndRatingDialogFragment
        this.starsFromCurrentUser = 0;
        showEnterCommentAndRatingDialog();
    }

    /**
     * Method to be called from Async task after new comment is added to coffeeSite's list of comments
     * @param comments
     */
    public void processComments(List<Comment> comments) {
        this.shownComments = comments;
        cs.setComments(this.shownComments);
        commentActionsProgressBar.setVisibility(View.GONE);
        showComments(this.shownComments);
    }

    /**
     * Method to be called from Async task after updated Comment is returned. Modifies
     * view of this Comment in the list of all Comments.
     *
     * @param comments
     */
    public void processUpdatedComment(Comment updatedComment) {
        if (this.shownComments != null) {
            for (Comment comment : this.shownComments) {
                if (comment != null && comment.getId() == updatedComment.getId()) {
                    this.shownComments.set(this.shownComments.indexOf(comment), updatedComment);
                    break;
                }
            }
            showComments(this.shownComments);
        }
        commentActionsProgressBar.setVisibility(View.GONE);
    }

    private void showComments(List<Comment> comments) {
        if (comments != null && comments.size() > 0) {
            recyclerViewAdapter.setComments(comments);
        } else {
            showEmptyComments();
        }
    }

    private void showEmptyComments() {
        List<Comment> emptyComments = new ArrayList<Comment>();
        emptyComments.add(new Comment(getString(R.string.no_comments_available)));
        recyclerViewAdapter.setComments(emptyComments);
    }

    public void showRESTCallError(Result.Error error) {
        if (error.getRestError() != null) {
            Toast.makeText(getApplicationContext(),
                    error.getRestError().getDetail(),
                    Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(getApplicationContext(),
                    error.getDetail(),
                    Toast.LENGTH_SHORT);
        }
        commentActionsProgressBar.setVisibility(View.GONE);
    }


    /**
     * Method to be called from async task after ...
     * @param numberOfCommentsAfterDelete
     */
    public void processNumberOfComments(int numberOfCommentsAfterDelete) {
        if (numberOfCommentsAfterDelete > 0) {
            if (Utils.isOnline()) {
                new GetCommentsForCoffeeSiteAsyncTask(this, cs.getId()).execute();
            }
        } else {
            cs.clearComments();
            showComments(null);
        }

        commentActionsProgressBar.setVisibility(View.GONE);
    }

    /* *********** RecyclerViewAdapter ************* */

        public static class CommentItemRecyclerViewAdapter extends RecyclerView.Adapter<CommentItemRecyclerViewAdapter.ViewHolder> {

            private final boolean offLineModeOn;
            private List<Comment> mValues;
            private CommentsListActivity parenActivity;

            public void setLoggedInUser(LoggedInUser loggedInUser) {
                this.loggedInUser = loggedInUser;
            }

            private LoggedInUser loggedInUser;

            private int commentIdToDelete;
            private Comment selectedComment;

             CommentItemRecyclerViewAdapter(CommentsListActivity parenActivity, boolean offLineModeOn) {
                 this.offLineModeOn = offLineModeOn;
                 this.parenActivity = parenActivity;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_list_content, parent, false);
                return new CommentsListActivity.CommentItemRecyclerViewAdapter.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, int position) {

                if (mValues != null && mValues.size() > 0) {

                    final Comment item = mValues.get(position);

                    if (getItemCount() == 1 && item.getId() == 0
                       && item.getText().equals(holder.commentTextView.getContext().getString(R.string.no_comments_available))) {
                        hideRatingSigns(holder);
                        hideNameAndDate(holder);
                        hideButtons(holder);

                        holder.commentTextView.setTypeface(holder.commentTextView.getTypeface(), Typeface.ITALIC);
                        holder.commentTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                        holder.commentTextView.setText(R.string.no_comments_available);
                    } else {

                        showRatingSigns(holder);
                        showNameAndDate(holder);

                        // Inserts dots/icons of user's rating
                        for (int i = item.getStarsFromUser() - 1; i >= 0; i--) {
                            holder.starsImageView.get(i).setImageDrawable(this.parenActivity.getDrawable(R.drawable.rating_star_full));
                        }

                        holder.commentTextView.setTypeface(holder.commentTextView.getTypeface(), Typeface.NORMAL);
                        holder.commentTextView.setTextAlignment(TEXT_ALIGNMENT_TEXT_START);
                        holder.commentTextView.setText(item.getText());
                        holder.itemView.setTag(item);

                        holder.userAndDateText.setText(item.getUserName() + ", " + item.getCreatedOnString());

                        if (loggedInUser != null && item.getUserName().equals(loggedInUser.getUserName())
                           && !this.offLineModeOn) {
                            showButtons(holder);
                        } else {
                            hideButtons(holder);
                        }

                        holder.deleteButtonIcon.setOnClickListener(
                                new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        commentIdToDelete = item.getId();
                                        DeleteCommentDialogFragment deleteDialog = new DeleteCommentDialogFragment();
                                        deleteDialog.show(parenActivity.getSupportFragmentManager(), "Delete comment dialog");
                                    }
                                }
                        );
                        // Handle click to edit Comment - add the click handler to both edit icon and
                        // whole LinearLayout with Comment and Rating itself
                        View.OnClickListener editCommentClickListener = view -> {
                            selectedComment = item;
                            parenActivity.currentCommentOperation = CommentOperation.UPDATE;
                            if (parenActivity.userAccountService != null && parenActivity.userAccountService.isUserLoggedIn()) {
                                parenActivity.startNumberOfStarsAsyncTask();
                            }
                        };

                        if (!offLineModeOn) {
                            holder.commentTextView.setOnClickListener(editCommentClickListener);
                            holder.editButtonIcon.setOnClickListener(editCommentClickListener);
                        }
                    }
                }
            }

            private void hideRatingSigns(ViewHolder holder) {
                for (ImageView starView : holder.starsImageView) {
                    starView.setVisibility(GONE);
                }
            }

            private void showRatingSigns(ViewHolder holder) {
                for (ImageView starView : holder.starsImageView) {
                    starView.setVisibility(VISIBLE);
                }
            }

            private void hideNameAndDate(ViewHolder holder) {
                holder.userAndDateText.setVisibility(GONE);
            }

            private void showNameAndDate(ViewHolder holder) {
                holder.userAndDateText.setVisibility(VISIBLE);
            }

            private void hideButtons(ViewHolder holder) {
                holder.deleteButtonIcon.setVisibility(GONE);
                holder.editButtonIcon.setVisibility(GONE);
            }

            private void showButtons(ViewHolder holder) {
                holder.deleteButtonIcon.setVisibility(VISIBLE);
                holder.editButtonIcon.setVisibility(VISIBLE);
            }

            public int getCommentIdAfterDeleteIconTap() {
                return commentIdToDelete;
            }

            public Comment getCommentAfterCommentTextTap() {
                return selectedComment;
            }

            @Override
            public int getItemCount() {
                return (mValues != null) ? mValues.size() : 0;
            }

            public void setComments(List<Comment> comments) {
                this.mValues = comments;
                notifyDataSetChanged();
            }

            /**
                * Inner ViewHolder class for CommentItemRecyclerViewAdapter
                */
                class ViewHolder extends RecyclerView.ViewHolder {

                    final TextView userAndDateText;
                    final TextView commentTextView;
                    final LinearLayout commentAndRatingLayout;

                    final LinearLayout editAndDeleteIconLayout;
                    final ImageView deleteButtonIcon;
                    final ImageView editButtonIcon;

                    final ImageView starRating1;
                    final ImageView starRating2;
                    final ImageView starRating3;
                    final ImageView starRating4;
                    final ImageView starRating5;

                    final List<ImageView> starsImageView;


                    ViewHolder(View view) {
                        super(view);

                        starsImageView = new ArrayList<>();

                        userAndDateText = (TextView) view.findViewById(R.id.userAndDateText);
                        commentTextView = (TextView) view.findViewById(R.id.commentText);
                        commentAndRatingLayout = (LinearLayout)  view.findViewById(R.id.user_comment_and_rating_linearlayout);

                        editAndDeleteIconLayout = (LinearLayout)  view.findViewById(R.id.edit_delete_comment_images_linearlayout);
                        deleteButtonIcon = (ImageView) view.findViewById(R.id.deleteIconImageView);
                        editButtonIcon = (ImageView) view.findViewById(R.id.edit_comment_icon_imageview);

                        starRating5 = (ImageView) view.findViewById(R.id.rating_imageView_5);
                        starsImageView.add(starRating5);
                        starRating4 = (ImageView) view.findViewById(R.id.rating_imageView_4);
                        starsImageView.add(starRating4);
                        starRating3 = (ImageView) view.findViewById(R.id.rating_imageView_3);
                        starsImageView.add(starRating3);
                        starRating2 = (ImageView) view.findViewById(R.id.rating_imageView_2);
                        starsImageView.add(starRating2);
                        starRating1 = (ImageView) view.findViewById(R.id.rating_imageView_1);
                        starsImageView.add(starRating1);
                    }
                }
        }

}
