package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.MapsActivity;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.comments.CommentAndStars;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.UsersCSRatingAndCommentSaveOperationListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.comments.UsersCSRatingAndCommentUpdateOperationListener;
import cz.fungisoft.coffeecompass2.activity.support.DistanceChangeTextView;
import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
import cz.fungisoft.coffeecompass2.activity.ui.comments.EnterCommentAndRatingDialogFragment;
import cz.fungisoft.coffeecompass2.asynctask.comment.SaveCommentAndStarsAsyncTask;
import cz.fungisoft.coffeecompass2.asynctask.comment.UpdateStarsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.Comment;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;
import cz.fungisoft.coffeecompass2.activity.ui.fragments.CoffeeSiteDetailsTabsAdapter;
import cz.fungisoft.coffeecompass2.activity.ui.fragments.DetailsCollectionFragment;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static android.view.View.GONE;

import java.util.List;

/**
 * An activity representing a single CoffeeSite detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link FoundCoffeeSitesListActivity} (not working on tablets yet)
 * We need CoffeeSiteService to load current instance of CoffeeSite.
 */
public class CoffeeSiteDetailActivity extends ActivityWithLocationService
                                      implements CoffeeSiteServicesConnectionListener,
                                                 UserAccountServiceConnectionListener,
                                                 EnterCommentAndRatingDialogFragment.CommentAndRatingDialogListener,
                                                 UsersCSRatingAndCommentSaveOperationListener,
                                                 UsersCSRatingAndCommentUpdateOperationListener,
                                                 CoffeeSiteLoadServiceOperationsListener {

    private static final String TAG = "CoffeeSiteDetailAct";

    private DetailsCollectionFragment detailsCollectionFragment;

    private CoffeeSite coffeeSite;

    protected CoffeeSiteLoadOperationsService coffeeSiteLoadOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteLoadOperationsService> coffeeSiteLoadOperationsServiceConnector;

    private ProgressBar asyncRestCallTaskProgressBar;
    private LoggedInUser currentUser;

    /**
     * Shows current CoffeeSite distance under the details of the CoffeeSite
     */
    private DistanceChangeTextView distanceTextView;

    /**
     * UserAccount Service is probably not needed here
     */
    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userAccountServiceConnector;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    /**
     * Request type to ask CreateCoffeeSiteActivity to edit CoffeeSite
     */
    static final int EDIT_COFFEESITE_REQUEST = 1;

    /**
     * To show snackbar
     */
    private View contextView;

    private Toolbar mainToolbar;

    private String coffeeSiteURL = "";

    // Calling activity can request to show image fragment first
    private boolean showImageFirstRequest = false;

    /**
     * Enum to distinguish between saving the very first Stars rating or updating Stars rating
     */
//    private enum StarsOperation {SAVE, UPDATE}

//    private StarsOperation starsOperation = StarsOperation.SAVE; // default is SAVE as the new CoffeeSite rating is expected

    private int numOfStarsSelectedByUser = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_detail);

        contextView = findViewById(R.id.coffeesite_detaill_main_layout);

        asyncRestCallTaskProgressBar = findViewById(R.id.load_coffeeSite_progressBar);

        // Read coffee site data from calling activity
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            coffeeSite = bundle.getParcelable("coffeeSite");
            if (coffeeSite != null && !(coffeeSite instanceof CoffeeSiteMovable)) {
                coffeeSite = new CoffeeSiteMovable(coffeeSite);
            }
            showImageFirstRequest = bundle.getBoolean("showImageFirst");

            // Opened from MainActivity upon receiving notification about new CoffeeSite
            coffeeSiteURL = bundle.getString("coffeeSiteUrl");
        }

        // Setup main toolbar
        mainToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mainToolbar != null && coffeeSite != null) {
            getSupportActionBar().setTitle("Detaily");
            mainToolbar.setSubtitle(coffeeSite.getName());
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        // http://developer.android.com/guide/components/fragments.html
        if (savedInstanceState == null) {
            // Create the detailsCollectionFragment and add it to the activity
            // using a fragment transaction.
            if (this.coffeeSite != null) {
                createAndShowDetailsFragments(this.coffeeSite);
            }
        }

        // Show distance to the CoffeeSite
        LinearLayout distanceLinearLayout = findViewById(R.id.distance_detail_linear_layout);

        if (coffeeSite instanceof  CoffeeSiteMovable) {
            distanceLinearLayout.setVisibility(View.VISIBLE);
            distanceTextView = (DistanceChangeTextView) findViewById(R.id.distanceTextView);

            distanceTextView.setTag(TAG + ". DistanceTextView for " + coffeeSite.getName());

            distanceTextView.setCoffeeSite((CoffeeSiteMovable) coffeeSite);
            distanceTextView.setText(Utils.getDistanceInBetterReadableForm(coffeeSite.getDistance()));
        } else {
            distanceLinearLayout.setVisibility(View.GONE);
        }

        /*
         Must be called here, in onCreate(), as after successful connection to UserAccountService
         loading of users CoffeeSites starts. We need this loading only if this Activity
         is created, not in case we returned to it from another Activity
         */
        doBindUserAccountService();
    }

    private void createAndShowDetailsFragments(CoffeeSite coffeeSite) {
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putParcelable(CoffeeSiteDetailsTabsAdapter.ARG_OBJECT_FRAGMENT, coffeeSite);
        fragmentArgs.putBoolean("showImageFirst", showImageFirstRequest);
        detailsCollectionFragment = new DetailsCollectionFragment();
        detailsCollectionFragment.setArguments(fragmentArgs);
        detailsCollectionFragment.setCurrentUser(currentUser);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.coffeesite_detail_container, detailsCollectionFragment)
                .commit();
    }

    /** **************** UserAccountService ******************* START ****/

    private void doBindUserAccountService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
        userAccountServiceConnector = new UserAccountServiceConnector();
        userAccountServiceConnector.addUserAccountServiceConnectionListener(this);

        if (bindService(new Intent(this, UserAccountService.class),
                userAccountServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    @Override
    public void onUserAccountServiceConnected() {
        userAccountService = userAccountServiceConnector.getUserLoginService();
        currentUser = userAccountService.getLoggedInUser();
        //detailsCollectionFragmentAdapter.setCurrentUser(currentUser);
        if (detailsCollectionFragment != null) {
            detailsCollectionFragment.setCurrentUser(currentUser);
        }
    }

    /** UnBind UserAccountService ****/
    private void doUnbindUserAccountService() {
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            userAccountServiceConnector.removeUserAccountServiceConnectionListener(this);
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

    /**
     * locationService is not null here
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();
        if (coffeeSite != null && locationService != null) {
            if (coffeeSite instanceof CoffeeSiteMovable) {
                ((CoffeeSiteMovable) coffeeSite).setLocationService(locationService);
                locationService.addPropertyChangeListener((CoffeeSiteMovable) coffeeSite);
                ((CoffeeSiteMovable) coffeeSite).addPropertyChangeListener(distanceTextView);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindCoffeeSiteLoadOperationsService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Always try to load actual instance of CoffeeSite and convert it to CoffeeSiteMovable in
        doBindCoffeeSiteLoadOperationsService();
    }

    /**
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindUserAccountService();
    }

    public void startCoffeeSiteLoad(long coffeeSiteId) {
        showProgressbar();
        coffeeSiteLoadOperationsService.findCoffeeSiteById(coffeeSiteId);
    }

    public void startCoffeeSiteLoad(String coffeeSiteURL) {
        showProgressbar();
        coffeeSiteLoadOperationsService.findCoffeeSiteByURL(coffeeSiteURL);
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void showProgressbar() {
        asyncRestCallTaskProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void hideProgressbar() {
        asyncRestCallTaskProgressBar.setVisibility(GONE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // navigateUpTo(new Intent(this, FoundCoffeeSitesListActivity.class));

            /*
             * The standard way, navigateUpTo(new Intent(this, FoundCoffeeSitesListActivity.class));
             * did not work for me. I have implemented this hint based on
             * internet advice. It transforms the Back button click from Navigation bar
             * to "main" Back button of Android click
             */
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Receives result from {@link CreateCoffeeSiteActivity}, which was requested by this activity,
     * when user clicked on Edit menu item.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult() from CreateCoffeeSiteActivity. Request code: " + requestCode + ". Result code: " + resultCode);

        // When returning from CreateCoffeeSiteActivity the parameter onActivityResult(int requestCode
        // has the wrong, unassigned value. Therefore we pass the requestCode using Extras
        if (data != null) {
            int requestC = data.getExtras().getInt("requestCode");
            if (requestC == EDIT_COFFEESITE_REQUEST) {
                requestCode = EDIT_COFFEESITE_REQUEST;
            }

            // Check which request we're responding to
            if (requestCode == EDIT_COFFEESITE_REQUEST) {
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    if (Utils.isOnline(getApplicationContext()) && !coffeeSite.getStatusZaznamu().getStatus().isEmpty()) { // do no load if this is CoffeeSIte creteted offline, not saved yet
                        // Reloads CoffeeSite to show current saved data after edit
                        startCoffeeSiteLoad(coffeeSite.getId());
                    } else { // or gets as return value from Edit activity
                        // add property change listener for new coffeesite
                        coffeeSite = new CoffeeSiteMovable(data.getExtras().getParcelable("coffeeSite"));
                        ((CoffeeSiteMovable) coffeeSite).setLocationService(locationService);
                        locationService.addPropertyChangeListener((CoffeeSiteMovable) coffeeSite);
                        ((CoffeeSiteMovable) coffeeSite).addPropertyChangeListener(distanceTextView);
                        // Refresh detailFragment to update View with the new CoffeeSiteMovable
                        mainToolbar.setSubtitle(coffeeSite.getName());
                        refreshDetailFragment(coffeeSite);
                    }
                }
            }
        }
    }

    /* ====== HANDLERS of BUTTONS clicks ======== */

    public void onCommentsButtonClick(View v) {
        Intent commentsIntent = new Intent(this, CommentsListActivity.class);
        commentsIntent.putExtra("site", (Parcelable) coffeeSite);
        startActivity(commentsIntent);
    }

    public void onMapButtonClick(View v) {
        if (Utils.isOnline(getApplicationContext())) {
            if (locationService != null) {
                Intent mapIntent = new Intent(this, MapsActivity.class);
                mapIntent.putExtra("currentLocation", locationService.getCurrentLatLng());
                mapIntent.putExtra("site", (Parcelable) coffeeSite);
                startActivity(mapIntent);
            }
        } else {
            Utils.showMapNotAvailableIfNoInternetToast(getApplicationContext());
        }
    }

    /**
     * Method to be called from async task GetNumberOfCommentsAsyncTask after call to obtain number of comments for
     * the CoffeeSite within this Activity
     *
     * @param numberOfComments
     */
    public void processNumberOfComments(int numberOfComments) {
        // Not needed to be implemented currently as the Comments button is still visible/enabled
        // But we need it as it is really called from GetNumberOfCommentsAsyncTask
    }

    public void showRESTCallError(Result.Error error) {
        hideProgressbar();
        if (error != null) {
            Log.e(TAG, "REST call error: " + error.getDetail());
            Toast.makeText(getApplicationContext(), error.getDetail(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Server connection error.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteLoadOperationsService;

    private void doBindCoffeeSiteLoadOperationsService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other  applications).
        coffeeSiteLoadOperationsServiceConnector = new CoffeeSiteServicesConnector<>();
        coffeeSiteLoadOperationsServiceConnector.addCoffeeSiteServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteLoadOperationsService.class),
                coffeeSiteLoadOperationsServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteLoadOperationsService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteLoadOperationsService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    /**
     * When the CoffeeSiteLoadOperationsService is connected, loads the current CoffeeSite's data
     * from server.
     */
    @Override
    public void onCoffeeSiteServiceConnected() {
        if (coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService() != null) {
            coffeeSiteLoadOperationsService = coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService();
            coffeeSiteLoadOperationsService.addLoadOperationsListener(this);
            // refresh CoffeeSite after start if possible
            if (Utils.isOnline(getApplicationContext())) {
               if (coffeeSite != null && !coffeeSite.getStatusZaznamu().getStatus().isEmpty()) {
                   startCoffeeSiteLoad(coffeeSite.getId());
                   return;
               }
               if (coffeeSiteURL != null && !coffeeSiteURL.isEmpty()) {
                   startCoffeeSiteLoad(coffeeSiteURL);
               }
            }
        }
    }

    private void doUnbindCoffeeSiteLoadOperationsService() {
        if (mShouldUnbindCoffeeSiteLoadOperationsService) {
            if (coffeeSiteLoadOperationsService != null) {
                coffeeSiteLoadOperationsService.removeLoadOperationsListener(this);
            }
            // Release information about the service's state.
            coffeeSiteLoadOperationsServiceConnector.removeCoffeeSiteServiceConnectionListener(this);
            unbindService(coffeeSiteLoadOperationsServiceConnector);
            mShouldUnbindCoffeeSiteLoadOperationsService = false;
        }
    }

    /**
     * CoffeeSites's data reloaded from server, show the data.
     *
     * @param loadedCoffeeSite - CoffeeSite's data reloaded from server
     * @param error - indication, if there was error during loading
     */
    @Override
    public void onCoffeeSiteLoaded(CoffeeSite loadedCoffeeSite, String error) {
        hideProgressbar();
        Log.i(TAG, "CoffeeSite load success?: " + error.isEmpty());
        if (error.isEmpty()) {
            // Loaded actual instance of CoffeeSite - transform it to CoffeeSiteMovable
            if (loadedCoffeeSite != null) {
                // Keep already known distance if available
                if (coffeeSite != null) {
                    loadedCoffeeSite.setDistance(coffeeSite.getDistance());
                }
                coffeeSite = new CoffeeSiteMovable(loadedCoffeeSite);
                ((CoffeeSiteMovable) coffeeSite).addPropertyChangeListener(distanceTextView);
                // add new coffeeSite as a listener of the locationService
                if (locationService != null) {
                    ((CoffeeSiteMovable) coffeeSite).setLocationService(locationService);
                    locationService.addPropertyChangeListener((CoffeeSiteMovable) coffeeSite);
                }
            }
            // Refresh detailFragment to update View with the new CoffeeSiteMovable
            mainToolbar.setSubtitle(coffeeSite.getName());
            refreshDetailFragment(coffeeSite);
        } else {
            // Not needed to change current coffeeSite, only show some message?
            showCoffeeSiteLoadFailure(error);
        }
    }

    private void showCoffeeSiteLoadFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffee_site_load_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    /**
     * Helper method to update detail fragment data
     * @param coffeeSite current CoffeeSite data to be shown in {@code detailFragment}
     */
    private void refreshDetailFragment(CoffeeSite coffeeSite) {
        if (coffeeSite != null) {
            if (detailsCollectionFragment == null) {
                createAndShowDetailsFragments(this.coffeeSite);
            }
            else {
                detailsCollectionFragment.setCoffeeSite(coffeeSite);
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.detach(detailsCollectionFragment);
                ft.attach(detailsCollectionFragment);
                ft.commit();
            }
        }
    }

    /**
     * Process positive response, i.e. try to save Star
     * @param dialog
     */
    @Override
    public void onSaveUpdateCommentDialogPositiveClick(EnterCommentAndRatingDialogFragment dialog) {
        if (Utils.isOnline(getApplicationContext())) {
            asyncRestCallTaskProgressBar.setVisibility(View.VISIBLE);
            // Even if only Stars are updated, we need Comment object to be saved by UpdateCommentAndStarsAsyncTask
            numOfStarsSelectedByUser = dialog.getCommentAndStars().getStars().getNumOfStars();
            new UpdateStarsAsyncTask(userAccountService, this, coffeeSite.getId(), numOfStarsSelectedByUser).execute();
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    /**
     * Stars rating has been updated, reload the coffee site to show current data.
     * @param comment
     */
    @Override
    public void processUpdatedStarsRating(Integer result) {
        hideProgressbar();
        startCoffeeSiteLoad(coffeeSite.getId());
    }

    /**
     * Method to be called from Async task after new stars rating is saved to coffeeSite.
     * Used as a trigger to reload coffeeSite to show current stars rating
     * @param comments
     */
    @Override
    public void processSaveComments(List<Comment> comments) {
        hideProgressbar();
        startCoffeeSiteLoad(coffeeSite.getId());
    }

    @Override
    public void processFailedCommentUpdate(Result.Error error) {
        // if error, then this is probably the first rating for this user and coffeeSite, so, update not possible
        // try to use SaveCommentAndStarsAsyncTask
        CommentAndStars commentAndStars = new CommentAndStars();
        commentAndStars.setStars(new CommentAndStars.Stars(numOfStarsSelectedByUser));
        commentAndStars.setComment("");
        new SaveCommentAndStarsAsyncTask(coffeeSite.getId(), userAccountService, this, commentAndStars).execute();
    }

    @Override
    public void processFailedCommentSave(Result.Error error) {
        showRESTCallError(error);
    }
}
