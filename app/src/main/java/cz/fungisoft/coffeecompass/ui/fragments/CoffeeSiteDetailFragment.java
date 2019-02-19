package cz.fungisoft.coffeecompass.ui.fragments;

import android.app.Activity;
import android.os.Bundle;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.fungisoft.coffeecompass.R;
import cz.fungisoft.coffeecompass.activity.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass.activity.CoffeeSiteListActivity;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;


/**
 * A fragment representing a single CoffeeSite detail screen.
 * This fragment is either contained in a {@link CoffeeSiteListActivity}
 * in two-pane mode (on tablets) or a {@link CoffeeSiteDetailActivity}
 * on handsets.
 */
public class CoffeeSiteDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The object this fragment is presenting.
     */
    private CoffeeSite mItem;

    private CoffeeSiteListContent content;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CoffeeSiteDetailFragment() {
    }

    public void setCoffeeSiteListContent(CoffeeSiteListContent content) {
        this.content = content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy name specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load name from a name provider.
            mItem = content.getItemsMap().get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.name);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.coffeesite_detail_fragment, container, false);

        // Show the CoffeeSite info in a TextViews.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.nameTextView)).setText(mItem.name);
            ((TextView) rootView.findViewById(R.id.statusZarizeniTextView)).setText(mItem.getStatusZarizeni());
            ((TextView) rootView.findViewById(R.id.siteTypeTextView)).setText(mItem.getTypPodniku());
            ((TextView) rootView.findViewById(R.id.locationTypeTextView)).setText(mItem.getTypLokality());
            ((TextView) rootView.findViewById(R.id.cupTypeTextView)).setText(mItem.getCupTypes());
            ((TextView) rootView.findViewById(R.id.cenaTextView)).setText(mItem.getCena());
            ((TextView) rootView.findViewById(R.id.nextToMachineOfferTextView)).setText(mItem.getNextToMachineTypes());
            ((TextView) rootView.findViewById(R.id.otherOfferTextView)).setText(mItem.getOtherOffers());
            ((TextView) rootView.findViewById(R.id.coffeeSortTextView)).setText(mItem.getCoffeeSorts());
            ((TextView) rootView.findViewById(R.id.streetTextView)).setText(mItem.getUliceCP());
            ((TextView) rootView.findViewById(R.id.distanceTextView)).setText(String.valueOf(mItem.distance) + " m");
            ((TextView) rootView.findViewById(R.id.openingTextView)).setText(mItem.getOteviraciDobaDny() + ", " + mItem.getOteviraciDobaHod());
            ((TextView) rootView.findViewById(R.id.hodnoceniTextView)).setText(mItem.getHodnoceni());
            ((TextView) rootView.findViewById(R.id.createdByUserTextView)).setText(mItem.getCreatedByUser());
            ((TextView) rootView.findViewById(R.id.initialCommentTextView)).setText(mItem.getUvodniKoment());
        }

        return rootView;
    }

}
