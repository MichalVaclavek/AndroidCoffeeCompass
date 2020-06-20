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
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovableListContent;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.ui.fragments.CoffeeSiteDetailFragment;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Adapter to show found list of {@link cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable}.
 * Is capable to react on events defined by CoffeeSiteMovable,
 * especially the distance change event of any of the CoffeeSiteMovable
 * item in the list.
 */
public class CoffeeSiteMovableItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                                                      implements PropertyChangeListener {

    private static final String TAG = "CoffeeSiteListAdapter";

    private final FoundCoffeeSitesListActivity mParentActivity;
    private List<CoffeeSiteMovable> mValues = new ArrayList<>();
    private CoffeeSiteMovableListContent content;

    private final boolean mTwoPane;

    private View.OnClickListener mOnClickListener;

    private int currentSearchRange;

    private static CoffeeSiteMovable dummyEmptyListCoffeeSite = new CoffeeSiteMovable(0, "Dummy", 0);
    private static CoffeeSiteMovable initialDummyEmptyListCoffeeSite = new CoffeeSiteMovable(0, "InitialDummy", 0);

    // Animations for vyhledavam text
    private AlphaAnimation animation1;
    private AlphaAnimation animation2;

    private String searchingDistanceLabel;

    private EmptyCardViewHolder emptyCardViewHolder;

    final int[] searchingTextAnimationsCounter = {0};

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
            // 1. Find an item which is not in right position
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
     * Inserts newly detected sites (in the searched range) into the current list
     * of found coffee sites.
     *
     * @param newSites
     */
    public void insertNewSites(List<CoffeeSiteMovable> newSites) {

        // If there is no newSites returned, after initial Empty card was shown, show standard Empty card
        if (newSites.size() == 0 && mValues.size() == 1 && mValues.get(0).getName().equals("InitialDummy")) {
            // Remove Initial Empty card
            mValues.remove(0);
            this.notifyItemRemoved(0);
            // Replace by standard Empty card
            mValues.add(0, dummyEmptyListCoffeeSite);
            this.notifyItemInserted(0);
        }

        // If there are new CoffeeSites and there was 'Empty list card' shown, remove it
        if (newSites.size() > 0 && mValues.size() >= 1
                && (mValues.get(0).getName().equals("Dummy") || mValues.get(0).getName().equals("InitialDummy"))) {
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

        // If the list is empty now, insert standard 'Empty card'
        if (mValues.size() == 0) {
            mValues.add(0, dummyEmptyListCoffeeSite);
            this.notifyItemInserted(0);
        }
    }


    public int getCurrentNumberOfSitesShown() {
        if (mValues != null && mValues.size() == 1 && (mValues.get(0).getName().equals("Dummy") || mValues.get(0).getName().equals("InitialDummy"))) {
            return 0;
        } else {
            return mValues.size();
        }
    }

    /**
     * Returns current CoffeeSitesMovable shown by the Adapter
     * @return current CoffeeSitesMovable shown by the Adapter
     */
    public List<CoffeeSiteMovable> getShownItems() {
        if (mValues != null && mValues.size() >= 1
                && (!mValues.get(0).getName().equals("Dummy") || !mValues.get(0).getName().equals("InitialDummy"))) {
            return mValues;
        } else {
            return new ArrayList<>();
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
                                                    boolean twoPane) {
        this.content = content;
        this.currentSearchRange = currentSearchRange;
        mParentActivity = parent;
        mTwoPane = twoPane;

        searchingDistanceLabel = mParentActivity.getResources().getString(R.string.current_range_label);
        mOnClickListener = createOnClickListener();

        if (this.content != null) {
            mValues = this.content.getItems();

            if (mValues.size() == 0) {
                mValues.add(0, dummyEmptyListCoffeeSite);
                this.notifyItemInserted(0);
            }
        } else {
            mValues.add(0, initialDummyEmptyListCoffeeSite);
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
            // Case -2 for 'Initial Empty card'
            case -2:
                View view3 = LayoutInflater.from(parent.getContext())
                             .inflate(R.layout.coffeesite_list_emptycard_initial_search, parent, false);
                retVal = new CoffeeSiteMovableItemRecyclerViewAdapter.InitialSearchingCardViewHolder(view3);
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
                emptyCardViewHolder = (EmptyCardViewHolder) viewHolder;
                setupEmptyCardViewHolder(emptyCardViewHolder);
                break;
            // Case 1 for 'Initial Empty card'
            case -2:
                InitialSearchingCardViewHolder initialEmptySearchingCardViewHolder = (InitialSearchingCardViewHolder) viewHolder;
                setupInitialEmptySearchingCardViewHolder(initialEmptySearchingCardViewHolder);
                break;
        }
    }

    /**
     * Returns itemVIewType to distinquish if the list of mValues contains standard items i.e.
     * CoffeeSiteMovable or if it contains only 'Empty card' or 'InitialDummy' card to inform,
     * that the standard list is empty.
     *
     * Type 0 for standard View i.e. CoffeeSiteMovable in the list available
     * Type -1 for standard 'Empty card' i.e. standard list is empty
     * Type -2 for 'InitialDummy' i.e. CoffeeSites list is empty and searching of CoffeeSites just began
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        int retViewType = 0;
        if (mValues.size() == 1 && mValues.get(0).getName().equals("InitialDummy")) {
            retViewType = -2;
        }
        if (mValues.size() == 1 && mValues.get(0).getName().equals("Dummy")) {
            retViewType = -1;
        }
        return retViewType;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Creates view to show, that no CoffeeSite was found. This view card also contains
     * TextView which can be animated, when searching CoffeeSites if it starts again.
     * <p>
     * Animation is played 3 times, only to indicate, that something is happening - searching,
     * and if nothing is found, than animation must finish to indicate, that process of
     * searching is gone.
     *
     * @param viewHolder
     */
    private void setupEmptyCardViewHolder(final EmptyCardViewHolder viewHolder) {

        viewHolder.currentDistanceTextViewLabel.setText(searchingDistanceLabel);
        viewHolder.currentDistanceTextView.setText(getSearchingDistanceLabel(this.currentSearchRange));

        // Animation of the label indicating system is alive and searching for new locations
        animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(1600);
        animation1.setStartOffset(200);

        animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(500);
        animation2.setStartOffset(800);

        // Animation of the searchingInfoLabel
        animation1.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                // start animation2 when animation1 ends (continue)
                if (searchingTextAnimationsCounter[0] != 3) {
                    viewHolder.searchingInfoLabel.startAnimation(animation2);
                } else { // 3 animations played, show default message, reset counter end stop
                    searchingTextAnimationsCounter[0] = 0;
                    emptyCardViewHolder.searchingInfoLabel.setText(R.string.searching_will_start_after_move_message);
                }
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationStart(Animation arg0) {
                // TODO Auto-generated method stub
                searchingTextAnimationsCounter[0]++;
            }
        });

        //animation2 AnimationListener
        animation2.setAnimationListener(new Animation.AnimationListener() {

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

    }

    /**
     * Start animation of text on Empty card, when new searching of CoffeeSites started again
     */
    public void newSitesInRangeSearchingStarted() {
        if (emptyCardViewHolder != null &&
                mValues.size() == 1 && (mValues.get(0).getName().equals("Dummy")))    {
            emptyCardViewHolder.searchingInfoLabel.setText(R.string.still_searching);
            emptyCardViewHolder.searchingInfoLabel.setVisibility(View.VISIBLE);
            emptyCardViewHolder.searchingInfoLabel.startAnimation(animation1);
        }
    }

    /**
     * Not yet implemented as the AsyncTasks are not ready to support it.
     * @param numberOfSitesAlreadyRead
     */
    public void showNumberOfSitesAlreadyRead(int numberOfSitesAlreadyRead) {
        // Update label to show how many sites where already read when new reading of Sites in range
        if (mValues.size() == 1 && (mValues.get(0).getName().equals("Dummy") || mValues.get(0).getName().equals("InitialDummy")))  {
            //TODO - number of already read CoffeeSites on standard Empty or InitialDummy card
        }
    }

    /**
     * Creates view to show, that initial, first CoffeeSite searching is in progress by
     * animating {@code viewHolder.searchingInfoLabel} TextView
     *
     * @param viewHolder
     */
    private void setupInitialEmptySearchingCardViewHolder(final InitialSearchingCardViewHolder viewHolder) {

        viewHolder.currentDistanceTextViewLabel.setText(searchingDistanceLabel);
        viewHolder.currentDistanceTextView.setText(getSearchingDistanceLabel(this.currentSearchRange));

        // Animation of the label indicating system is alive and searching for new locations
        animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(1600);
        animation1.setStartOffset(200);

        animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(500);
        animation2.setStartOffset(800);

        // Animation of the searchingInfoLabel
        animation1.setAnimationListener(new Animation.AnimationListener() {

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

        animation2.setAnimationListener(new Animation.AnimationListener() {

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
             * Standard constructor for CoffeeSite's ViewHolder.
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
         * Inner ViewHolder class for standard 'EmptyCard'
         */
        class EmptyCardViewHolder extends RecyclerView.ViewHolder {

            final TextView currentDistanceTextViewLabel;
            final TextView currentDistanceTextView;
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
                currentDistanceTextViewLabel = view.findViewById(R.id.currentRangeLabelTextView);
                currentDistanceTextView = view.findViewById(R.id.current_distance_range_empty_card_textView);
                searchingInfoLabel = view.findViewById(R.id.stillSearchingTextView);
                searchingInfoLabel.setText(R.string.searching_will_start_after_move_message);
            }
        }

        /**
         * Inner ViewHolder class for 'Initial Empty card'
         */
        class InitialSearchingCardViewHolder extends RecyclerView.ViewHolder {

            final TextView currentDistanceTextViewLabel;
            final TextView currentDistanceTextView;
            final TextView searchingInfoLabel;
            final CardView emptyCardView;

            /**
             * Standard constructor for ViewHolder.
             *
             * @param view
             */
            InitialSearchingCardViewHolder(View view) {
                super(view);

                emptyCardView = view.findViewById(R.id.list_empty_card_initial_search);
                currentDistanceTextViewLabel = view.findViewById(R.id.current_range_initial_empty_card_label_TextView);
                currentDistanceTextView = view.findViewById(R.id.current_distance_range_initial_empty_card_textView);
                searchingInfoLabel = view.findViewById(R.id.initialSearchingTextView);
            }
        }


        private String getSearchingDistanceLabel(int searchRange) {
            return (searchRange >= 1000) ?  searchRange/1000 + " km"
                                         : searchRange + " m";
        }

}
