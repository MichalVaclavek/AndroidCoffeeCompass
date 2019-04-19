package cz.fungisoft.coffeecompass.activity.support;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.activity.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass.activity.CoffeeSiteListActivity;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass.services.LocationService;
import cz.fungisoft.coffeecompass.ui.fragments.CoffeeSiteDetailFragment;

/**
 * Adapter for the found list of CoffeeSitesMovable
 */
public class CoffeeSiteItemRecyclerViewAdapterForCoffeeSites extends RecyclerView.Adapter<CoffeeSiteItemRecyclerViewAdapterForCoffeeSites.ViewHolder> implements PropertyChangeListener {

    private static final String TAG = "CoffeeSiteListAdapter";

    private final CoffeeSiteListActivity mParentActivity;
    private final List<CoffeeSiteMovable> mValues;
    private final CoffeeSiteListContent content;

    private final boolean mTwoPane;

    private View.OnClickListener mOnClickListener;

    private static LocationService mLocationService;

    /**
     * Checks if the order of CoffeeSiteMovable items in the list needs 're-ordering'<br>
     * caused by new distance detected within one of the CoffeeSiteMovable in the list.<br>
     *
     *
     * @param evt - event of CoffeeSite's 'distance' property change
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (mValues != null) {
            int fromPosition = -1;
            int toPosition = -1;
            boolean moveUp  = false;
            boolean moveDown = false;
            //1. Find an item which is not in right position
            // only one item can be on a wrong position ??
            //TODO - to be sure, we should keep searching for wrong positions until end of list??
            // remember current/wrong pos.
            //2. Find if it has to move up or down
            for (int i = 0; i < mValues.size() ; i++) {
                // check if dist. of this csm is higher then previous csm
                if (i != 0) {
                   if (mValues.get(i).getDistance() < mValues.get(i-1).getDistance()) { // wrong position
                       fromPosition = i;
                       moveUp = true;
                       break;
                   }
                }
                // check if dist. of this csm is lower then next csm
                if (i != mValues.size() - 1) {
                    if (mValues.get(i).getDistance() > mValues.get(i+1).getDistance()) { // wrong position
                        fromPosition = i;
                        moveDown = true;
                        break;
                    }
                }
            }

            // 3. find a new position
            if (moveUp) {
                for (int i = fromPosition - 1; i > 0 ; i--) { // go through higher positions and remember the latest where the distance is higher then mValues.get(fromPosition)
                    if (mValues.get(fromPosition).getDistance() < mValues.get(i).getDistance()) { // correct position found
                        toPosition = i;
                    }
                }
            }
            if (moveDown) {
                for (int i = fromPosition + 1; i < mValues.size() ; i++) { // go down through positions and remember the latest where the distance is lower then mValues.get(fromPosition)
                    if (mValues.get(fromPosition).getDistance() > mValues.get(i).getDistance()) { // correct position
                        toPosition = i;
                    }
                }
            }

            // 4. update data array model
            // 5. and notify RecyclerView to update items
            if ( moveUp || moveDown ) {
                CoffeeSiteMovable item = mValues.get(fromPosition);
                mValues.remove(fromPosition);
                mValues.add(toPosition, item);
                // notify adapter
                this.notifyItemMoved(fromPosition, toPosition);
            }
        }
    }

        /**
         * Inner ViewHolder class for CoffeeSiteItemRecyclerViewAdapter
         */
        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView csNameView; // to show name of CoffeeSite
            final TextView locAndTypeView; // to show type of the CoffeeSite and location type
            final TextView coffeeSortView; // to show available sorts of coffee on this CoffeeSite
            //                final TextView distanceView; // to show distance attribute of the CoffeeSite
            final DistanceChangeTextView distanceView;

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
                distanceView = (DistanceChangeTextView) view.findViewById(R.id.csDistanceTextView);
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
    public CoffeeSiteItemRecyclerViewAdapterForCoffeeSites(CoffeeSiteListActivity parent, CoffeeSiteListContent content,
                                                           LocationService locationService,
                                                           boolean twoPane) {
        this.content = content;
        mValues = this.content.getItems();
        mParentActivity = parent;
        mTwoPane = twoPane;
        mLocationService = locationService;

        mOnClickListener = createOnClickListener();
    }

    private View.OnClickListener createOnClickListener() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSiteMovable item = (CoffeeSiteMovable) view.getTag();
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
    public CoffeeSiteItemRecyclerViewAdapterForCoffeeSites.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coffeesite_list_content, parent, false);

        return new CoffeeSiteItemRecyclerViewAdapterForCoffeeSites.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CoffeeSiteItemRecyclerViewAdapterForCoffeeSites.ViewHolder holder, int position) {

        holder.csNameView.setText(mValues.get(position).getName());
        holder.locAndTypeView.setText(mValues.get(position).getTypPodniku() + ", " +  mValues.get(position).getTypLokality());
        holder.coffeeSortView.setText(mValues.get(position).getCoffeeSorts());

        holder.distanceView.setText(mValues.get(position).getDistance() + " m");
        holder.distanceView.setCoffeeSite(mValues.get(position));
        holder.distanceView.setTag(TAG + ". DistanceTextView for " + mValues.get(position).getName());

        mValues.get(position).addPropertyChangeListener(holder.distanceView);
        Log.d(TAG, ". Distance Text View " + holder.distanceView.getTag() + " added to listen distance change of " + mValues.get(position).getName() + ". Object id: " + mValues.get(position));

        if (!mValues.get(position).getMainImageURL().isEmpty()) {
            Picasso.get().load(mValues.get(position).getMainImageURL()).rotate(90).into(holder.siteFoto);
        }

        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

}
