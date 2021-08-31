package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.support.DistanceChangeTextView;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSitesInRangeUpdateListener;
import cz.fungisoft.coffeecompass2.activity.ui.fragments.CoffeeSiteDetailFragment;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * Adapter to show found list of {@link cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable}.
 * Is capable to react on events defined by CoffeeSiteMovable,
 * especially the distance change event of any of the CoffeeSiteMovable
 * item in the list.
 */
public class FoundCoffeeSitesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                                                 implements PropertyChangeListener,
                                                            CoffeeSitesInRangeUpdateListener {

    private static final String TAG = "CoffeeSiteListAdapter";

    private final FoundCoffeeSitesListActivity mParentActivity;


    private final List<CoffeeSiteMovable> mValues = new ArrayList<>();

    private final boolean mTwoPane;

    private final View.OnClickListener mOnClickListener;
    private final View.OnClickListener mOnImageClickListener;

    private final int currentSearchRange;

    private static final CoffeeSiteMovable dummyEmptyListCoffeeSite = new CoffeeSiteMovable(0, "Dummy", 0);
    private static final CoffeeSiteMovable initialDummyEmptyListCoffeeSite = new CoffeeSiteMovable(0, "InitialDummy", 0);

    // Animations for vyhledavam text
    private AlphaAnimation animation1;
    private AlphaAnimation animation2;

    private final String searchingDistanceLabel;

    private EmptyCardViewHolder emptyCardViewHolder;

    /**
     * Indicates, that searching of CoffeeSites is in progress
     */
    private boolean searchingInProgress = false;
    /**
     * Indicates, that animation "vyhledavam" on EmptyCardViewHolder is in progress
     */
    private boolean animationRunning = false;


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
    @Override
    public void onNewSitesInRange(List<CoffeeSiteMovable> newSites) {
        // Listen to location change to allow correct sorting according distance
        for (CoffeeSiteMovable csm : newSites) {
            csm.addPropertyChangeListener(this);
        }

        // If there are new CoffeeSites and there was 'Empty list card' or InitialDummy shown, remove it
        if (newSites.size() > 0 && mValues.size() >= 1
                && (mValues.get(0).getName().equals("Dummy") || mValues.get(0).getName().equals("InitialDummy"))) {
            mValues.remove(0);
            this.notifyItemRemoved(0);
        }

        // Go from top, and find first coffeeSite which distance is bigger then new site. Insert into it's position
        for (CoffeeSiteMovable csmToInsert : newSites) {
            int posToInsert = -1;
            for (int i = 0; i < mValues.size(); i++) {
                if ((csmToInsert.getDistance() < mValues.get(i).getDistance())) {
                    posToInsert = i;
                    break;
                }
            }
            if (posToInsert == -1) { // add to the end of current list
                posToInsert = mValues.size();
            }

            mValues.add(posToInsert, csmToInsert);
            this.notifyItemInserted(posToInsert);
        }
    }

    @Override
    public void onNewSitesInRangeError(String error) {
    }

    /**
     * Removes coffeeSites which are no longer in the search range.
     *
     * @param oldSites
     */
    @Override
    public void onSitesOutOfRange(List<CoffeeSiteMovable> oldSites, boolean searchInRange) {
        // Go through all current sites and remove sites being out of range
        for (CoffeeSiteMovable csmToRemove : oldSites) {
            csmToRemove.removePropertyChangeListener(this);

            for (int i = mValues.size() - 1; i >= 0  ; i--) {
                if (mValues.get(i).getId() == csmToRemove.getId()) {
                    mValues.remove(i);
                    this.notifyItemRemoved(i);
                    break;
                }
            }
        }

        // If the list is empty now, insert standard 'Empty card'
        if (mValues.size() == 0 && searchInRange) {
            mValues.add(0, dummyEmptyListCoffeeSite);
            this.notifyItemInserted(0);
        }
    }


    public int getCurrentNumberOfSitesShown() {
        if (mValues.size() == 1 && (mValues.get(0).getName().equals("Dummy") || mValues.get(0).getName().equals("InitialDummy"))) {
            return 0;
        } else {
            return mValues.size();
        }
    }

    /**
     * Standard constructor of the class MyCoffeeSiteItemRecyclerViewAdapter
     *
     * @param parent - parent Activity for the Adapter, in this case this FoundCoffeeSitesListActivity
     * @param content - instance of the CoffeeSiteMovableListContent to be displayed by this activity
     * @param twoPane - not used probably
     */
    public FoundCoffeeSitesRecyclerViewAdapter(FoundCoffeeSitesListActivity parent,
                                               int currentSearchRange, boolean twoPane) {
        this.currentSearchRange = currentSearchRange;
        mParentActivity = parent;
        mTwoPane = twoPane;

        searchingDistanceLabel = mParentActivity.getResources().getString(R.string.current_range_label);
        mOnClickListener = createOnClickListener();
        mOnImageClickListener = createOnClickListenerForShowImageActivityStart();

        mValues.add(0, initialDummyEmptyListCoffeeSite);
        this.notifyItemInserted(0);
    }

    /**
     * OnClick listener to open CoffeeSiteDetailActivity to show details
     * of the CoffeeSite as it is shown when searching and looking into the details.
     *
     * @return
     */
    private View.OnClickListener createOnClickListener() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSiteMovable siteMovable = (CoffeeSiteMovable) view.getTag();
                if (mTwoPane) {
                    // Open CoffeeSiteDetailFragment, if the siteMovable is clicked and there is
                    // landscape orientation to show details of the CoffeeSiteMovable holding this siteMovable
                    Bundle arguments = new Bundle();
                    arguments.putString(CoffeeSiteDetailFragment.ARG_ITEM_ID, Long.toString(siteMovable.getId()));
                    CoffeeSiteDetailFragment fragment = new CoffeeSiteDetailFragment();
                    fragment.setArguments(arguments);
                } else {
                    // Open CoffeeSiteDetailActivity if the siteMovable is clicked
                    // to show details of the CoffeeSiteMovable holding this siteMovable
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);
                    intent.putExtra("coffeeSite", (Parcelable) siteMovable);
                    context.startActivity(intent);
                }
            }
        };
        return retVal;
    }

    /**
     * OnClick listener to open CoffeeSiteImageActivity to show picture
     * of the CoffeeSite as it is shown when searching and looking into the details.
     *
     * @return
     */
    private View.OnClickListener createOnClickListenerForShowImageActivityStart() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoffeeSite coffeeSite = (CoffeeSite) view.getTag();
                if (coffeeSite != null) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CoffeeSiteDetailActivity.class);
                    intent.putExtra("coffeeSite", (Parcelable) coffeeSite);
                    // unfortunately, when the image is first in details fragment, it's height is not alligned with details fragment height
                    intent.putExtra("showImageFirst", false);
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
                retVal = new FoundCoffeeSitesRecyclerViewAdapter.ViewHolder1(view);
                break;
            // Case -1 for 'Empty card'
            case -1:
                View view2 = LayoutInflater.from(parent.getContext())
                             .inflate(R.layout.coffeesite_list_emptycard, parent, false);
                retVal = new FoundCoffeeSitesRecyclerViewAdapter.EmptyCardViewHolder(view2);
                break;
            // Case -2 for 'Initial Empty card'
            case -2:
                View view3 = LayoutInflater.from(parent.getContext())
                             .inflate(R.layout.coffeesite_list_emptycard_initial_search, parent, false);
                retVal = new FoundCoffeeSitesRecyclerViewAdapter.InitialSearchingCardViewHolder(view3);
                break;
        }
        return retVal;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()) {
            // Case 0 for standard View with any CoffeeSiteMovable in the list available
            case 0:
                ViewHolder1 basicViewHolder = (ViewHolder1) viewHolder;
                setupBasicViewHolder(position, basicViewHolder);
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
     * Clears list of current CoffeeSites. Used when new searching in town starts.
     */
    public void clearList() {
        mValues.clear();
    }


    /**
     * Start animation of text on Empty card, when new searching of CoffeeSites started again
     */
    public void newSitesSearchingStarted() {
        searchingInProgress = true;
        if (emptyCardViewHolder != null &&
                mValues.size() == 1 && (mValues.get(0).getName().equals("Dummy"))) {
            if (!animationRunning) {
                emptyCardViewHolder.searchingInfoLabel.setText(R.string.still_searching);
                emptyCardViewHolder.searchingInfoLabel.startAnimation(animation1);
            }
        }
    }

    /**
     * Finish animations or replace Initial Empty card by standard Dummy card.
     * Applicable when searching in location/range, not in town
     */
    public void newSitesSearchingFinished() {
        searchingInProgress = false;
        if (emptyCardViewHolder != null &&
                mValues.size() == 1 && (mValues.get(0).getName().equals("Dummy"))) {
            if (!animationRunning) {
                emptyCardViewHolder.searchingInfoLabel.setText(R.string.searching_will_start_after_move_message);
            }
        }

        // If there is no newSites returned, after Initial Empty card was shown, show Standard Empty card
        // if this was searching in location/range
        if (mValues.size() == 1 && mValues.get(0).getName().equals("InitialDummy")) {
            // Remove Initial Empty card
            mValues.remove(0);
            this.notifyItemRemoved(0);
            // Replace by Standard Empty card
            mValues.add(0, dummyEmptyListCoffeeSite);
            this.notifyItemInserted(0);
        }
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
                viewHolder.searchingInfoLabel.startAnimation(animation2);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationStart(Animation arg0) {
                animationRunning = true;
                emptyCardViewHolder.searchingInfoLabel.setText(R.string.still_searching);
            }
        });

        //animation2 AnimationListener
        animation2.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                // start animation1 when animation2 ends (repeat)
                if (searchingInProgress) {
                    viewHolder.searchingInfoLabel.startAnimation(animation1);
                } else {
                    animationRunning = false;
                    emptyCardViewHolder.searchingInfoLabel.setText(R.string.searching_will_start_after_move_message);
                }
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationStart(Animation arg0) {}

        });
        animationRunning = false; // init value
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
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationStart(Animation arg0) {}

        });

        animation2.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                // start animation1 when animation2 ends (repeat)
                viewHolder.searchingInfoLabel.startAnimation(animation1);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationStart(Animation arg0) {}

        });

        viewHolder.searchingInfoLabel.startAnimation(animation1);
    }

    /**
     * Setup of ViewHolder holding info about found CoffeeSite
     *
     * @param position
     * @param viewHolder
     */
    private void setupBasicViewHolder(int position, ViewHolder1 viewHolder) {

        viewHolder.csNameView.setText(this.mValues.get(position).getName());
        viewHolder.locAndTypeView.setText(this.mValues.get(position).getTypPodniku() + ", " +  this.mValues.get(position).getTypLokality());
        viewHolder.coffeeSortView.setText(this.mValues.get(position).getCoffeeSortsOneString());

        viewHolder.distanceView.setText(Utils.getDistanceInBetterReadableForm(this.mValues.get(position).getDistance()));
        viewHolder.distanceView.setCoffeeSite(this.mValues.get(position));
        this.mValues.get(position).addPropertyChangeListener(viewHolder.distanceView);
        viewHolder.distanceView.setTag(TAG + ". DistanceTextView for " + this.mValues.get(position).getName());
        Log.d(TAG, ". Distance Text View " + viewHolder.distanceView.getTag() + " added to listen distance change of " + this.mValues.get(position).getName() + ". Object id: " + this.mValues.get(position));

        boolean isOnline = Utils.isOnline(mParentActivity.getApplicationContext());
        if (isOnline && !this.mValues.get(position).getMainImageURL().isEmpty()) {
            Picasso.get().load(this.mValues.get(position).getMainImageURL())
                    .fit().placeholder(R.drawable.kafe_backround_120x160)
                    .into(viewHolder.siteFoto);
        } else {
            Picasso.get().load(ImageUtil.getImageFile(mParentActivity.getApplicationContext(), this.mValues.get(position).getMainImageFilePath()))
                    .fit().placeholder(R.drawable.kafe_backround_120x160)
                    .into(viewHolder.siteFoto);
        }

        viewHolder.itemView.setTag(this.mValues.get(position));
        viewHolder.itemView.setOnClickListener(this.mOnClickListener);
        // SiteFoto has its own listener
        viewHolder.siteFoto.setOnClickListener(this.mOnImageClickListener);
        viewHolder.siteFoto.setTag(this.mValues.get(position));

        // Needs to be reseted, when this holder creation interupts animation of the EmptyCardViewHolder,
        // so it cannot be reseted by animation itself
        animationRunning = false;
    }



        /**
         * Inner ViewHolder class for MyCoffeeSiteItemRecyclerViewAdapter
         */
        class ViewHolder1 extends RecyclerView.ViewHolder {

            final LinearLayout csDataView; // to assign listener different from listener assigned to siteFoto
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
                csDataView = (LinearLayout) view.findViewById(R.id.found_cs_data_linearview);
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
