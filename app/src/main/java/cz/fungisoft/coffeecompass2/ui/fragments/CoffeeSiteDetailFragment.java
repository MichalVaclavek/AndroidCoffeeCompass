package cz.fungisoft.coffeecompass2.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.activity.support.DistanceChangeTextView;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * A fragment representing a single CoffeeSite detail screen.
 * This fragment is either contained in a {@link FoundCoffeeSitesListActivity}
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
    private CoffeeSite mItem;

    private DistanceChangeTextView distanceTextView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CoffeeSiteDetailFragment() {
    }

    public void setCoffeeSite(CoffeeSite csm) {
       mItem = csm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        if (mItem instanceof  CoffeeSiteMovable) {
            ((CoffeeSiteMovable)mItem).removePropertyChangeListener(distanceTextView);
        }
        super.onStop();
    }


    @Override
    public void onStart() {
        if (distanceTextView != null && mItem != null
        && mItem instanceof  CoffeeSiteMovable) {
            ((CoffeeSiteMovable)mItem).addPropertyChangeListener(distanceTextView);
            distanceTextView.setText(Utils.getDistanceInBetterReadableForm(mItem.getDistance()));
        }
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.coffeesite_detail_fragment, container, false);

        // Show the CoffeeSite info in a TextViews.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.nameTextView)).setText(mItem.getName());
            ((TextView) rootView.findViewById(R.id.statusZarizeniTextView)).setText(mItem.getStatusZarizeni().toString());
            ((TextView) rootView.findViewById(R.id.siteTypeTextView)).setText(mItem.getTypPodniku().toString());
            ((TextView) rootView.findViewById(R.id.locationTypeTextView)).setText(mItem.getTypLokality().toString());

            if (mItem.getCupTypes().size() > 0) {
                ((TextView) rootView.findViewById(R.id.cupTypeTextView)).setText(mItem.getCupTypesOneString());
            } else {
                rootView.findViewById(R.id.cupTableRow).setVisibility(View.GONE);
            }

            if (mItem.getCena() != null) {
                ((TextView) rootView.findViewById(R.id.cenaTextView)).setText(mItem.getCena().toString());
            }

            if (mItem.getNextToMachineTypes().size() > 0) {
                ((TextView) rootView.findViewById(R.id.nextToMachineOfferTextView)).setText(mItem.getNextToMachineTypesOneString());
            } else {
                rootView.findViewById(R.id.nextToMachineTableRow).setVisibility(View.GONE);
            }

            if (mItem.getOtherOffers().size() > 0) {
                ((TextView) rootView.findViewById(R.id.otherOfferTextView)).setText(mItem.getOtherOffersOneString());
            } else {
                rootView.findViewById(R.id.nextOfferTableRow).setVisibility(View.GONE);
            }

            if (mItem.getCoffeeSorts().size() > 0) {
                ((TextView) rootView.findViewById(R.id.coffeeSortTextView)).setText(mItem.getCoffeeSortsOneString());
            } else {
                rootView.findViewById(R.id.coffeeSortTableRow).setVisibility(View.GONE);
            }

            if (!mItem.getUliceCP().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.streetTextView)).setText(mItem.getUliceCP());
            } else {
                rootView.findViewById(R.id.streetTableRow).setVisibility(View.GONE);
            }

            if (!mItem.getOteviraciDobaDny().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.openingTextView)).setText(mItem.getOteviraciDobaDny());
            }
            if (!mItem.getOteviraciDobaHod().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.openingTextView)).setText(mItem.getOteviraciDobaHod());
            }
            if (!mItem.getOteviraciDobaHod().isEmpty() && !mItem.getOteviraciDobaDny().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.openingTextView)).setText(mItem.getOteviraciDobaDny() + ", " + mItem.getOteviraciDobaHod());
            } else {
                rootView.findViewById(R.id.openingTableRow).setVisibility(View.GONE);
            }

            if (mItem.getHodnoceni() != null && !mItem.getHodnoceni().toString().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.hodnoceniTextView)).setText(mItem.getHodnoceni().toString());
            }  else {
                rootView.findViewById(R.id.hodnoceni_tablerow).setVisibility(View.GONE);
            }

            ((TextView) rootView.findViewById(R.id.createdByUserTextView)).setText(mItem.getCreatedByUserName());
            ((TextView) rootView.findViewById(R.id.createdOnTextView)).setText(mItem.getCreatedOnString());

            if (mItem.getUvodniKoment() != null && !mItem.getUvodniKoment().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.initialCommentTextView)).setText(mItem.getUvodniKoment());
            } else {
                rootView.findViewById(R.id.authorCommentTableRow).setVisibility(View.GONE);
            }

            TableRow distanceTableRow = rootView.findViewById(R.id.cs_detail_distance_row);

            if (mItem instanceof  CoffeeSiteMovable) {
                distanceTableRow.setVisibility(View.VISIBLE);

                distanceTextView = (DistanceChangeTextView) rootView.findViewById(R.id.distanceTextView);
                distanceTextView.setText(Utils.getDistanceInBetterReadableForm(mItem.getDistance()));
                distanceTextView.setTag(TAG + ". DistanceTextView for " + mItem.getName());

                distanceTextView.setCoffeeSite((CoffeeSiteMovable) mItem);
                //((CoffeeSiteMovable)mItem).addPropertyChangeListener(distanceTextView);
            } else {
                distanceTableRow.setVisibility(View.GONE);
            }
        }

        return rootView;
    }

}
