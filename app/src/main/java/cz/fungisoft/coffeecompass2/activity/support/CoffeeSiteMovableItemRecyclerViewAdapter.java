package cz.fungisoft.coffeecompass2.activity.support;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovableListContent;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.LocationService;
import cz.fungisoft.coffeecompass2.ui.fragments.CoffeeSiteDetailFragment;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Adapter to show found list of {@link cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable}.
 * Is capable to react on events defined by CoffeeSiteMovable,
 * especially the distance change event of any of the CoffeeSiteMovable
 * item in the list.
 */
public class CoffeeSiteMovableItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                                                      implements PropertyChangeListener
{
    private static final String TAG = "CoffeeSiteListAdapter";

    private final FoundCoffeeSitesListActivity mParentActivity;
    private List<CoffeeSiteMovable> mValues;
    private CoffeeSiteMovableListContent content;

    private final boolean mTwoPane;

    private View.OnClickListener mOnClickListener;
    //private static LocationService mLocationService;

    private int currentSearchRange;

    private static CoffeeSiteMovable dummyEmptyListCoffeeSite = new CoffeeSiteMovable(0, "Dummy", 0);

    // Animations for vyhledavam text
    private AlphaAnimation animation1;
    private AlphaAnimation animation2;

    private String searchingDistanceLabel;

    /**
     * Checks if the order of CoffeeSiteMovable items in the list needs 're-ordering'<br>
     * caused by new distance event detected within one of the CoffeeSiteMovable in the list.<br>
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
            // and notify RecyclerView to update items
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
     * Inserts newly detected sites in the searched range.
     *
     * @param newSites
     */
    public void insertNewSites(List<CoffeeSiteMovable> newSites) {

        // If there was 'Empty list card' shown, remove it
        if (mValues.size() >= 1 && mValues.get(0).getName().equals("Dummy")) {
            mValues.remove(0);
            this.notifyItemRemoved(0);
        }
        // Go from top, and find first coffeeSite which distance is bigger then new site. Insert into it's position
        for (CoffeeSiteMovable csmToInsert : newSites) {
            int posToInsert = -1;
            for (int i = 0; i < mValues.size(); i++) {
                if ((csmToInsert.getDistance() < mValues.get(i).getDistance()) ) {
                    posToInsert = i;
                    break;
                }
            }
            if ( posToInsert == - 1) { // add to the end of current list
                posToInsert = mValues.size();
            }

            mValues.add(posToInsert, csmToInsert);
            this.notifyItemInserted(posToInsert);
        }
    }

    /**
     * Removes coffeeSites which are no longer in the search range.
     *
     * @param oldSites
     */
    public void removeOldSites(List<CoffeeSiteMovable> oldSites) {
        //Go through all current sites and remove sites being out of range
        for (CoffeeSiteMovable csmToRemove : oldSites) {
            for (int i = mValues.size() - 1; i >= 0  ; i--) {
                if (mValues.get(i).getId() == csmToRemove.getId()) {
                    mValues.remove(i);
                    this.notifyItemRemoved(i);
                    break;
                }
            }
        }

        // If the list is empty now, insert an 'Empty card'
        if (mValues.size() == 0) {
            mValues.add(0, dummyEmptyListCoffeeSite);
            this.notifyItemInserted(0);
        }
    }


    /**
     * Standard constructor of the class MyCoffeeSiteItemRecyclerViewAdapter
     *
     * @param parent - parent Activity for the Adapter, in this case this FoundCoffeeSitesListActivity
     * @param content - instance of the CoffeeSiteMovableListContent to be displayed by this activity
     * @param twoPane
     */
    public CoffeeSiteMovableItemRecyclerViewAdapter(FoundCoffeeSitesListActivity parent,
                                                    CoffeeSiteMovableListContent content,
                                                    int currentSearchRange,
                                                    //LocationService locationService,
                                                    boolean twoPane) {
        this.content = content;
        this.currentSearchRange = currentSearchRange;
        mValues = this.content.getItems();
        mParentActivity = parent;
        mTwoPane = twoPane;
        //mLocationService = locationService;

        searchingDistanceLabel = mParentActivity.getResources().getString(R.string.current_range);

        mOnClickListener = createOnClickListener();

        if (mValues.size() == 0) {
            mValues.add(0, dummyEmptyListCoffeeSite );
            this.notifyItemInserted(0);
        }
    }

    private View.OnClickListener createOnClickListener() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSiteMovable item = (CoffeeSiteMovable) view.getTag();
                if (mTwoPane) {
                    // Open CoffeeSiteDetailFragment, if the item is clicked and there is
                    // landscape orientation
                    // to show details of the CoffeeSiteMovable holding this item
                    Bundle arguments = new Bundle();
                    arguments.putString(CoffeeSiteDetailFragment.ARG_ITEM_ID, Long.toString(item.getId()));
                    CoffeeSiteDetailFragment fragment = new CoffeeSiteDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.coffeesite_detail_container, fragment)
                            .commit();
                } else {
                    // Open CoffeeSiteDetailActivity if the item is clicked
                    // to show details of the CoffeeSiteMovable holding this item
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);
                    intent.putExtra("coffeeSite", (Parcelable) item);
                    context.startActivity(intent);
                }
            }
        };
        return retVal;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder retVal = null;

        switch (viewType) {
            // Case 0 for standard View with any CoffeeSiteMovable in the list available
            case 0:
                View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.coffeesite_list_content, parent, false);
                retVal = new CoffeeSiteMovableItemRecyclerViewAdapter.ViewHolder1(view);
                break;
            // Case -1 for 'Empty card'
            case -1:
                View view2 = LayoutInflater.from(parent.getContext())
                             .inflate(R.layout.coffeesite_list_emptycard, parent, false);
                retVal = new CoffeeSiteMovableItemRecyclerViewAdapter.EmptyCardViewHolder(view2);
                break;
        }
        return retVal;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()) {
            // Case 0 for standard View with any CoffeeSiteMovable in the list available
            case 0:
                ViewHolder1 viewHolder1 = (ViewHolder1) viewHolder;
                setupBasicViewHolder(position, viewHolder1);
                break;
            // Case -1 for 'Empty card'
            case -1:
                EmptyCardViewHolder emptyCardViewHolder = (EmptyCardViewHolder) viewHolder;
                setupEmptyCardViewHolder(emptyCardViewHolder);
                break;
        }
    }

    /**
     * Returns itemVIewType to distinquish if the list of mValues contains standard items i.e.
     * CoffeeSiteMovable or if it contains only 'Empty card' informing that the standard list
     * is empty.
     * Type 0 for standard View i.e. CoffeeSiteMovable in the list available
     * Type -1 for 'Empty card' i.e. standard list is empty
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        int retViewType = 0;
        if (mValues.size() == 1 && mValues.get(0).getName().equals("Dummy")) {
            retViewType = -1;
        }
        return retViewType;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void setupEmptyCardViewHolder(final EmptyCardViewHolder viewHolder) {

        viewHolder.currentDistanceEmptyCardView.setText(searchingDistanceLabel + this.currentSearchRange +  " m");

        // Animation of the label indicating system is alive and searching for new locations
        animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(1600);
        animation1.setStartOffset(200);

        animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(500);
        animation2.setStartOffset(800);

        // Animation of the searchingInfoLabel
        animation1.setAnimationListener(new Animation.AnimationListener(){

            @Override
            public void onAnimationEnd(Animation arg0) {
                // start animation2 when animation1 ends (continue)
                viewHolder.searchingInfoLabel.startAnimation(animation2);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub
            }
        });

        //animation2 AnimationListener
        animation2.setAnimationListener(new Animation.AnimationListener(){

            @Override
            public void onAnimationEnd(Animation arg0) {
                // start animation1 when animation2 ends (repeat)
                viewHolder.searchingInfoLabel.startAnimation(animation1);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub
            }
        });

        viewHolder.searchingInfoLabel.startAnimation(animation1);
    }

    private void setupBasicViewHolder(int position, ViewHolder1 viewHolder1) {

        viewHolder1.csNameView.setText(this.mValues.get(position).getName());
        viewHolder1.locAndTypeView.setText(this.mValues.get(position).getTypPodniku() + ", " +  this.mValues.get(position).getTypLokality());
        viewHolder1.coffeeSortView.setText(this.mValues.get(position).getCoffeeSortsOneString());

        //viewHolder1.distanceView.setText(this.mValues.get(position).getDistance() + " m");
        viewHolder1.distanceView.setText(Utils.getDistanceInBetterReadableForm(this.mValues.get(position).getDistance()));
        viewHolder1.distanceView.setCoffeeSite(this.mValues.get(position));
        viewHolder1.distanceView.setTag(TAG + ". DistanceTextView for " + this.mValues.get(position).getName());

        this.mValues.get(position).addPropertyChangeListener(viewHolder1.distanceView);
        Log.d(TAG, ". Distance Text View " + viewHolder1.distanceView.getTag() + " added to listen distance change of " + this.mValues.get(position).getName() + ". Object id: " + this.mValues.get(position));

        if (!this.mValues.get(position).getMainImageURL().isEmpty()) {
            Picasso.get().load(this.mValues.get(position).getMainImageURL()).into(viewHolder1.siteFoto);
        }

        viewHolder1.itemView.setTag(this.mValues.get(position));
        viewHolder1.itemView.setOnClickListener(this.mOnClickListener);
    }

        /**
         * Inner ViewHolder class for MyCoffeeSiteItemRecyclerViewAdapter
         */
        class ViewHolder1 extends RecyclerView.ViewHolder {

            final TextView csNameView; // to show name of CoffeeSite
            final TextView locAndTypeView; // to show type of the CoffeeSite and location type
            final TextView coffeeSortView; // to show available sorts of coffee on this CoffeeSite
            final DistanceChangeTextView distanceView; // to show distance attribute of the CoffeeSite

            final ImageView siteFoto;

            /**
             * Standard constructor for ViewHolder.
             *
             * @param view
             */
            ViewHolder1(View view) {
                super(view);
                csNameView = (TextView) view.findViewById(R.id.csNameTextView);
                locAndTypeView = (TextView) view.findViewById(R.id.locAndTypeTextView);
                coffeeSortView = (TextView) view.findViewById(R.id.coffeeSortsTextView);
                distanceView = (DistanceChangeTextView) view.findViewById(R.id.csDistanceTextView);
                siteFoto = (ImageView) view.findViewById(R.id.csListFotoImageView);
            }
        }

        /**
         * Inner ViewHolder class for 'EmptyCard'
         */
        class EmptyCardViewHolder extends RecyclerView.ViewHolder {

            final TextView currentDistanceEmptyCardView;
            final TextView searchingInfoLabel;
            final CardView emptyCardView;

            /**
             * Standard constructor for ViewHolder.
             *
             * @param view
             */
            EmptyCardViewHolder(View view) {
                super(view);

                emptyCardView = view.findViewById(R.id.list_empty_card);
                currentDistanceEmptyCardView = view.findViewById(R.id.currentRangeTextView);
                searchingInfoLabel = view.findViewById(R.id.stillSearchingTextView);
            }
        }

}
