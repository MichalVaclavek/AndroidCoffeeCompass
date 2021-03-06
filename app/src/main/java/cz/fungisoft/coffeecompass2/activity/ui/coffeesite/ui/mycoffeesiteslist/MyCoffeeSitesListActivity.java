package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteServiceCUDOperationsListener;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.MyCoffeeSitesViewModel;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteCUDOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteStatusChangeService;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;

import static cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSiteItemRecyclerViewAdapter.EDIT_COFFEESITE_REQUEST;

/**
 * Activity to load and show list of CoffeeSites created, not Canceled, by logged-in user.
 * Allows to Activate, Deactivate and Cancel every single CoffeeSite in the list.
 * Allows to go back to MainActivity, to go to CreateCoffeeSiteActivity to create new CoffeeSite,
 * and reload the list of sites.
 */
public class MyCoffeeSitesListActivity extends AppCompatActivity
                                       implements UserAccountServiceConnectionListener,
                                                  CancelCoffeeSiteDialogFragment.CancelCoffeeSiteDialogListener,
                                                  InsertAuthorCommentDialogFragment.InsertAuthorCommentDialogListener,
                                                  CoffeeSiteServicesConnectionListener,
                                                  CoffeeSiteLoadServiceOperationsListener,
                                                  CoffeeSiteServiceCUDOperationsListener {

    private static final String TAG = "MyCoffeeSitesListAct";

    /**
     * The main attribute of activity containing all the CoffeeSites to show
     * on this or child Activities
     */
    private List<CoffeeSite> content;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private MyCoffeeSiteItemRecyclerViewAdapter recyclerViewAdapter;
    private Parcelable mListState;

    private static final String LIST_STATE_KEY = "CoffeeSiteList";

    /**
     * UserAccount Service is probably not needed here
     */
    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userAccountServiceConnector;

    private LoggedInUser currentUser;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    /**
     * To show snackbar
     */
    public View contextView;

    private ProgressBar loadMyCoffeeSitesProgressBar;

    /**
     * Used to change selected CoffeeSite's status (ACTIVE, INACTIVE, CANCELED)
     */
    protected CoffeeSiteStatusChangeService coffeeSiteStatusChangeService;
    private CoffeeSiteServicesConnector<CoffeeSiteStatusChangeService> coffeeSiteStatusChangeServiceConnector;

    /**
     * Used for saving selected CoffeeSite after inserting CoffeeSite's creator initial coment
     */
    protected CoffeeSiteCUDOperationsService coffeeSiteCUDOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteCUDOperationsService> coffeeSiteCUDOperationsServiceConnector;


    protected CoffeeSiteLoadOperationsService coffeeSiteLoadOperationsService;
    private CoffeeSiteServicesConnector<CoffeeSiteLoadOperationsService> coffeeSiteLoadOperationsServiceConnector;

    /**
     * Request type to ask CreateCoffeeSiteActivity to create new CoffeeSite
     */
    static final int CREATE_COFFEESITE_REQUEST = 2;

    /**
     * Flag to indicate if the list of user's CoffeeSites is being loading
     * to check if the MenuItems should be enabled or disabled
     */
    private boolean isLoadingPage = false;

    private boolean isLastPage = false;;
    private int currentPage = 1;
    public static final int PAGE_SIZE = 20;

    private MyCoffeeSitesViewModel myCoffeeSitesViewModel;

    private boolean offLineModeOn = false;

    /**
     * Getter and setter to be synchronized to update status correctly when
     * CoffeeSites load async task is running.
     * Really needed?
     * @return
     */
    public synchronized boolean isLoadingPage() {
        return isLoadingPage;
    }

    public synchronized void setLoadingPage(boolean loadingPage) {
        this.isLoadingPage = loadingPage;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_coffee_sites_list);

        contextView = findViewById(R.id.my_coffeesite_frameLayout);

        loadMyCoffeeSitesProgressBar = findViewById(R.id.progress_my_coffeesites_load);

        offLineModeOn = Utils.isOfflineModeOn(getApplicationContext());

        myCoffeeSitesViewModel = new MyCoffeeSitesViewModel(getApplication());

        /* If called from MainActivity, we can pass the number of CoffeeSites from user
         * to be shown/loaded
         * Not used yet
         * int numberOfMyCoffeeSites = getIntent().getIntExtra("myCoffeeSitesNumber", 0);
         */

        // Load content CoffeeSites in case of returnign back to this activity?
        // Usually the content is loaded after the UserAccountService is connected
        content = getIntent().getParcelableExtra("myCoffeeSites");

        if (savedInstanceState != null) { // i.e. after orientation was changed
            Collections.sort(content);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_sitesList_Toolbar);
        setSupportActionBar(toolbar);

        layoutManager = new LinearLayoutManager(this);

        currentPage = 1;

        prepareAndActivateRecyclerView();

        /*
         Must be called here, in onCreate(), as after successful connection to UserAccountService
         loading of users CoffeeSites starts. We need this loading only if this Activity
         is created, not in case we returned to it from another Activity
         */
        doBindUserAccountService();

        // Starts loading of all CoffeeSites related services, see onCoffeeSiteServiceConnected() to see next steps
        doBindCoffeeSiteStatusChangeService();
    }

    @Override
    public void onBackPressed() {
        if (!isLoadingPage()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_my_coffeesites_list, menu);

        MenuItem addCoffeeSiteMenuItem = menu.findItem(R.id.action_go_to_create_coffeesite);
        addCoffeeSiteMenuItem.setVisible(!offLineModeOn);
        MenuItem reloadMyCoffeeSitesMenuItem = menu.findItem(R.id.action_refresh_list);

        /* Disable menu options when the list of user's sites
         is being loaded, otherwise enable.
         This method is invoked by invalidateOptionsMenu
         called before and after loading i.e. in showProgressBar()
         and hideProgressBar() methods
        */
        if (isLoadingPage()) {
            addCoffeeSiteMenuItem.setEnabled(false);
            reloadMyCoffeeSitesMenuItem.setEnabled(false);
        } else {
            addCoffeeSiteMenuItem.setEnabled(true);
            reloadMyCoffeeSitesMenuItem.setEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            goToMainActivity();
            return true;
        }
        if (id == R.id.action_go_to_create_coffeesite) {
            goToCreateNewSiteActivity();
            return true;
        }
        if (id == R.id.action_refresh_list) {
            if (currentUser != null && !offLineModeOn) {
                if (recyclerViewAdapter != null) {
                    // Delete current list
                    recyclerViewAdapter.clearList();
                }
                // Load list again, only if not in OFFLINE mode
                startMyCoffeeSitesLoadOperation();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToMainActivity() {
        Intent i = new Intent(MyCoffeeSitesListActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void goToCreateNewSiteActivity() {
        Intent activityIntent = new Intent(this, CreateCoffeeSiteActivity.class);
        startActivityForResult(activityIntent, CREATE_COFFEESITE_REQUEST);
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
        if (offLineModeOn) {
            myCoffeeSitesViewModel.getUsersCoffeeSites(currentUser).observe(this, new Observer<List<CoffeeSite>>() {
                @Override
                public void onChanged(@Nullable final List<CoffeeSite> myCoffeeSites) {
                    // Update the cached copy of the CoffeeSites in the adapter.
                    recyclerViewAdapter.setCoffeeSites(myCoffeeSites);
                }
            });
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

    /** **************** UserAccountService ******************* END ****/

    /********* CoffeeSiteCUDOperationsService ****** START ********/

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteCUDOperationsService;

    private void doBindCoffeeSiteCUDOperationsService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        coffeeSiteCUDOperationsServiceConnector = new CoffeeSiteServicesConnector<>();
        coffeeSiteCUDOperationsServiceConnector.addCoffeeSiteServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteCUDOperationsService.class),
                coffeeSiteCUDOperationsServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteCUDOperationsService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteLoadOperationsService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindCoffeeSiteCUDOperationsService() {
        if (mShouldUnbindCoffeeSiteCUDOperationsService) {
            if (coffeeSiteLoadOperationsService != null) {
                coffeeSiteCUDOperationsService.removeCUDOperationsListener(this);
            }
            // Release information about the service's state.
            coffeeSiteCUDOperationsServiceConnector.removeCoffeeSiteServiceConnectionListener(this);
            unbindService(coffeeSiteCUDOperationsServiceConnector);
            mShouldUnbindCoffeeSiteCUDOperationsService = false;
        }
    }

    /* */

    private void prepareAndActivateRecyclerView() {
        //Bundle extras = getIntent().getExtras();
        recyclerView = findViewById(R.id.my_coffeesite_list);
        assert recyclerView != null;

        recyclerView.setLayoutManager(layoutManager);
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerViewAdapter = new MyCoffeeSiteItemRecyclerViewAdapter(this, offLineModeOn);
        recyclerView.setAdapter(recyclerViewAdapter);
        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        if (content != null) {
            recyclerViewAdapter.setCoffeeSites(content);
        }
    }


    private void startMyCoffeeSitesLoadOperation() {
        if (Utils.isOnline()) {
            if (coffeeSiteLoadOperationsService != null && !isLoadingPage()) {
                showProgressbarAndDisableMenuItems();
                setLoadingPage(true);
                //coffeeSiteLoadOperationsService.findAllCoffeeSitesFromCurrentUser();
                coffeeSiteLoadOperationsService.findCoffeeSitesPageFromCurrentUser(1, PAGE_SIZE);
            }
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }


    /********* CoffeeSiteStatusChangeService **************/

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteStatusChangeService;

    private void doBindCoffeeSiteStatusChangeService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        coffeeSiteStatusChangeServiceConnector = new CoffeeSiteServicesConnector<>();
        coffeeSiteStatusChangeServiceConnector.addCoffeeSiteServiceConnectionListener(this);
        if (bindService(new Intent(this, CoffeeSiteStatusChangeService.class),
                coffeeSiteStatusChangeServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteStatusChangeService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteLoadOperationsService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindCoffeeSiteStatusChangeService() {
        if (mShouldUnbindCoffeeSiteStatusChangeService) {
            // Release information about the service's state.
            coffeeSiteStatusChangeServiceConnector.removeCoffeeSiteServiceConnectionListener(this);
            unbindService(coffeeSiteStatusChangeServiceConnector);
            mShouldUnbindCoffeeSiteStatusChangeService = false;
        }
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
     * Loads all services needed to work with CoffeeSites in following order:
     *
     * 1) CoffeeSiteStatusChangeService - start loading in onCreate()
     * 2) CoffeeSiteCUDOperationsService
     * 3) CoffeeSiteLoadOperationsService
     *
     */
    @Override
    public void onCoffeeSiteServiceConnected() {

        if (coffeeSiteStatusChangeServiceConnector.getCoffeeSiteService() != null) {
            if (coffeeSiteStatusChangeService == null) {
                coffeeSiteStatusChangeService = coffeeSiteStatusChangeServiceConnector.getCoffeeSiteService();
                recyclerViewAdapter.setCoffeeSiteStatusChangeService(coffeeSiteStatusChangeService);
                doBindCoffeeSiteCUDOperationsService();
            }
        }

        if (coffeeSiteCUDOperationsServiceConnector != null && coffeeSiteCUDOperationsServiceConnector.getCoffeeSiteService() != null) {
            if (coffeeSiteCUDOperationsService == null) {
                coffeeSiteCUDOperationsService = coffeeSiteCUDOperationsServiceConnector.getCoffeeSiteService();
                coffeeSiteCUDOperationsService.addCUDOperationsListener(this);
                recyclerViewAdapter.setCoffeeSiteCUDOperationsService(coffeeSiteCUDOperationsService);
                doBindCoffeeSiteLoadOperationsService();
            }
        }

        if (coffeeSiteLoadOperationsServiceConnector != null && coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService() != null) {
            if (coffeeSiteLoadOperationsService == null) {
                coffeeSiteLoadOperationsService = coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService();
                coffeeSiteLoadOperationsService.addLoadOperationsListener(this);
                // All services ready, start loading all coffeesites
                if (!offLineModeOn) {
                    startMyCoffeeSitesLoadOperation();
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

    @Override
    public void onCoffeeSiteListFromLoggedInUserLoaded(List<CoffeeSite> coffeeSites, String error) {
        setLoadingPage(false);
        hideProgressbarAndEnableMenuItems();

        if (!error.isEmpty()) {
            showMyCoffeeSitesLoadFailure(error);
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_LOAD. Error: " + error);
        }
        else {
            showMyCoffeeSitesLoadSuccess();
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_LOAD. Result: " + "OK");
            content = coffeeSites;
            if (content != null) {
                recyclerViewAdapter.setCoffeeSites(content);
            }
        }
    }

    @Override
    public void onCoffeeSiteFirstPageFromLoggedInUserLoaded(CoffeeSitePageEnvelope coffeeSitesPage, String error) {
        setLoadingPage(false);
        hideProgressbarAndEnableMenuItems();

        if (!error.isEmpty()) {
            showMyCoffeeSitesLoadFailure(error);
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_FIRST_PAGE_LOAD. Error: " + error);
        }
        else {
            showMyCoffeeSitesLoadSuccess();
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_FIRST_PAGE_LOAD. Result: " + "OK");
            if (coffeeSitesPage != null) {
                recyclerViewAdapter.addCoffeeSitesFirstPage(coffeeSitesPage.getContent());
            }
            if (coffeeSitesPage.getContent().size() >= PAGE_SIZE) {
                recyclerViewAdapter.addFooter();
            } else {
                isLastPage = true;
            }
        }
    }

    @Override
    public void onCoffeeSiteNextPageFromLoggedInUserLoaded(CoffeeSitePageEnvelope coffeeSitesPage, String error) {
        setLoadingPage(false);
        hideProgressbarAndEnableMenuItems();

        if (!error.isEmpty()) {
            showMyCoffeeSitesLoadFailure(error);
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_NEXT_PAGE_LOAD. Error: " + error);
        }
        else {
            showMyCoffeeSitesLoadSuccess();
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_NEXT_PAGE_LOAD. Result: " + "OK");
            if (coffeeSitesPage != null) {
                recyclerViewAdapter.addCoffeeSitesNextPage(coffeeSitesPage.getContent());
            }
            if(coffeeSitesPage.getContent().size() >= PAGE_SIZE){
                recyclerViewAdapter.addFooter();
            } else {
                isLastPage = true;
            }
        }
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition =  ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

            if (!isLoadingPage() && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE) {
                    if (!offLineModeOn) {
                        loadMoreItems();
                    }
                }
            }
        }
    };

    private void loadMoreItems() {
        currentPage += 1;
        showProgressbarAndDisableMenuItems();
        setLoadingPage(true);
        coffeeSiteLoadOperationsService.findCoffeeSitesPageFromCurrentUser(currentPage, PAGE_SIZE);
    }


    /**
     * Helper method to to show progressBar when loading CoffeeSites
     */
    public void showProgressbarAndDisableMenuItems() {
        loadMyCoffeeSitesProgressBar.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void hideProgressbarAndEnableMenuItems() {
        loadMyCoffeeSitesProgressBar.setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    private void showMyCoffeeSitesLoadSuccess()
    {}


    private void showMyCoffeeSitesLoadFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.my_coffeesites_load_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    /**
     * Receives result from CreateCoffeeSiteActivity, which was requested by MyCoffeeSiteItemRecyclerViewAdapter
     * when users clicked on Edit button.
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
                CoffeeSite editedCoffeeSite = (CoffeeSite) data.getExtras().getParcelable("coffeeSite");
                int coffeeSitePositionInRecyclerView = data.getExtras().getInt("coffeeSitePosition");
                if (recyclerViewAdapter != null) {
                    recyclerViewAdapter.updateEditedCoffeeSite(editedCoffeeSite, coffeeSitePositionInRecyclerView);
                }
            }
        }
        // Check which request we're responding to
        if (requestCode == CREATE_COFFEESITE_REQUEST && resultCode == RESULT_OK) {
            // User created new CoffeeSite, refresh the whole list of MyCoffeeSites
            if (currentUser != null && !offLineModeOn) {
                if (recyclerViewAdapter != null) {
                    // Delete current list
                    recyclerViewAdapter.clearList();
                }
                startMyCoffeeSitesLoadOperation();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Activated only, when Author's comment is inserted/updated. Not needed to update recycler view?
     * @param updatedCoffeeSite
     * @param error
     */
    @Override
    public void onCoffeeSiteUpdated(CoffeeSite updatedCoffeeSite, String error) {
        hideProgressbarAndEnableMenuItems();

        if (recyclerViewAdapter.isUpdatingCommentOnly()) {
            Log.i(TAG, "Author's comment save success?: " + error.isEmpty());
            if (error.isEmpty()) {
                recyclerViewAdapter.updateCoffeeSiteAfterCommentEdited(updatedCoffeeSite);
                showCoffeeSiteAuthorCommentSaveSuccess();
            }
            else {
               showCoffeeSiteSaveAuthorCommentFailure(error);
            }
        }
    }

    private void showCoffeeSiteAuthorCommentSaveSuccess() {
        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.author_comment_save_success,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showCoffeeSiteSaveAuthorCommentFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.author_comment_save_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    /*** DIALOGS LISTENERS ****/
    /*** Dialog for entering Author's comment to CoffeeSite ****/

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (recyclerViewAdapter != null) {
            if (dialog instanceof  CancelCoffeeSiteDialogFragment) {
                recyclerViewAdapter.onCancelCoffeeSiteDialogPositiveClick();
            }
            if (dialog instanceof InsertAuthorCommentDialogFragment) {
                recyclerViewAdapter.onInsertAuthorCommentDialogPositiveClick((InsertAuthorCommentDialogFragment) dialog);
            }
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (recyclerViewAdapter != null) {
            if (dialog instanceof  CancelCoffeeSiteDialogFragment) {
                recyclerViewAdapter.onCancelCoffeeSiteDialogNegativeClick();
            }
            if (dialog instanceof InsertAuthorCommentDialogFragment) {
                recyclerViewAdapter.onInsertAuthorCommentDialogNegativeClick((InsertAuthorCommentDialogFragment) dialog);
            }
        }
    }

    /*** DIALOGS LISTENERS *** END ***/

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save CoffeeSites list state
        mListState = layoutManager.onSaveInstanceState();
        state.putParcelable(LIST_STATE_KEY, mListState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Retrieve CoffeeSites list state and item positions
        if(state != null) {
            mListState = state.getParcelable(LIST_STATE_KEY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        offLineModeOn = Utils.isOfflineModeOn(getApplicationContext());
        if (mListState != null) {
            layoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // save RecyclerView state
        Bundle listState = new Bundle();
        if (recyclerView != null) {
            mListState = recyclerView.getLayoutManager().onSaveInstanceState();
            listState.putParcelable(LIST_STATE_KEY, mListState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        doUnbindUserAccountService();
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.onDestroy();
        }
        doUnbindCoffeeSiteStatusChangeService();
        doUnbindCoffeeSiteLoadOperationsService();
        doUnbindCoffeeSiteCUDOperationsService();

        currentPage = 1;
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
        super.onDestroy();
    }

}
