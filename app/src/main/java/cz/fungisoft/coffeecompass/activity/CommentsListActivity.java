package cz.fungisoft.coffeecompass.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteDetailFragment;

public class CommentsListActivity extends AppCompatActivity {

    private CoffeeSite cs;
    private List<Comment> comments;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        cs = (CoffeeSite) getIntent().getSerializableExtra("site");
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
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
        recyclerView.setAdapter(new CommentsListActivity.CommentItemRecyclerViewAdapter(this, comments ));
    }

        /* *********** RecyclerViewAdapter ************* */

        public static class CommentItemRecyclerViewAdapter extends RecyclerView.Adapter<CommentItemRecyclerViewAdapter.ViewHolder>
        {

            private final CommentsListActivity mParentActivity;
            private final List<Comment> mValues;
    //        private final CoffeeSiteListContent content;

    //        private final boolean mTwoPane;

//            private View.OnClickListener mOnClickListener;

            CommentItemRecyclerViewAdapter(CommentsListActivity parent, List<Comment> comments) {
    //            this.content = content;
                mValues = comments;
                mParentActivity = parent;
    //            mTwoPane = twoPane;

            }

            private View.OnClickListener createOnClickListener() {
                View.OnClickListener retVal = null;

                /*
                retVal = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CoffeeSite item = (CoffeeSite) view.getTag();
                        if (mTwoPane) {
                            Bundle arguments = new Bundle();
                            arguments.putString(CoffeeSiteDetailFragment.ARG_ITEM_ID, Long.toString(item.id));
                            CoffeeSiteDetailFragment fragment = new CoffeeSiteDetailFragment();
                            fragment.setArguments(arguments);
                            mParentActivity.getSupportFragmentManager().beginTransaction()
                                    .addToBackStack(null)
                                    .replace(R.id.coffeesite_detail_container, fragment)
                                    .commit();
                        } else {
                            Context context = view.getContext();
                            Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);

                            intent.putExtra(CoffeeSiteDetailFragment.ARG_ITEM_ID, String.valueOf(item.id));
                            intent.putExtra("listContent", content);
                            intent.putExtra("latFrom", fromLong);
                            intent.putExtra("longFrom", fromLat);

                            context.startActivity(intent);
                        }
                    }
                };
                */
                return retVal;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_list_content, parent, false);
                return new CommentsListActivity.CommentItemRecyclerViewAdapter.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, int position) {
    //            holder.csNameView.setText(String.valueOf(mValues.get(position).id));
//                holder.csNameView.setText(String.valueOf(position + 1));
    //            holder.locAndTypeView.setText(mValues.get(position).name + " (" + mValues.get(position).distance + " m)");

                holder.userAndDateText.setText(mValues.get(position).getUserName() + ", " + mValues.get(position).getCreatedOnString());
                //            holder.locAndTypeView.setText(mValues.get(position).name + " (" + mValues.get(position).distance + " m)");
                holder.commentText.setText(mValues.get(position).getCommentText());
                holder.itemView.setTag(mValues.get(position));
//                holder.itemView.setOnClickListener(mOnClickListener);
            }

            @Override
            public int getItemCount() {
                return mValues.size();
            }

                class ViewHolder extends RecyclerView.ViewHolder {
                    final TextView userAndDateText;
                    final TextView commentText;

                    ViewHolder(View view) {
                        super(view);
                        userAndDateText = (TextView) view.findViewById(R.id.userAndDateText);
                        commentText = (TextView) view.findViewById(R.id.commentText);
//                        locAndTypeView.setTypeface(locAndTypeView.getTypeface(), Typeface.BOLD);
                    }
                }
        }

}
