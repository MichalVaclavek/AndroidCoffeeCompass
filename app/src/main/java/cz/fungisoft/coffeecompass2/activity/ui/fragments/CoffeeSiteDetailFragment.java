package cz.fungisoft.coffeecompass2.activity.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.UsersCSRatingLoadOperationListener;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.activity.ui.comments.EnterCommentAndRatingDialogFragment;
import cz.fungisoft.coffeecompass2.asynctask.coffeesite.GetNumberOfStarsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.utils.Utils;

/**
 * A fragment representing a single CoffeeSite detail information list.
 * This fragment is either contained in a {@link CoffeeSiteDetailActivity} on handsets-mobile
 * (or should be in a {@link FoundCoffeeSitesListActivity} in two-pane mode (on tablets),
 * but currently only CoffeeSiteDetailActivity is used to show this fragment) )
 */
public class CoffeeSiteDetailFragment extends Fragment implements UsersCSRatingLoadOperationListener {

    private static final String TAG = "CoffeeSiteDetailFrag";

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The object this fragment is presenting.
     */
    private CoffeeSite coffeeSite;

    private ImageView editCoffeeSiteIcon;

    /**
     * We need to know current to determine if the edit CoffeeSite image can be shown
     */
    private static LoggedInUser currentUser;

    /**
     * Request type to ask CreateCoffeeSiteActivity to edit CoffeeSite
     */
    static final int EDIT_COFFEESITE_REQUEST = 1;

    /**
     * This is rating string, which will not be shown
     */
    private static final String EMPTY_RATING = "0.0 (0)";

    private final ImageView[] ratingCupsViews = new ImageView[5];

    private View rootView;

    private static final String UNKNOWN_VALUE = "-";

    /**
     * to show EnterCommentAndRatingDialogFragment with current user's rating of the coffee site in this activity
     */
    private int starsFromCurrentUser = 0;

    private ProgressBar asyncRestCallTaskProgressBar;

    /**
     * Layout with average rating of the coffee site as cup icons. Is clickable to se user's rating of the coffee site.
     */
    private LinearLayout averageStartRatingLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CoffeeSiteDetailFragment() {
    }

    public void setCoffeeSite(CoffeeSite csm) {
       this.coffeeSite = csm;
        // Pokud je fragment již zobrazen, aktualizuj UI hned
        if (rootView != null && coffeeSite != null) {
            showAllCoffeeSiteInfo(rootView, coffeeSite);
        }
    }

    public void setCurrentUser(LoggedInUser mCurrentUser) {
       currentUser = mCurrentUser;
       if (editCoffeeSiteIcon != null && coffeeSite != null) {
           if (currentUser != null && currentUser.getUserName().equals(coffeeSite.getCreatedByUserName())) {
               editCoffeeSiteIcon.setVisibility(View.VISIBLE);
           } else {
               editCoffeeSiteIcon.setVisibility(View.GONE);
           }
       }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (rootView != null && coffeeSite != null) {
            showAllCoffeeSiteInfo(rootView, coffeeSite);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.coffeesite_detail_fragment, container, false);
        editCoffeeSiteIcon = rootView.findViewById(R.id.edit_coffeesite_detail_imageView);
        editCoffeeSiteIcon.setOnClickListener(createOnClickListenerForEditCoffeeSiteImageView());
        asyncRestCallTaskProgressBar = getActivity().findViewById(R.id.load_coffeeSite_progressBar);

        averageStartRatingLayout = rootView.findViewById(R.id.rating_icons_detail_layout);
        averageStartRatingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNumberOfStarsAsyncTask();
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            coffeeSite = bundle.getParcelable(CoffeeSiteDetailsTabsAdapter.ARG_OBJECT_FRAGMENT);
            if (coffeeSite != null && !(coffeeSite instanceof CoffeeSiteMovable)) {
                coffeeSite = new CoffeeSiteMovable(coffeeSite);
            }
        }
        return rootView;
    }

    private void showAllCoffeeSiteInfo(View rootView, CoffeeSite coffeeSite) {
        // Show the CoffeeSite info in a TextViews.
        if (coffeeSite != null) {
            ((TextView) rootView.findViewById(R.id.statusZarizeniTextView)).setText(coffeeSite.getStatusZarizeni().toString());
            ((TextView) rootView.findViewById(R.id.siteTypeTextView)).setText(coffeeSite.getTypPodniku().toString());
            ((TextView) rootView.findViewById(R.id.locationTypeTextView)).setText(coffeeSite.getTypLokality().toString());

            if (coffeeSite.getCena() != null && !coffeeSite.getCena().toString().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.cenaTextView)).setText(coffeeSite.getCena().toString());
            } else {
                ((TextView) rootView.findViewById(R.id.cenaTextView)).setText(UNKNOWN_VALUE);
            }

            if (coffeeSite.getNextToMachineTypes().size() > 0) {
                ((TextView) rootView.findViewById(R.id.nextToMachineOfferTextView)).setText(coffeeSite.getNextToMachineTypesOneString());
            } else {
                rootView.findViewById(R.id.nextToMachineTableRow).setVisibility(View.GONE);
            }

            if (coffeeSite.getOtherOffers().size() > 0) {
                ((TextView) rootView.findViewById(R.id.otherOfferTextView)).setText(coffeeSite.getOtherOffersOneString());
            } else {
                ((TextView) rootView.findViewById(R.id.otherOfferTextView)).setText(UNKNOWN_VALUE);
            }

            if (coffeeSite.getCoffeeSorts().size() > 0) {
                ((TextView) rootView.findViewById(R.id.coffeeSortTextView)).setText(coffeeSite.getCoffeeSortsOneString());
            } else {
                ((TextView) rootView.findViewById(R.id.coffeeSortTextView)).setText(UNKNOWN_VALUE);
            }

            if (!coffeeSite.getUliceCP().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.streetTextView)).setText(coffeeSite.getUliceCP());
            } else {
                ((TextView) rootView.findViewById(R.id.streetTextView)).setText(UNKNOWN_VALUE);
            }

            if (!coffeeSite.getMesto().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.townNameTextView)).setText(coffeeSite.getMesto());
            } else {
                ((TextView) rootView.findViewById(R.id.townNameTextView)).setText(UNKNOWN_VALUE);
            }

            if (!coffeeSite.getOteviraciDobaDny().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.openingTextView)).setText(coffeeSite.getOteviraciDobaDny());
            }
            if (!coffeeSite.getOteviraciDobaHod().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.openingTextView)).setText(coffeeSite.getOteviraciDobaHod());
            }
            if (!coffeeSite.getOteviraciDobaHod().isEmpty() && !coffeeSite.getOteviraciDobaDny().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.openingTextView)).setText(coffeeSite.getOteviraciDobaDny() + ", " + coffeeSite.getOteviraciDobaHod());
            } else {
                ((TextView) rootView.findViewById(R.id.openingTextView)).setText(UNKNOWN_VALUE);
            }

            if (coffeeSite.getHodnoceni() != null
                    && !coffeeSite.getHodnoceni().toString().isEmpty()
                    && !EMPTY_RATING.equals(coffeeSite.getHodnoceni().toString())) {
                // Get all cups rating views
                ratingCupsViews[0] = ((ImageView) rootView.findViewById(R.id.rating_cup_1));
                ratingCupsViews[1] = ((ImageView) rootView.findViewById(R.id.rating_cup_2));
                ratingCupsViews[2] = ((ImageView) rootView.findViewById(R.id.rating_cup_3));
                ratingCupsViews[3] = ((ImageView) rootView.findViewById(R.id.rating_cup_4));
                ratingCupsViews[4] = ((ImageView) rootView.findViewById(R.id.rating_cup_5));

                ((TableRow) rootView.findViewById(R.id.hodnoceni_tablerow)).setVisibility(View.VISIBLE);
                populateRatingIcons(ratingCupsViews, coffeeSite.getHodnoceni().getAvgStars());
                ((TextView) rootView.findViewById(R.id.hodnoceniLabel)).setText(getString(R.string.detail_rating_label, coffeeSite.getHodnoceni().getNumOfHodnoceni()));
                ((TextView) rootView.findViewById(R.id.hodnoceni_detail_TextView)).setText(coffeeSite.getHodnoceni().toStringShort());
            }
            else {
                ((TextView) rootView.findViewById(R.id.hodnoceniLabel)).setText(getString(R.string.detail_rating_label, 0));
                ((TextView) rootView.findViewById(R.id.hodnoceni_detail_TextView)).setText("");
            }

            ((TextView) rootView.findViewById(R.id.createdByUserTextView)).setText(coffeeSite.getCreatedByUserName());
            ((TextView) rootView.findViewById(R.id.createdOnTextView)).setText(coffeeSite.getCreatedOnString());

            if (coffeeSite.getUvodniKoment() != null && !coffeeSite.getUvodniKoment().isEmpty()) {
                ((TextView) rootView.findViewById(R.id.initialCommentTextView)).setText(coffeeSite.getUvodniKoment());
            } else {
                ((TextView) rootView.findViewById(R.id.initialCommentTextView)).setText(UNKNOWN_VALUE);
            }
        }
    }

    /**
     * Defines cup icons for all rating cups image views
     *
     * @param ratingCupsViews
     * @param rating
     */
    private void populateRatingIcons(ImageView[] ratingCupsViews, float rating) {
        if (rating > 0 && rating <=5) {
            // Set all cups empty first
            for (ImageView cupImageView : ratingCupsViews) {
                cupImageView.setImageResource(R.drawable.cup_rating_empty_4);
            }
            // Set whole part of rating
            int intPart = (int) rating;
            for (int i = 0; i < intPart; i++) {
                ratingCupsViews[i].setImageResource(R.drawable.cup_rating_grey_full_5);
            }
            // Set fraction part of rating
            switch (Utils.getRatingFraction(rating)) {
                case QUARTER: ratingCupsViews[intPart].setImageResource(R.drawable.cup_rating_gray_quarter_5);
                    break;
                case HALF: ratingCupsViews[intPart].setImageResource(R.drawable.cup_rating_half_5);
                    break;
                case THREE_QUARTERS: ratingCupsViews[intPart].setImageResource(R.drawable.cup_rating_gray_three_quarters_5);
                    break;
                default:  ratingCupsViews[intPart].setImageResource(R.drawable.cup_rating_empty_4);
            }
        }
    }

    /**
     * OnClick listener to handle click on edit CoffeeSite imageView icon
     *
     * @return
     */
    private View.OnClickListener createOnClickListenerForEditCoffeeSiteImageView() {
        View.OnClickListener retVal;

        retVal = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get selected cup image
                if (view instanceof ImageView) {
                    if (currentUser != null
                            && currentUser.getUserName().equals(coffeeSite.getCreatedByUserName())) {
                        goToEditCoffeeSiteActivity();
                    }
                }
            }
        };
        return retVal;
    }

    private void goToEditCoffeeSiteActivity() {
        Intent activityIntent = new Intent(getActivity(), CreateCoffeeSiteActivity.class);
        activityIntent.putExtra("coffeeSite", (Parcelable) coffeeSite);
        // to indicate to CreateCoffeeSiteActivity that it was called from this Activity to correctly return back
        // as this Activity can be also called from MyCoffeeSitesListActivity
        activityIntent.putExtra("coffeeSitePosition", -1);
        // Return value handled in owner CoffeeSiteDetailActivity
        startActivityForResult(activityIntent, EDIT_COFFEESITE_REQUEST);
    }

    /**
     * Starts Async task to obtain number of stars assigned by current user to this coffee site
     */
    private void startNumberOfStarsAsyncTask() {
        if (Utils.isOnline(rootView.getContext())) {
            asyncRestCallTaskProgressBar.setVisibility(View.VISIBLE);
            // Async task for loading current user's rating for this CoffeeSite
            // The EnterCommentAndRatingDialog dialog is opened after the Async task finishes
            new GetNumberOfStarsAsyncTask(currentUser.getUserId(), coffeeSite.getId(), this).execute();
        } else { // Dialog can be opened as there might be only temporary connection problem
            showEnterCommentAndRatingDialog();
        }
    }

    /**
     * Method to be called from async task after the number of stars for this CoffeeSite and User
     * is returned from server.
     * This method can be called only before a current user wants to open EnterCommentAndRatingDialogFragment
     * to enter new Comment and Stars for the CoffeeSite or before updating current Comment and Stars for CoffeeSite.
     *
     * @param stars
     */
    @Override
    public void processNumberOfStarsForSiteAndUser(int stars) {
        this.starsFromCurrentUser = stars;
        asyncRestCallTaskProgressBar.setVisibility(View.GONE);
        showEnterCommentAndRatingDialog();
    }

    /**
     * Method to be called from Async task after failed request for the number<br>
     * of stars for this CoffeeSite and User is returned from server.<br>
     * This method can be called only before a current user wants to open<br>
     * EnterCommentAndRatingDialogFragment to enter Comment and Stars for the CoffeeSite.
     */
    @Override
    public void processFailedNumberOfStarsForSiteAndUser(Result.Error error) {
        asyncRestCallTaskProgressBar.setVisibility(View.GONE);
        showRESTCallError(error);
        // Open EnterCommentAndRatingDialogFragment
        this.starsFromCurrentUser = 0;
        showEnterCommentAndRatingDialog();
    }

    public void showRESTCallError(Result.Error error) {
        if (error != null) {
            Log.e(TAG, "REST call error: " + error.getDetail());
            Toast.makeText(rootView.getContext(), error.getDetail(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(rootView.getContext(), "Server connection error.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show the dialog for entering coffee site rating
     */
    private void showEnterCommentAndRatingDialog() {
        // Create an instance of the dialog fragment and show it
        // Passes number of stars for CoffeeSite and User to EnterCommentAndRatingDialogFragment
        EnterCommentAndRatingDialogFragment dialog;
        dialog = newDialogInstance(this.starsFromCurrentUser);
        dialog.show(getParentFragmentManager(), "EnterCommentAndRatingDialogFragment");
    }

    /**
     *
     * @param numOfStars
     * @param currentCommentText
     * @return
     */
    public static EnterCommentAndRatingDialogFragment newDialogInstance(int numOfStars) {
        EnterCommentAndRatingDialogFragment f = new EnterCommentAndRatingDialogFragment();

        // Supply num. of stars as an argument
        Bundle args = new Bundle();
        args.putInt("numOfStars", numOfStars);
        args.putBoolean("commentEditable", false);
        f.setArguments(args);
        return f;
    }
}
