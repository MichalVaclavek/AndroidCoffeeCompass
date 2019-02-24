package cz.fungisoft.coffeecompass.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.Utils;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteDetailFragment;


/**
 * An activity representing a list of CoffeeSites. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CoffeeSiteDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CoffeeSiteListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private CoffeeSiteListContent content;

    /**
     * Location of the searchFromPoint to be passed to Map if selected
     */
    private double fromLat;
    private double fromLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.sitesListToolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.coffeesite_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.coffeesite_list);
        assert recyclerView != null;

        Bundle extras = getIntent().getExtras();

        content = (CoffeeSiteListContent) getIntent().getSerializableExtra("listContent");
        fromLat = getIntent().getDoubleExtra("latFrom", 181);
        fromLong = getIntent().getDoubleExtra("longFrom", 181);

        if (extras != null) {
            setupRecyclerView((RecyclerView) recyclerView, content);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_map:

                if (Utils.isOnline()) {
                    openMap();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "No Internet connection.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openMap() {

        if ((this.fromLong >= -180) && (this.fromLong <= 180)
                && (this.fromLat >= -180) && (this.fromLat <= 180)) {

            Intent mapIntent = new Intent(this, MapsActivity.class);
            mapIntent.putExtra("currentLong", fromLong);
            mapIntent.putExtra("currentLat", fromLat);
            mapIntent.putExtra("listContent", content);
            startActivity(mapIntent);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, CoffeeSiteListContent listContent) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, listContent, fromLat, fromLong, mTwoPane));
    }

    /* *********** RecyclerViewAdapter ************* */

    public static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>
    {

        private final CoffeeSiteListActivity mParentActivity;
        private final List<CoffeeSite> mValues;
        private final CoffeeSiteListContent content;

        private final boolean mTwoPane;

        private View.OnClickListener mOnClickListener;

        SimpleItemRecyclerViewAdapter(CoffeeSiteListActivity parent, CoffeeSiteListContent content,
                                      double fromLatLoc, double fromLongLoc,
                                      boolean twoPane) {
            this.content = content;
            mValues = this.content.getItems();
            mParentActivity = parent;
            mTwoPane = twoPane;

            mOnClickListener = createOnClickListener(fromLatLoc, fromLongLoc);
        }


        private View.OnClickListener createOnClickListener(final double fromLong, final double fromLat) {
            View.OnClickListener retVal;

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
            return retVal;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.coffeesite_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
//            holder.mIdView.setText(String.valueOf(mValues.get(position).id));
            holder.mIdView.setText(String.valueOf(position + 1));
            holder.mContentView.setText(mValues.get(position).name + " (" + mValues.get(position).distance + " m)");

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
                mContentView.setTypeface(mContentView.getTypeface(), Typeface.BOLD);
            }
        }
    }

    /* Adapter end */
}
