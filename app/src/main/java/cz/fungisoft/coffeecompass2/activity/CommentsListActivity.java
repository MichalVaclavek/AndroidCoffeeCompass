package cz.fungisoft.coffeecompass2.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.Comment;

/**
 * Activity to show list of CoffeeSite's Comments
 * Later it should allow to add a Comment for {@link LoggedInUser}
 */
public class CommentsListActivity extends AppCompatActivity {

    private CoffeeSite cs;
    private List<Comment> comments;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            cs = (CoffeeSiteMovable) bundle.getParcelable("site");
        }
        comments = cs.getComments();

        Toolbar toolbar = (Toolbar) findViewById(R.id.comments_toolbar);
        setSupportActionBar(toolbar);

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

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*
        * adds underline under every Comment in the list
         */
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        if (comments != null) {
            setupRecyclerView((RecyclerView) recyclerView, comments);
        }
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
        recyclerView.setAdapter(new CommentsListActivity.CommentItemRecyclerViewAdapter(comments ));
    }

        /* *********** RecyclerViewAdapter ************* */

        public static class CommentItemRecyclerViewAdapter extends RecyclerView.Adapter<CommentItemRecyclerViewAdapter.ViewHolder>
        {
            private final List<Comment> mValues;

            CommentItemRecyclerViewAdapter(List<Comment> comments) {
                mValues = comments;
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
                holder.commentText.setText(mValues.get(position).getCommentText());
                holder.itemView.setTag(mValues.get(position));
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

                    ViewHolder(View view) {
                        super(view);
                        userAndDateText = (TextView) view.findViewById(R.id.userAndDateText);
                        commentText = (TextView) view.findViewById(R.id.commentText);
                    }
                }
        }

}
