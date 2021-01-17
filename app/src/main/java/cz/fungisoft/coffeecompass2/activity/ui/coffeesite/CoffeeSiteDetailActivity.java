package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.MapsActivity;
import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModeDownloadPreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;
import cz.fungisoft.coffeecompass2.ui.fragments.CoffeeSiteDetailFragment;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static android.view.View.GONE;

/**
 * An activity representing a single CoffeeSite detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link FoundCoffeeSitesListActivity}.
 * We need CoffeeSiteService to load current instance of CoffeeSite ...
 */
public class CoffeeSiteDetailActivity extends ActivityWithLocationService
                                      implements CoffeeSiteServicesConnectionListener,
                                                 UserAccountServiceConnectionListener,
                                                 CoffeeSiteLoadServiceOperationsListener {

    private static final String TAG = "CoffeeSiteDetailAct";

    private Button commentsButton;

    private CoffeeSiteDetailFragment detailFragment;

    private CoffeeSite coffeeSite;

    protected CoffeeSiteLoadOperationsService coffeeSiteLoadOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteLoadOperationsService> coffeeSiteLoadOperationsServiceConnector;

    private ProgressBar loadCoffeeSiteProgressBar;
    private LoggedInUser currentUser;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_detail);

        // Provides OFFLINE mode status
        DataForOfflineModeDownloadPreferenceHelper dataDownloadPreferenceHelper = new DataForOfflineModeDownloadPreferenceHelper(this);

        contextView = findViewById(R.id.coffeesite_detaill_main_layout);

        commentsButton = findViewById(R.id.commentsButton);

        loadCoffeeSiteProgressBar = findViewById(R.id.load_coffeeSite_progressBar);

        // Read coffee site data from calling activity
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            coffeeSite = bundle.getParcelable("coffeeSite");
            if (!(coffeeSite instanceof CoffeeSiteMovable)) {
                coffeeSite = new CoffeeSiteMovable(coffeeSite);
            }
        }

        if (coffeeSite != null) {
            Button imageButton = findViewById(R.id.imageButton);
            imageButton.setVisibility(GONE);
            if (!coffeeSite.getMainImageURL().isEmpty()) {
                boolean offlineModeOn = Utils.isOfflineModeOn(getApplicationContext());
                if (!coffeeSite.getMainImageFileName().isEmpty() && (Utils.isOnline() || offlineModeOn)) {
                    imageButton.setVisibility(View.VISIBLE);
                    imageButton.setEnabled(true);
                }
            }
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
        //
        // http://developer.android.com/guide/components/fragments.html
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            detailFragment = new CoffeeSiteDetailFragment();
            detailFragment.setCoffeeSite(coffeeSite);
        }

        /*
         Must be called here, in onCreate(), as after successful connection to UserAccountService
         loading of users CoffeeSites starts. We need this loading only if this Activity
         is created, not in case we returned to it from another Activity
         */
        doBindUserAccountService();
    }

    /** **************** UserAccountService ******************* START ****/

    private void doBindUserAccountService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
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
        MenuItem editCoffeeSiteMenuItem = mainToolbar.getMenu().size() > 0 ? mainToolbar.getMenu().findItem(R.id.action_go_to_edit_coffeesite) : null;

        if (editCoffeeSiteMenuItem != null && currentUser != null
                && currentUser.getUserName().equals(coffeeSite.getCreatedByUserName())) {
            editCoffeeSiteMenuItem.setVisible(true);
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
        if (coffeeSite != null ) {
            if (coffeeSite instanceof CoffeeSiteMovable) {
                ((CoffeeSiteMovable) coffeeSite).setLocationService(locationService);
                locationService.addPropertyChangeListener((CoffeeSiteMovable) coffeeSite);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.coffeesite_detail_container, detailFragment)
                    .commit();
        }
    }

    @Override
    protected void onStop() {
        doUnbindCoffeeSiteLoadOperationsService();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Always try to load actual instance of CoffeeSite and convert it to CoffeeSiteMovable in
        doBindCoffeeSiteLoadOperationsService();
    }

    /**
     * locationService.removePropertyChangeListener((CoffeeSiteMovable) coffeeSite); should
     * be performed here, but if done, then FoundCoffeeSiteListActivity, the respective item
     * in FoundCoffeeSitesRecyclerViewAdapter, does not updates CoffeeSite distance in the
     * given label ...
     * TODO found the error ...
     */
    @Override
    protected void onDestroy() {
        doUnbindUserAccountService();
        super.onDestroy();
    }

    public void startCoffeeSiteLoad() {
        showProgressbar();
        coffeeSiteLoadOperationsService.findCoffeeSiteById(coffeeSite.getId());
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void showProgressbar() {
        loadCoffeeSiteProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void hideProgressbar() {
        loadCoffeeSiteProgressBar.setVisibility(GONE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_coffeesite_detail, menu);
        MenuItem editCoffeeSiteMenuItem = menu.findItem(R.id.action_go_to_edit_coffeesite);
        if (currentUser != null && currentUser.getUserName().equals(coffeeSite.getCreatedByUserName())) {
            editCoffeeSiteMenuItem.setVisible(true);
        }
        return true;
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

        if (id == R.id.action_go_to_edit_coffeesite) {
             if (currentUser != null
                 && currentUser.getUserName().equals(coffeeSite.getCreatedByUserName())) {
                if (Utils.isOnline()) {
                    goToEditCoffeeSiteActivity();
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToEditCoffeeSiteActivity() {
        Intent activityIntent = new Intent(this, CreateCoffeeSiteActivity.class);
        activityIntent.putExtra("coffeeSite", (Parcelable) coffeeSite);
        startActivityForResult(activityIntent, EDIT_COFFEESITE_REQUEST);
    }

    /**
     * Receives result from CreateCoffeeSiteActivity, which was requested by this activity,
     * when user clicked on Edit menu item.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "onActivityResult() from CreateCoffeeSiteActivity. Request code: " + requestCode + ". Result code: " + resultCode);

        // Check which request we're responding to
        if (requestCode == EDIT_COFFEESITE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (Utils.isOnline()) {
                    // Reloads CoffeeSite to show current saved data after edit
                    startCoffeeSiteLoad();
                } else { // or gets as return value from Edit activity
                    coffeeSite = new CoffeeSiteMovable(data.getExtras().getParcelable("coffeeSite"));
                    // Refresh detailFragment to update View with the new CoffeeSiteMovable
                    refreshDetailFragment(coffeeSite);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /* ====== HANDLERS of BUTTONS clicks ======== */

    public void onImageButtonClick(View v) {
        Intent imageIntent = new Intent(this, CoffeeSiteImageActivity.class);
        imageIntent.putExtra("coffeeSite", (Parcelable) coffeeSite);
        startActivity(imageIntent);
    }

    public void onCommentsButtonClick(View v) {
        Intent commentsIntent = new Intent(this, CommentsListActivity.class);
        commentsIntent.putExtra("site", (Parcelable) coffeeSite);
        startActivity(commentsIntent);
    }

    public void onMapButtonClick(View v) {
        if (Utils.isOnline()) {
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
        if (error != null) {
            Log.e(TAG, "REST call error: " + error.getDetail());
            Toast.makeText(getApplicationContext(),
                    error.getDetail(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Server connection error.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * To enable the commentsButton in case comments are available for the Coffee site
     * found in GetCommentsAsyncTask
     */
    public void enableCommentsButton() {
        commentsButton.setVisibility(View.VISIBLE);
        commentsButton.setEnabled(true);
    }

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteLoadOperationsService;

    private void doBindCoffeeSiteLoadOperationsService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
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
            // refresh CoffeeSite after start
            if (Utils.isOnline()) {
               startCoffeeSiteLoad();
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
                // add new coffeeSite as a listener of the locationService
                if (locationService != null) {
                    ((CoffeeSiteMovable) coffeeSite).setLocationService(locationService);
                    locationService.addPropertyChangeListener((CoffeeSiteMovable) coffeeSite);
                }
            }
            // Refresh detailFragment to update View with the new CoffeeSiteMovable
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
        detailFragment.setCoffeeSite(coffeeSite);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(detailFragment);
        ft.attach(detailFragment);
        ft.commit();
    }

}
