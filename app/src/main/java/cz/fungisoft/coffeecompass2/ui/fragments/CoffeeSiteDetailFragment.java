package cz.fungisoft.coffeecompass2.ui.fragments;

import android.app.Activity;
import android.os.Bundle;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.CoffeeSiteListActivity;
import cz.fungisoft.coffeecompass2.activity.support.DistanceChangeTextView;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;

/**
 * A fragment representing a single CoffeeSite detail screen.
 * This fragment is either contained in a {@link CoffeeSiteListActivity}
 * in two-pane mode (on tablets) or a {@link CoffeeSiteDetailActivity}
 * on handsets.
 */
public class CoffeeSiteDetailFragment extends Fragment {

    private static final String TAG = "CoffeeSiteDetailFrag";
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The object this fragment is presenting.
     */
    private CoffeeSiteMovable mItem;

    private DistanceChangeTextView distanceTextView;

    private CoffeeSiteListContent content;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CoffeeSiteDetailFragment() {
    }

    public void setCoffeeSiteListContent(CoffeeSiteListContent content) {
        this.content = content;
        mItem = content.getItemsMap().get(getArguments().getString(ARG_ITEM_ID));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = content.getItemsMap().get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getName());
            }
        }
    }

    @Override
    public void onStop() {
        mItem.removePropertyChangeListener(distanceTextView);
        super.onStop();
    }

    @Override
    public void onResume() {
        if (distanceTextView != null) {
            distanceTextView.setText(String.valueOf(mItem.getDistance()) + " m");
        }
        super.onResume();
    }

    @Override
    public void onStart() {
        if (distanceTextView != null && mItem != null) {
            distanceTextView.setText(String.valueOf(mItem.getDistance()) + " m");
            mItem.addPropertyChangeListener(distanceTextView);
        }
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.coffeesite_detail_fragment, container, false);

        // Show the CoffeeSite info in a TextViews.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.nameTextView)).setText(mItem.getName());
            ((TextView) rootView.findViewById(R.id.statusZarizeniTextView)).setText(mItem.getStatusZarizeni());
            ((TextView) rootView.findViewById(R.id.siteTypeTextView)).setText(mItem.getTypPodniku());
            ((TextView) rootView.findViewById(R.id.locationTypeTextView)).setText(mItem.getTypLokality());

            if (mItem.getCupTypes().length() > 0) {
                ((TextView) rootView.findViewById(R.id.cupTypeTextView)).setText(mItem.getCupTypes());
            } else {
                rootView.findViewById(R.id.cupTableRow).setVisibility(View.GONE);
            }

            ((TextView) rootView.findViewById(R.id.cenaTextView)).setText(mItem.getCena());

            if (mItem.getNextToMachineTypes().length() > 0) {
                ((TextView) rootView.findViewById(R.id.nextToMachineOfferTextView)).setText(mItem.getNextToMachineTypes());
            } else {
                rootView.findViewById(R.id.nextToMachineTableRow).setVisibility(View.GONE);
            }

            if (mItem.getOtherOffers().length() > 0) {
                ((TextView) rootView.findViewById(R.id.otherOfferTextView)).setText(mItem.getOtherOffers());
            } else {
                rootView.findViewById(R.id.nextOfferTableRow).setVisibility(View.GONE);
            }

            if (mItem.getCoffeeSorts().length() > 0) {
                ((TextView) rootView.findViewById(R.id.coffeeSortTextView)).setText(mItem.getCoffeeSorts());
            } else {
                rootView.findViewById(R.id.coffeeSortTableRow).setVisibility(View.GONE);
            }

            if (!mItem.getUliceCP().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.streetTextView)).setText(mItem.getUliceCP());
            } else {
                rootView.findViewById(R.id.streetTableRow).setVisibility(View.GONE);
            }

            if (!mItem.getOteviraciDobaHod().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.openingTextView)).setText(mItem.getOteviraciDobaDny() + ", " + mItem.getOteviraciDobaHod());
            } else {
                rootView.findViewById(R.id.openingTableRow).setVisibility(View.GONE);
            }

            ((TextView) rootView.findViewById(R.id.hodnoceniTextView)).setText(mItem.getHodnoceni());

            ((TextView) rootView.findViewById(R.id.createdByUserTextView)).setText(mItem.getCreatedByUser());
            ((TextView) rootView.findViewById(R.id.createdOnTextView)).setText(mItem.getCreatedOnString());

            if (!mItem.getUvodniKoment().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.initialCommentTextView)).setText(mItem.getUvodniKoment());
            } else {
                rootView.findViewById(R.id.authorCommentTableRow).setVisibility(View.GONE);
            }

            distanceTextView = (DistanceChangeTextView) rootView.findViewById(R.id.distanceTextView);
            distanceTextView.setText(String.valueOf(mItem.getDistance()) + " m");
            distanceTextView.setTag(TAG + ". DistanceTextView for " + mItem.getName());
            distanceTextView.setCoffeeSite(mItem);
            mItem.addPropertyChangeListener(distanceTextView);
        }

        return rootView;
    }

}
