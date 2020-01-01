package cz.fungisoft.coffeecompass2.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetNumberOfStarsAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.DeleteCommentAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.GetCommentsAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.SaveCommentAndStarsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceConnectionListener;

import static android.view.View.*;

/**
 * Activity to show list of CoffeeSite's Comments
 * Later it should allow to add a Comment for {@link LoggedInUser}
 */
public class CommentsListActivity extends AppCompatActivity implements UserLoginServiceConnectionListener,
                                                                       EnterCommentAndRatingDialogFragment.CommentAndRatingDialogListener,
                                                                       DeleteCommentDialogFragment.DeleteCommentDialogListener {

    private static final String TAG = "CommentsListActivity";

    private CoffeeSite cs;
    private List<Comment> comments;
    private int starsFromCurrentUser = 0;
    private RecyclerView.LayoutManager layoutManager;

    private RecyclerView recyclerView;
    private CommentsListActivity.CommentItemRecyclerViewAdapter recyclerViewAdapter;

    private ProgressBar commentActionsProgressBar;

    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userLoginServiceConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            cs = (CoffeeSiteMovable) bundle.getParcelable("site");
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

        recyclerView = findViewById(R.id.commentsList);
        assert recyclerView != null;

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*
        * adds underline under every Comment in the list
         */
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        doBindUserLoginService();
    }

    @Override
    protected void onDestroy() {
        doUnbindUserLoginService();
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

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<Comment> comments) {
        recyclerViewAdapter =
                new CommentsListActivity.CommentItemRecyclerViewAdapter(comments, userAccountService, this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    /**
     * Show the dialog
     */
    private void showEnterCommentAndRatingDialog() {
        // Create an instance of the dialog fragment and show it
        //EnterCommentAndRatingDialogFragment dialog = new EnterCommentAndRatingDialogFragment();
        //Passes number of stars for CoffeeSite and User to EnterCommentAndRatingDialogFragment
        EnterCommentAndRatingDialogFragment dialog = newInstance(this.starsFromCurrentUser);
        dialog.show(getSupportFragmentManager(), "EnterCommentAndRatingDialogFragment");
    }

    public static EnterCommentAndRatingDialogFragment newInstance(int numOfStars) {
        EnterCommentAndRatingDialogFragment f = new EnterCommentAndRatingDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("numOfStars", numOfStars);
        f.setArguments(args);

        return f;
    }

    // ** UserLogin Service connection/disconnection ** //

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    @Override
    public void onUserLoginServiceConnected() {
        userAccountService = userLoginServiceConnector.getUserLoginService();

        if (userAccountService != null && userAccountService.isUserLoggedIn()) {

            if (Utils.isOnline()) {
                commentActionsProgressBar.setVisibility(View.VISIBLE);
                // Async task to load Comments for the site
                // Comments are shown at the end of the Async task
                new GetCommentsAsyncTask(this, cs.getId()).execute();
                // Async task for loading current user rating for this CoffeeSite
                new GetNumberOfStarsAsyncTask( Integer.parseInt(userAccountService.getLoggedInUser().getUserId()), cs.getId(), this).execute();
            }
            // Adds Floating Action Button if a user is loged-in
            FloatingActionButton fab = findViewById(R.id.fab_new_comment);

            fab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEnterCommentAndRatingDialog();
                }
            });
            fab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Process positive response, i.e. try to save Comment and Star
     * @param dialog
     */
    @Override
    public void onSaveCommentDialogPositiveClick(EnterCommentAndRatingDialogFragment dialog) {
        if (Utils.isOnline()) {
            commentActionsProgressBar.setVisibility(View.VISIBLE);
            new SaveCommentAndStarsAsyncTask(cs.getId(), userAccountService.getLoggedInUser(), this, dialog.getCommentAndStars()).execute();
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    /**
     * Process negative response. nothing to do here
     * @param dialog
     */
    @Override
    public void onSaveCommentDialogNegativeClick(EnterCommentAndRatingDialogFragment dialog) {
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

    @Override
    public void onDeleteCommentDialogNegativeClick(DeleteCommentDialogFragment dialog) {

    }

    private void doBindUserLoginService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userLoginServiceConnector = new UserAccountServiceConnector(this);
        if (bindService(new Intent(this, UserAccountService.class),
                userLoginServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserLoginService() {
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            unbindService(userLoginServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

    /**
     * Method to be called from async task after the number of stars for this CoffeeSite and User is returned from server.
     * @param stars
     */
    public void processNumberOfStarsForSiteAndUser(int stars) {
        this.starsFromCurrentUser = stars;
        commentActionsProgressBar.setVisibility(View.GONE);
    }

    /**
     * Method to be called from async task after new comment is added to coffeeSite's list of comments
     * @param comments
     */
    public void processComments(List<Comment> comments) {
        cs.setComments(comments);
        showComments();
        commentActionsProgressBar.setVisibility(View.GONE);
    }

    private void showComments() {
        this.comments = cs.getComments();
        if (this.comments != null) {
            setupRecyclerView((RecyclerView) this.recyclerView, this.comments);
        }
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
     * Method to be called from async task after delete of comment.
     * @param numberOfCommentsAfterDelete
     */
    public void processNumberOfComments(int numberOfCommentsAfterDelete) {
        if (numberOfCommentsAfterDelete > 0) {
            if (Utils.isOnline()) {
                new GetCommentsAsyncTask(this, cs.getId()).execute();
            }
        } else {
            cs.clearComments();
            showComments();
        }

        commentActionsProgressBar.setVisibility(View.GONE);
    }


    /* *********** RecyclerViewAdapter ************* */

        public static class CommentItemRecyclerViewAdapter extends RecyclerView.Adapter<CommentItemRecyclerViewAdapter.ViewHolder>
        {
            private final List<Comment> mValues;
            private CommentsListActivity parenActivity;
            private UserAccountService userAccountService;

            private int commentIdToDelete;


            CommentItemRecyclerViewAdapter(List<Comment> comments, UserAccountService userAccountService, CommentsListActivity parenActivity) {
                this.mValues = comments;
                this.parenActivity = parenActivity;
                this.userAccountService = userAccountService;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_list_content, parent, false);
                return new CommentsListActivity.CommentItemRecyclerViewAdapter.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, int position) {

                holder.userAndDateText.setText(mValues.get(position).getUserName() + ", " + mValues.get(position).getCreatedOnString());
                holder.commentText.setText(mValues.get(position).getText());
                holder.itemView.setTag(mValues.get(position));

                final Comment item = mValues.get(position);

                if ((item != null) && item.getUserName().equals(userAccountService.getLoggedInUser().getUserName())) {
                    holder.deleteButtonIcon.setVisibility(VISIBLE);
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
            }

            public int getCommentIdAfterDeleteIconTap() {
                return commentIdToDelete;
            }

            @Override
            public int getItemCount() {
                return mValues.size();
            }

                /**
                * Inner ViewHolder class for CommentItemRecyclerViewAdapter
                */
                class ViewHolder extends RecyclerView.ViewHolder {

                    final TextView userAndDateText;
                    final TextView commentText;
                    final ImageView deleteButtonIcon;

                    ViewHolder(View view) {
                        super(view);
                        userAndDateText = (TextView) view.findViewById(R.id.userAndDateText);
                        commentText = (TextView) view.findViewById(R.id.commentText);
                        deleteButtonIcon = (ImageView) view.findViewById(R.id.deleteIconImageView);
                    }
                }
        }

}
