package cz.fungisoft.coffeecompass.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.Utils;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass.services.LocationService;
import cz.fungisoft.coffeecompass.services.UpdateDistanceTimerTask;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteDetailFragment;


/**
 * An activity representing a list of CoffeeSites. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CoffeeSiteDetailActivity} representing
 * item details.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CoffeeSiteListActivity extends ActivityWithLocationService {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * The main attribute of activity containing all the CoffeeSites to show
     * on this or child Activities
     */
    private CoffeeSiteListContent content;

    private CoffeeSiteItemRecyclerViewAdapter recyclerViewAdapter;

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

        content = (CoffeeSiteListContent) getIntent().getSerializableExtra("listContent");
    }

    @Override
    public void onPause() {
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.cancelDistanceUpdateTimers();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.startDistanceUpdateTimers();
        }
    }

    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();

        Bundle extras = getIntent().getExtras();
        View recyclerView = findViewById(R.id.coffeesite_list);
        assert recyclerView != null;
        if (extras != null) {
            setupRecyclerView((RecyclerView) recyclerView, content);
        }
    }

    @Override
    public void updateDistanceTextViewAndOrModel(int position, long meters) {
        content.getItems().get(position).setDistance(meters);
        recyclerViewAdapter.updateDistanceTextView(position, meters);
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
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("currentLocation", locationService.getCurrentLocation());
        mapIntent.putExtra("listContent", content);
        startActivity(mapIntent);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, CoffeeSiteListContent listContent) {
        recyclerViewAdapter = new CoffeeSiteItemRecyclerViewAdapter(this, listContent, locationService , mTwoPane);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.startDistanceUpdateTimers();
    }

        /* Inner class */
        /* *********** RecyclerViewAdapter, needed for RecyclerView ************* */
        public static class CoffeeSiteItemRecyclerViewAdapter extends RecyclerView.Adapter<CoffeeSiteItemRecyclerViewAdapter.ViewHolder>
        {
            private final CoffeeSiteListActivity mParentActivity;
            private final List<CoffeeSite> mValues;
            private final CoffeeSiteListContent content;

            private final boolean mTwoPane;

            private View.OnClickListener mOnClickListener;

            private static LocationService mLocationService;

            private List<UpdateDistanceTimerTask> checkingDistanceTimerTasks;

            private Map<Integer, TextView> distanceTextViews;


            /**
             * Inner ViewHolder class for CoffeeSiteItemRecyclerViewAdapter
             */
            class ViewHolder extends RecyclerView.ViewHolder {

                final TextView csNameView; // to show name of CoffeeSite
                final TextView locAndTypeView; // to show type of the CoffeeSite and location type
                final TextView coffeeSortView; // to show available sorts of coffee on this CoffeeSite
                final TextView distanceView; // to show distance attribute of the CoffeeSite

                final ImageView siteFoto;

                /**
                 * Standard constructor for ViewHolder.
                 *
                 * @param view
                 */
                ViewHolder(View view) {
                    super(view);
                    csNameView = (TextView) view.findViewById(R.id.csNameTextView);
                    locAndTypeView = (TextView) view.findViewById(R.id.locAndTypeTextView);
                    coffeeSortView = (TextView) view.findViewById(R.id.coffeeSortsTextView);
                    distanceView = (TextView) view.findViewById(R.id.csDistanceTextView);
                    siteFoto = (ImageView) view.findViewById(R.id.csListFotoImageView);
                }
            }

            /**
             * Standard constructor of the inner class CoffeeSiteItemRecyclerViewAdapter
             *
             * @param parent - parent Activity for the Adapter, in this case this CoffeeSiteListActivity
             * @param content - instance of the CoffeeSiteListContent to be displayed by this activity
             * @param twoPane
             */
            CoffeeSiteItemRecyclerViewAdapter(CoffeeSiteListActivity parent, CoffeeSiteListContent content,
                                              LocationService locationService,
                                              boolean twoPane) {
                this.content = content;
                mValues = this.content.getItems();
                mParentActivity = parent;
                mTwoPane = twoPane;
                mLocationService = locationService;

                mOnClickListener = createOnClickListener();

                checkingDistanceTimerTasks = new ArrayList<>();
                distanceTextViews = new HashMap<>();
            }

              private View.OnClickListener createOnClickListener() {
                View.OnClickListener retVal;

                retVal = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CoffeeSite item = (CoffeeSite) view.getTag();
                        if (mTwoPane) {
                            Bundle arguments = new Bundle();
                            arguments.putString(CoffeeSiteDetailFragment.ARG_ITEM_ID, Long.toString(item.getId()));
                            CoffeeSiteDetailFragment fragment = new CoffeeSiteDetailFragment();
                            fragment.setArguments(arguments);
                            mParentActivity.getSupportFragmentManager().beginTransaction()
                                    .addToBackStack(null)
                                    .replace(R.id.coffeesite_detail_container, fragment)
                                    .commit();
                        } else {
                            Context context = view.getContext();
                            Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);

                            intent.putExtra(CoffeeSiteDetailFragment.ARG_ITEM_ID, String.valueOf(item.getId()));
                            intent.putExtra("listContent", content);
                            intent.putExtra("latFrom", mLocationService.getCurrentLocation().latitude); // needed to be passed to MapsActivity if chosen in CoffeeSiteDetailActivity
                            intent.putExtra("longFrom", mLocationService.getCurrentLocation().longitude);

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

                holder.csNameView.setText(mValues.get(position).getName());
                holder.locAndTypeView.setText(mValues.get(position).getTypPodniku() + ", " +  mValues.get(position).getTypLokality());
                holder.coffeeSortView.setText(mValues.get(position).getCoffeeSorts());
                holder.distanceView.setText(mValues.get(position).getDistance() + " m");
                distanceTextViews.put(position, holder.distanceView);

                if (!mValues.get(position).getMainImageURL().isEmpty()) {
                    Picasso.get().load(mValues.get(position).getMainImageURL()).rotate(90).into(holder.siteFoto);
                }

                holder.itemView.setTag(mValues.get(position));
                holder.itemView.setOnClickListener(mOnClickListener);

                LatLng siteLatLng = new LatLng(mValues.get(position).getLatitude(), mValues.get(position).getLongitude());
                UpdateDistanceTimerTask checkingDistanceTimerTask  = new UpdateDistanceTimerTask(mParentActivity, position, siteLatLng, mLocationService);
                checkingDistanceTimerTasks.add(checkingDistanceTimerTask);
                checkingDistanceTimerTask.startTimerTask(1000, 1000);
            }

            public void cancelDistanceUpdateTimers() {
                for (UpdateDistanceTimerTask task : checkingDistanceTimerTasks) {
                    if (task != null && task.isRunning()) {
                        task.stopTimerTask();
                    }
                }
                checkingDistanceTimerTasks.clear();
            }

            public void startDistanceUpdateTimers() {
                if (checkingDistanceTimerTasks.size() == 0) {
                    for (Map.Entry<Integer, TextView> distTextView : distanceTextViews.entrySet()) {

                        LatLng siteLatLng = new LatLng(mValues.get(distTextView.getKey()).getLatitude(), mValues.get(distTextView.getKey()).getLongitude());
                        UpdateDistanceTimerTask checkingDistanceTimerTask = new UpdateDistanceTimerTask(mParentActivity, distTextView.getKey(), siteLatLng, mLocationService);
                        checkingDistanceTimerTasks.add(checkingDistanceTimerTask);
                        checkingDistanceTimerTask.startTimerTask(1000, 1000);
                    }
                }
            }

            @Override
            public int getItemCount() {
                return mValues.size();
            }

            public void updateDistanceTextView(int position, long distance) {
                distanceTextViews.get(position).setText(distance + " m");
            }
        }

        /* Adapter end */
}
