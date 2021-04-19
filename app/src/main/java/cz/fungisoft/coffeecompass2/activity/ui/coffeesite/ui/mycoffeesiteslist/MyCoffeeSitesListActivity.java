package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteServiceCUDOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteUploadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.MyCoffeeSitesViewModel;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteCUDOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteImageService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteImageServiceConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteLoadOperationsService;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteServicesConnector;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteStatusChangeService;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteImageServiceCallResultListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteImageServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.CoffeeSiteServicesConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.ImageUtil;
import cz.fungisoft.coffeecompass2.utils.NetworkStateReceiver;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity.MODE_CREATE_FROM_MYCOFFEESITESACTIVITY;
import static cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSiteItemRecyclerViewAdapter.EDIT_COFFEESITE_REQUEST;

/**
 * Activity to load and show list of CoffeeSites created, not Canceled, by logged-in user.
 * Allows to Activate, Deactivate and Cancel every single CoffeeSite in the list.
 * <p>
 * Allows to go back to MainActivity, to go to CreateCoffeeSiteActivity to create new CoffeeSite,
 * and reload the list of sites.
 * <p>
 * Also uploads CoffeeSites created when Offline.
 */
public class MyCoffeeSitesListActivity extends AppCompatActivity
                                       implements UserAccountServiceConnectionListener,
                                                  CancelCoffeeSiteDialogFragment.CancelCoffeeSiteDialogListener,
                                                  InsertAuthorCommentDialogFragment.InsertAuthorCommentDialogListener,
                                                  CoffeeSiteServicesConnectionListener,
                                                  CoffeeSiteImageServiceConnectionListener,
                                                  CoffeeSiteImageServiceCallResultListener,
                                                  CoffeeSiteLoadServiceOperationsListener,
                                                  CoffeeSiteUploadServiceOperationsListener,
                                                  CoffeeSiteServiceCUDOperationsListener {

    private static final String TAG = "MyCoffeeSitesListAct";

    /**
     * Detector of internet connection change
     */
    private final NetworkStateReceiver networkChangeStateReceiver = new NetworkStateReceiver();

    /**
     * The main attribute of activity containing all the CoffeeSites to show
     * on this or child Activities
     */
    private static List<CoffeeSite> content = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private static MyCoffeeSiteItemRecyclerViewAdapter recyclerViewAdapter;
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
     * Service for uploading and deleting CoffeeSite's image over REST
     */
    protected CoffeeSiteImageService coffeeSiteImageService;
    private CoffeeSiteImageServiceConnector coffeeSiteImageServiceConnector;

    /**
     * Request type to ask CreateCoffeeSiteActivity to create new CoffeeSite
     */
    public static final int CREATE_COFFEESITE_REQUEST = 2;

    /**
     * Flag to indicate if the list of user's CoffeeSites is being loading
     * to check if the MenuItems should be enabled or disabled
     */
    private boolean isLoadingPage = false;

    private boolean isLastPage = false;
    private int currentPage = 1;
    public static final int PAGE_SIZE = 20;

    private MyCoffeeSitesViewModel myCoffeeSitesViewModel;


    /**
     * CoffeeSites to be saved on server (newly created or modified previously saved on server ones)
     */
    private static List<CoffeeSite> notSavedCoffeeSites;

    /**
     * Setter and getter synchronized
     *
     * @return
     */
    private static synchronized List<CoffeeSite> getNotSavedCoffeeSites() {
        return notSavedCoffeeSites;
    }

    public static synchronized void setNotSavedCoffeeSites(List<CoffeeSite> notSavedCoffeeSites) {
        MyCoffeeSitesListActivity.notSavedCoffeeSites = notSavedCoffeeSites;
    }

    /**
     * CoffeeSites saved on server after download for OfflineMode
     */
    private static List<CoffeeSite> coffeeSitesInDBDownloaded;


    /**
     * flag to indicate, which list is being shown in the Activity:
     *
     * 1) list of all CoffeeSites from user loaded online from server paginated
     * or CoffeeSites downloaded into DB (if we are Offline now)
     * 2) list of CoffeeSites saved in DB, but modified or newly created (by current user)
     */
    boolean showListNotModifiedCoffeeSites = true; // default means list number 1

    private synchronized void setShowNotModifiedCoffeeSites(boolean show) {
        showListNotModifiedCoffeeSites = show;
    }

    private synchronized boolean isShowNotModifiedCoffeeSites() {
        return showListNotModifiedCoffeeSites;
    }

    /**
     * Getter and setter to be synchronized to update status correctly when
     * CoffeeSites (up)load async task is running.
     *
     * @return
     */
    public synchronized boolean isLoadingPage() {
        return isLoadingPage;
    }

    public synchronized void setLoadingPage(boolean loadingPage) {
        this.isLoadingPage = loadingPage;
    }

    private ActivityResultLauncher<Intent> createCoffeeSiteActivityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_coffee_sites_list);

        contextView = findViewById(R.id.my_coffeesite_frameLayout);
        loadMyCoffeeSitesProgressBar = findViewById(R.id.progress_my_coffeesites_load);
        layoutManager = new LinearLayoutManager(this);

        myCoffeeSitesViewModel = new MyCoffeeSitesViewModel(getApplication());

        /* If called from MainActivity, we can pass the number of CoffeeSites from user
         * to be shown/loadedGetNumSitesFromUserAT
         * Not used yet
         * int numberOfMyCoffeeSites = getIntent().getIntExtra("myCoffeeSitesNumber", 0);
         */

        showListNotModifiedCoffeeSites = getIntent().getBooleanExtra("showListNotModifiedCoffeeSites", true);

        if (savedInstanceState != null) { // i.e. after orientation was changed
            Collections.sort(content);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_sitesList_Toolbar);
        setSupportActionBar(toolbar);
        // Setup main toolbar with back button arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
         Must be called here, in onCreate(), as after successful connection to UserAccountService
         loading of users CoffeeSites starts. We need this loading only if this Activity
         is created, not in case we returned to it from another Activity */
        doBindUserAccountService();

        // Starts loading of all CoffeeSites related services, see onCoffeeSiteServiceConnected() to see next steps
        // If services are connected correctly, initiates loading of CoffeeSites
        doBindCoffeeSiteStatusChangeService();

        // To save images of the CoffeeSites created/updated in Offline
        doBindCoffeeSiteImageService();

        prepareAndActivateRecyclerView();
    }


    public void  openCreateCoffeeSiteActivityForResult() {
        Intent intent = new Intent(this, CreateCoffeeSiteActivity.class);
        createCoffeeSiteActivityResultLauncher.launch(intent);
    }

    @Override
    public void onBackPressed() {
        if (!isLoadingPage()) {
            super.onBackPressed();
        }
    }

    /**
     * Switch to switch between two modes of this list. Either standard list of my CoffeeSites
     * is shown (i.e. my CoffeeSites on server or in local DB)
     * or lis yof CoffeeSites in local Db, which are not saved on server yet.
     */
    private SwitchMaterial switchAB;

    private MenuItem addCoffeeSiteMenuItem;

    private MenuItem uploadCoffeeSitesMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_my_coffeesites_list, menu);

        addCoffeeSiteMenuItem = menu.findItem(R.id.action_go_to_create_coffeesite);
        uploadCoffeeSitesMenuItem = menu.findItem(R.id.action_upload_coffeesites);

        addCoffeeSiteMenuItem.setVisible(true); // new CoffeeSite can be always created, Online and Offline too

        // Switch button to switch between list of CoffeeSites not saved on server yet  (i.e. new and modified CoffeeSites)
        // and list of all CoffeeSites created by user either loaded from server or from DB
        MenuItem item = menu.findItem(R.id.switchId);
        item.setActionView(R.layout.switch_layout);
        switchAB = item.getActionView().findViewById(R.id.switchAB);

        myCoffeeSitesViewModel.getNumOfCoffeeSitesNotSavedOnServer().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer numOfSitesNotSavedOnServer) {
                if (numOfSitesNotSavedOnServer != null) {
                    switchAB.setVisibility(numOfSitesNotSavedOnServer > 0 ? View.VISIBLE : View.GONE);
                    uploadCoffeeSitesMenuItem.setVisible(!isShowNotModifiedCoffeeSites() && numOfSitesNotSavedOnServer > 0 && Utils.isOnline(getApplicationContext()));
                    if (numOfSitesNotSavedOnServer == 0) {
                        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_sites_activity_toolbar_mainlabel);
                    }
                }
            }
        });

        // Switch button to switch between list of CoffeeSites not saved on server yet  (i.e. new and modified CoffeeSites)
        // and list of all CoffeeSites created by user either loaded from server or from DB
        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && getNotSavedCoffeeSites() != null) {
                    setShowNotModifiedCoffeeSites(false);
                    uploadCoffeeSitesMenuItem.setVisible(getNotSavedCoffeeSites().size() > 0 && Utils.isOnline(getApplicationContext()));
                    if (recyclerViewAdapter != null) {
                        recyclerViewAdapter.clearList();
                        content = new ArrayList<>(getNotSavedCoffeeSites());
                        recyclerViewAdapter.setCoffeeSites(content);
                    }
                    Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_sites_activity_toolbar_tosave_label);
                    setLoadingPage(false);
                    hideProgressbar();
                } else {
                    uploadCoffeeSitesMenuItem.setVisible(false);
                    setShowNotModifiedCoffeeSites(true);
                    if (recyclerViewAdapter != null) {
                        recyclerViewAdapter.clearList();
                    }
                    reloadAllUsersCoffeeSites();
                    /* see also method onUserAccountServiceConnected() for more info (reloading list of CoffeeSites if in OfflineMode ) */
                }
            }
        });

        return true;
    }

    //TODO - used by RecycleViewAdpater, check if needed
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /* Disable menu options when the list of user's sites
         is being loaded, otherwise enable.
         This method is invoked by invalidateOptionsMenu
         called before and after loading i.e. in showProgressBar()
         and hideProgressBar() methods  */
        boolean enableMenuButtons = !isLoadingPage();
        addCoffeeSiteMenuItem.setEnabled(enableMenuButtons);
        switchAB.setEnabled(enableMenuButtons);
        uploadCoffeeSitesMenuItem.setEnabled(enableMenuButtons);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        if (id == R.id.action_go_to_create_coffeesite) {
            if (currentUser != null) {
                DataForOfflineModePreferenceHelper dataForOfflineModePreferenceHelper = new DataForOfflineModePreferenceHelper(getApplicationContext());
                if (dataForOfflineModePreferenceHelper.getCSEntitiesDownloaded()) {
                    goToCreateNewSiteActivity();
                } else {
                    Snackbar mySnackbar = Snackbar.make(contextView, R.string.data_for_offline_coffee_site_create_not_available, Snackbar.LENGTH_LONG);
                    mySnackbar.show();
                }
            }
            return true;
        }
        // Upload unsaved CoffeeSites to server
        if (id == R.id.action_upload_coffeesites) {
            if (Utils.isOnline(getApplicationContext())) {
                DataForOfflineModePreferenceHelper dataForOfflineModePreferenceHelper = new DataForOfflineModePreferenceHelper(getApplicationContext());
                if (currentUser != null && dataForOfflineModePreferenceHelper.getDataSavedOfflineAvailable()
                        && getNotSavedCoffeeSites() != null) {
                    startUploadCoffeeSitesOperation(getNotSavedCoffeeSites());
                }
            } else {
                Utils.showNoInternetToast(getApplicationContext());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadAllUsersCoffeeSites() {
        boolean offLineModeOn = Utils.isOfflineModeOn(getApplicationContext());
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_sites_activity_toolbar_mainlabel);
        if (currentUser != null && !offLineModeOn) {
            // Load list again, only if not in OFFLINE mode
            startMyCoffeeSitesLoadOperation();
            return;
        }
        if (coffeeSitesInDBDownloaded != null && offLineModeOn) {
            showProgressbar();
            if (coffeeSitesInDBDownloaded.size() > 0) {
                String title = getResources().getString(R.string.my_sites_activity_toolbar_mainlabel) + " (offline)";
                Objects.requireNonNull(getSupportActionBar()).setTitle(title);
                content = new ArrayList<>(coffeeSitesInDBDownloaded);
                recyclerViewAdapter.setCoffeeSites(content);
            }
            hideProgressbar();
            setLoadingPage(false);
        }
    }

    private void goToMainActivity() {
        Intent i = new Intent(MyCoffeeSitesListActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void goToCreateNewSiteActivity() {
        Intent activityIntent = new Intent(MyCoffeeSitesListActivity.this, CreateCoffeeSiteActivity.class);
        //activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activityIntent.putExtra("requestCode", CREATE_COFFEESITE_REQUEST);
        startActivityForResult(activityIntent, CREATE_COFFEESITE_REQUEST);
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

        /**
         * Retrieves all CoffeeSites saved in local DB created by logged-in user, excluding
         * CoffeeSites not already uploaded to server i.e. completely new CoffeeSites or modified ones,
         * when Offline.
         */
        myCoffeeSitesViewModel.getUsersCoffeeSitesInDBNotModified(currentUser)
                              .observe(this, myCoffeeSites -> coffeeSitesInDBDownloaded = myCoffeeSites);
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
        // (and thus won't be supporting component replacement by other applications).
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
                coffeeSiteCUDOperationsService.removeUploadOperationsListener(this);
            }
            // Release information about the service's state.
            coffeeSiteCUDOperationsServiceConnector.removeCoffeeSiteServiceConnectionListener(this);
            unbindService(coffeeSiteCUDOperationsServiceConnector);
            mShouldUnbindCoffeeSiteCUDOperationsService = false;
        }
    }

    /********* CoffeeSiteCUDOperationsService ****** END ********/

    /******************* CoffeeSiteImageService *************** START ******************/

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindCoffeeSiteImageService;

    private void doBindCoffeeSiteImageService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
        coffeeSiteImageServiceConnector = new CoffeeSiteImageServiceConnector();
        coffeeSiteImageServiceConnector.addCoffeeSiteImageServiceConnectionListener(this);

        if (bindService(new Intent(this, CoffeeSiteImageService.class),
                coffeeSiteImageServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindCoffeeSiteImageService = true;
        } else {
            Log.e(TAG, "Error: The requested 'CoffeeSiteImageService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    /**
     * Called when CoffeeSiteImageService is Connected
     */
    @Override
    public void onCoffeeSiteImageServiceConnected() {
        coffeeSiteImageService = coffeeSiteImageServiceConnector.getCoffeeSiteImageService();
        if (coffeeSiteImageService != null) {
            coffeeSiteImageService.addImageOperationsResultListener(this);
        }
    }

    private void doUnbindCoffeeSiteImageService() {
        if (mShouldUnbindCoffeeSiteImageService) {
            if (coffeeSiteImageService != null) {
                coffeeSiteImageService.removeImageOperationsResultListener(this);
            }
            // Release information about the service's state.
            coffeeSiteImageServiceConnector.removeCoffeeSiteImageServiceConnectionListener(this);
            unbindService(coffeeSiteImageServiceConnector);
            mShouldUnbindCoffeeSiteImageService = false;
        }
    }

    /******************* CoffeeSiteImageService *************** END ******************/

    /* */

    private void prepareAndActivateRecyclerView() {
        //Bundle extras = getIntent().getExtras();
        recyclerView = findViewById(R.id.my_coffeesite_list);
        assert recyclerView != null;

        recyclerView.setLayoutManager(layoutManager);
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerViewAdapter = new MyCoffeeSiteItemRecyclerViewAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);
        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        if (content != null) {
            recyclerViewAdapter.setCoffeeSites(content);
        } else {
            recyclerViewAdapter.clearList();
        }
    }


    private void startMyCoffeeSitesLoadOperation() {
        if (coffeeSiteLoadOperationsService != null
                && !isLoadingPage()
                && Utils.isOnline(getApplicationContext())
                && isShowNotModifiedCoffeeSites()) {
            showProgressbar();
            setLoadingPage(true);
            coffeeSiteLoadOperationsService.findCoffeeSitesPageFromCurrentUser(1, PAGE_SIZE);
        }
    }

    /**
     * Runs AsyncTask to upload all CoffeeSites created/updated when Offline.
     */
    private void startUploadCoffeeSitesOperation(List<CoffeeSite> coffeeSites) {
        if (coffeeSiteCUDOperationsService != null && !isLoadingPage()
                && !coffeeSites.isEmpty()) {
            showProgressbar();
            setLoadingPage(true);
            coffeeSiteCUDOperationsService.uploadCoffeeSites(coffeeSites);
        }
    }

    /** Uploading result processing ***/

    /**
     * Keeps info about number of CoffeeSites (saved in local DB only) with image to be uploaded.
     * Used to check, if all images upload already finished
     */
    private int coffeeSitesWithImageToBeUploaded = 0;

    /**
     * Keeps info about number of CoffeeSites (saved in local DB only) whose
     * image was already uploaded.
     * Used to check, if all images upload already finished
     */
    private int coffeeSitesWithImageUploaded = 0;

    /**
     * List of CoffeeSites returned from server after their create/update
     */
    private List<CoffeeSite> returnedCoffeeSitesAfterUpload;

    @Override
    public void onCoffeeSitesUploaded(List<CoffeeSite> returnedCoffeeSites, String error) {
        hideProgressbar();
        setLoadingPage(false);
        Log.i(TAG, "Processing uploaded CoffeeSites. Error? :" + error);
        if (!error.isEmpty()) {
            showCoffeeSitesUploadFailure(error);
            Log.w(TAG, "CoffeeSites upload failure. Error: " + error);
        }
        else {
            Log.i(TAG, "CoffeeSites uploaded. Result: " + ((returnedCoffeeSites != null) ? "OK" : "FAILED"));

            showCoffeeSitesUploadSuccess();

            if (returnedCoffeeSites != null) {
                this.returnedCoffeeSitesAfterUpload = returnedCoffeeSites;
                coffeeSitesWithImageToBeUploaded = 0;
                for (CoffeeSite cs : getNotSavedCoffeeSites()) {
                    if (!cs.getMainImageFilePath().isEmpty()) {
                        coffeeSitesWithImageToBeUploaded++;
                    }
                }
                if (coffeeSitesWithImageToBeUploaded > 0) {
                    // Start upload of the CoffeeSite's images
                    coffeeSitesWithImageUploaded = 0;
                    //showProgressbarAndDisableMenuItems();
                    showProgressbar();
                    for (CoffeeSite cs : this.returnedCoffeeSitesAfterUpload) {
                        // As the returned, saved CoffeeSites have right ID, they do not have getMainImageFilePath correct
                        // Therefore, we need to find correct getMainImageFilePath from notSavedCoffeeSites
                        // The time of creation is used to assign correct MainImageFilePath to correct CoffeeSite
                        String imageFilePath = getMainImageFilePath(cs);
                        if (!imageFilePath.isEmpty()) {
                            uploadCoffeeSiteImage(ImageUtil.getImageFile(getApplicationContext(), imageFilePath), cs);
                        }
                    }
                } else {
                    coffeeSiteCUDOperationsService.deleteAllNotSavedCoffeeSitesFromDB();
                    setShowNotModifiedCoffeeSites(true);
                    reloadAllUsersCoffeeSites();
                }
            }
        }
    }

    /**
     * Helper method to find MainImageFilePath relevant to CoffeeSite returned from server after upload.
     * As they have new/different ID, then those saved in local DB {@code notSavedCoffeeSites} and empty
     * MainImageFilePath, the corresponding CoffeeSite from {@code notSavedCoffeeSites} (with correct MainImageFilePath)
     * will be assigned based on Date of creation, which is unique in this scenario.
     *
     * @param returnedCS
     * @return
     */
    private String getMainImageFilePath(CoffeeSite returnedCS) {
        for (CoffeeSite cs : getNotSavedCoffeeSites()) {
            if (!cs.getMainImageFilePath().isEmpty()) {
                if (cs.getCreatedOn().equals(returnedCS.getCreatedOn())) {
                    return cs.getMainImageFilePath();
                }
            }
        }
        return "";
    }


    private void uploadCoffeeSiteImage(File imageFile, CoffeeSite coffeeSite) {
        if (coffeeSiteImageService != null
                && imageFile.exists() && coffeeSite != null)  {
            setLoadingPage(true);
            coffeeSiteImageService.uploadImage(imageFile, coffeeSite);
        }
    }

    /**
     * Callback from CoffeeSite's image upload to server REST AsyncTask request
     *
     * @param coffeeSite
     * @param imageSaveResult
     */
    @Override
    public void onImageSaveSuccess(CoffeeSite coffeeSite, String imageSaveResult) {
        Log.i(TAG, "Image uploaded. CoffeeSite name: " + coffeeSite.getName());
        setLoadingPage(false);
        coffeeSitesWithImageUploaded++;
        coffeeSite.setMainImageURL(imageSaveResult);

        updateCoffeeSiteInDB(coffeeSite);

        if (coffeeSitesWithImageToBeUploaded == coffeeSitesWithImageUploaded) {
            coffeeSiteCUDOperationsService.deleteAllNotSavedCoffeeSitesFromDB();
            // Now uploade CoffeeSites are to be shown
            setShowNotModifiedCoffeeSites(true);
            showCoffeeSitesImagesUploadSuccess();
            hideProgressbar();
            reloadAllUsersCoffeeSites();
        }
    }

    @Override
    public void onImageSaveFailure(CoffeeSite coffeeSite, String imageSaveResult) {
        setLoadingPage(false);
        coffeeSitesWithImageUploaded++;
        coffeeSite.restoreId(); // to be still editable/deletable
        showImageUploadFailure(imageSaveResult);
        if (coffeeSitesWithImageToBeUploaded == coffeeSitesWithImageUploaded) {
            coffeeSiteCUDOperationsService.deleteAllNotSavedCoffeeSitesFromDB();

            // Now uploaded CoffeeSites are to be shown
            setShowNotModifiedCoffeeSites(true);
            hideProgressbar();
            reloadAllUsersCoffeeSites();
        }
    }

    private void updateCoffeeSiteInDB(CoffeeSite coffeeSite) {
        if (coffeeSiteCUDOperationsService != null) {
            coffeeSiteCUDOperationsService.updateInDB(coffeeSite);
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
        // (and thus won't be supporting component replacement by other applications).
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
        // (and thus won't be supporting component replacement by other applications).
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
                coffeeSiteCUDOperationsService.addUploadOperationsListener(this);
                recyclerViewAdapter.setCoffeeSiteCUDOperationsService(coffeeSiteCUDOperationsService);

                doBindCoffeeSiteLoadOperationsService();
            }
        }

        if (coffeeSiteLoadOperationsServiceConnector != null && coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService() != null) {
            if (coffeeSiteLoadOperationsService == null) {
                coffeeSiteLoadOperationsService = coffeeSiteLoadOperationsServiceConnector.getCoffeeSiteService();
                coffeeSiteLoadOperationsService.addLoadOperationsListener(this);

                /*
                 * Get CoffeeSites saved in local DB, which are not uploaded to server, first.
                 * If none is available, reload CoffeeSites from server or DB.
                 */
                myCoffeeSitesViewModel.getCoffeeSitesNotSavedOnServer().observe(this, new Observer<List<CoffeeSite>>() {
                    @Override
                    public void onChanged(@Nullable final List<CoffeeSite> coffeeSites) {
                        setNotSavedCoffeeSites(coffeeSites);
                        setShowNotModifiedCoffeeSites(coffeeSites == null || coffeeSites.size() <= 0
                                || Utils.isOnline(getApplicationContext()));
                        // All services ready, start loading all CoffeeSites if we are online and
                        // there are no Offline data available
                        if (isShowNotModifiedCoffeeSites()) {
                            reloadAllUsersCoffeeSites();
                        } else if (switchAB != null && coffeeSites != null && coffeeSites.size() > 0) {
                            switchAB.setChecked(true); // show Offline modified/created CoffeeSites
                        }
                    }
                });
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

    /*** Processing of Results of calling CoffeeSites services operations  ***/

    @Override
    public void onCoffeeSiteListFromLoggedInUserLoaded(List<CoffeeSite> coffeeSites, String error) {
        setLoadingPage(false);
        hideProgressbar();

        if (!error.isEmpty()) {
            showMyCoffeeSitesLoadFailure(error);
            Log.w(TAG, "COFFEE_SITES_FROM_CURRENT_USER_LOAD. Error: " + error);
        }
        else {
            showMyCoffeeSitesLoadSuccess();
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_LOAD. Result: OK");
            content = coffeeSites;
            if (content != null) {
                recyclerViewAdapter.setCoffeeSites(content);
            }
        }
    }

    @Override
    public void onCoffeeSiteFirstPageFromLoggedInUserLoaded(CoffeeSitePageEnvelope coffeeSitesPage, String error) {
        setLoadingPage(false);
        hideProgressbar();

        if (!error.isEmpty()) {
            showMyCoffeeSitesLoadFailure(error);
            Log.w(TAG, "COFFEE_SITES_FROM_CURRENT_USER_FIRST_PAGE_LOAD. Error: " + error);
        }
        else {
            showMyCoffeeSitesLoadSuccess();
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_FIRST_PAGE_LOAD. Result: OK");
            if (coffeeSitesPage != null) {
                recyclerViewAdapter.clearList();
                content.clear();
                content.addAll(coffeeSitesPage.getContent());
                recyclerViewAdapter.setCoffeeSites(content);
                if (!coffeeSitesPage.getLast()) {
                    recyclerViewAdapter.addFooter();
                } else {
                    isLastPage = true;
                }
            }
        }
    }

    @Override
    public void onCoffeeSiteNextPageFromLoggedInUserLoaded(CoffeeSitePageEnvelope coffeeSitesPage, String error) {
        setLoadingPage(false);
        hideProgressbar();

        if (!error.isEmpty()) {
            showMyCoffeeSitesLoadFailure(error);
            Log.w(TAG, "COFFEE_SITES_FROM_CURRENT_USER_NEXT_PAGE_LOAD. Error: " + error);
        }
        else {
            showMyCoffeeSitesLoadSuccess();
            Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_NEXT_PAGE_LOAD. Result: OK");
            if (coffeeSitesPage != null) {
                content.addAll(coffeeSitesPage.getContent());
                recyclerViewAdapter.setCoffeeSites(content);
                if (!coffeeSitesPage.getLast()) {
                    recyclerViewAdapter.addFooter();
                } else {
                    isLastPage = true;
                }
            }
        }
    }



    /** RECYCLER VIEW OnScrollListener **/

    private final RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
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

            if (!isLoadingPage() && !isLastPage && Utils.isOnline(getApplicationContext())
                    && isShowNotModifiedCoffeeSites()) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                    loadMoreItems();
                }
            }
        }
    };

    private void loadMoreItems() {
        currentPage += 1;
        showProgressbar();
        if (coffeeSiteLoadOperationsService != null) {
            setLoadingPage(true);
            coffeeSiteLoadOperationsService.findCoffeeSitesPageFromCurrentUser(currentPage, PAGE_SIZE);
        }
    }

    /**
     * Helper method to to show progressBar and disable menu items, when loading CoffeeSites or performing
     * other long time operations.
     */
    public void showProgressbar() {
        if (loadMyCoffeeSitesProgressBar.getVisibility() == View.GONE) {
            loadMyCoffeeSitesProgressBar.setVisibility(View.VISIBLE);
        }
        if (switchAB != null) {
            switchAB.setEnabled(false);
        }
        if (addCoffeeSiteMenuItem != null) {
            addCoffeeSiteMenuItem.setEnabled(false);
        }
        if (uploadCoffeeSitesMenuItem != null) {
            uploadCoffeeSitesMenuItem.setEnabled(false);
        }
    }

    /**
     * Helper method to to hide progressBar and enable menu items, when loading CoffeeSites or performing
     * other long time operations.
     */
    public void hideProgressbar() {
        loadMyCoffeeSitesProgressBar.setVisibility(View.GONE);
        if (switchAB != null) {
            switchAB.setEnabled(true);
        }
        if (addCoffeeSiteMenuItem != null) {
            addCoffeeSiteMenuItem.setEnabled(true);
        }
        if (uploadCoffeeSitesMenuItem != null) {
            uploadCoffeeSitesMenuItem.setEnabled(true);
        }
    }

    private void showMyCoffeeSitesLoadSuccess()
    {}


    private void showMyCoffeeSitesLoadFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.my_coffeesites_load_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSitesUploadSuccess() {
        Snackbar mySnackbar = Snackbar.make(contextView, getString(R.string.coffeesites_upload_success), Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showCoffeeSitesImagesUploadSuccess() {
        Snackbar mySnackbar = Snackbar.make(contextView, getString(R.string.coffeesites_images_upload_success), Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }


    private void showCoffeeSitesUploadFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffeesites_upload_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    private void showImageUploadFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffeesites_image_upload_failure);
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
    //TODO - neslo by predelat, tohle vypada skarede!
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult() from CreateCoffeeSiteActivity. Request code: " + requestCode + ". Result code: " + resultCode);

        // Check which request we're responding to
        // Modified CoffeeSite - update this CoffeeSite in the current recyclerViewAdapter list
        if (resultCode == RESULT_OK) {

            /* Mode of CreateCoffeeSiteActivity as the original request code might be modified:
             * MODE_CREATE = 0; // only when CreateCoffeeSiteActivity was called from MainActivity, cannot appear here
             * MODE_MODIFY = 1;
             * MODE_CREATE_FROM_MYCOFFEESITESACTIVITY = 2; // originally Modify of the offline CoffeeSite was changed because CoffeeSite was saved
             */
            int mode = data.getExtras().getInt("mode");
            if (mode == MODE_CREATE_FROM_MYCOFFEESITESACTIVITY) {
                setShowNotModifiedCoffeeSites(true);
            }
            // New CoffeeSite - reload all CoffeeSites of User
            if (requestCode == CREATE_COFFEESITE_REQUEST || mode == MODE_CREATE_FROM_MYCOFFEESITESACTIVITY) {
                // User created new CoffeeSite, refresh the whole list of MyCoffeeSites
                if (isShowNotModifiedCoffeeSites()) {
                    if (switchAB.isChecked()) {
                        switchAB.setChecked(false);
                    } else {
                        reloadAllUsersCoffeeSites();
                    }
                } else {
                    // if there are unsaved CoffeeSites, show them 
                    if (getNotSavedCoffeeSites() != null && !getNotSavedCoffeeSites().isEmpty()) {
                        setShowNotModifiedCoffeeSites(false); // show modified, not saved CoffeeSites if there are some as first option
                        switchAB.setChecked(true); // this invokes recyclerView to show newly created CoffeeSites, see switchAB.setOnCheckedChangeListener
                    }
                }
                return;
            }

            if (requestCode == EDIT_COFFEESITE_REQUEST) {
                CoffeeSite editedCoffeeSite = (CoffeeSite) data.getExtras().getParcelable("coffeeSite");
                int coffeeSitePositionInRecyclerView = data.getExtras().getInt("coffeeSitePosition");
                if (recyclerViewAdapter != null) {
                    recyclerViewAdapter.updateEditedCoffeeSite(editedCoffeeSite, coffeeSitePositionInRecyclerView);
                }
            }
        }
    }

    /**
     * Activated only, when Author's comment is inserted/updated. Not needed to update recycler view?
     *
     * @param updatedCoffeeSite
     * @param error
     */
    @Override
    public void onCoffeeSiteUpdated(CoffeeSite updatedCoffeeSite, String error) {
        //hideProgressbarAndEnableMenuItems();
        hideProgressbar();

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
        if (state != null) {
            mListState = state.getParcelable(LIST_STATE_KEY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListState != null) {
            layoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public void onPause() {
        // save RecyclerView state
        Bundle listState = new Bundle();
        if (recyclerView != null) {
            mListState = recyclerView.getLayoutManager().onSaveInstanceState();
            listState.putParcelable(LIST_STATE_KEY, mListState);
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeStateReceiver, filter);
    }

    @Override
    protected void onStop() {
        // To be sure, that progressBar is hiden
        hideProgressbar();
        unregisterReceiver(networkChangeStateReceiver);
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
        doUnbindCoffeeSiteImageService();

        currentPage = 1;
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);

        super.onDestroy();
    }

    public boolean isSwitchABChecked() {
        return switchAB != null ? switchAB.isChecked() : false;
    }

    /**
     * Used when internet connectivity status has changed in NetworkStateReceiver
     *
     * @param isOnline
     */
    public void updateActivityUI(boolean isOnline) {
        if (switchAB != null && switchAB.isChecked()) {
            uploadCoffeeSitesMenuItem.setVisible(isOnline);
        } else {
            reloadAllUsersCoffeeSites();
        }
    }
}
