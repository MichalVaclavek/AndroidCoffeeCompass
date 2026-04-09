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

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.BaseActivity;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.DataForOfflineModePreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.coffeesite.CoffeeSitePageEnvelope;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteLoadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteServiceCUDOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.coffeesite.CoffeeSiteUploadServiceOperationsListener;
import cz.fungisoft.coffeecompass2.activity.interfaces.images.CoffeeSiteImageManageListener;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.asynctask.image.ImageUploadNewApiAsyncTask;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.models.MyCoffeeSitesViewModel;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.ImageObject;
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
 * Also allows upload of the CoffeeSites created when offline.
 */
public class MyCoffeeSitesListActivity extends BaseActivity
                                       implements UserAccountServiceConnectionListener,
                                                  CancelCoffeeSiteDialogFragment.CancelCoffeeSiteDialogListener,
                                                  InsertAuthorCommentDialogFragment.InsertAuthorCommentDialogListener,
                                                  UploadCoffeeSitesDialogFragment.UploadCoffeeSitesDialogListener,
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
    private static List<CoffeeSite> notUploadedCoffeeSites;

    /**
     * Setter and getter synchronized
     *
     * @return
     */
    private static synchronized List<CoffeeSite> getNotUploadedCoffeeSites() {
        return notUploadedCoffeeSites;
    }

    public static synchronized void setNotUploadedCoffeeSites(List<CoffeeSite> notUploadedCoffeeSites) {
        MyCoffeeSitesListActivity.notUploadedCoffeeSites = notUploadedCoffeeSites;
    }

    public synchronized void removeCoffeeSiteFromCachedLists(CoffeeSite coffeeSite) {
        if (coffeeSite == null) {
            return;
        }

        removeCoffeeSiteById(content, coffeeSite.getId());
        removeCoffeeSiteById(notUploadedCoffeeSites, coffeeSite.getId());
        removeCoffeeSiteById(coffeeSitesInDBDownloaded, coffeeSite.getId());
    }

    private void removeCoffeeSiteById(List<CoffeeSite> coffeeSites, String coffeeSiteId) {
        if (coffeeSites == null || coffeeSiteId == null || coffeeSiteId.isEmpty()) {
            return;
        }

        Iterator<CoffeeSite> iterator = coffeeSites.iterator();
        while (iterator.hasNext()) {
            CoffeeSite coffeeSite = iterator.next();
            if (coffeeSite != null && coffeeSiteId.equals(coffeeSite.getId())) {
                iterator.remove();
            }
        }
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

        // Starts loading of all CoffeeSites related services, see onCoffeeSiteServiceConnected() to see next steps
        // If services are connected correctly, initiates loading of user's CoffeeSites
        doBindCoffeeSiteStatusChangeService();

        // To save images of the CoffeeSites created/updated in Offline
        doBindCoffeeSiteImageService();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });

        prepareAndActivateRecyclerView();
    }


    public void  openCreateCoffeeSiteActivityForResult() {
        Intent intent = new Intent(this, CreateCoffeeSiteActivity.class);
        createCoffeeSiteActivityResultLauncher.launch(intent);
    }

    private void handleBackPressed() {
        if (!isLoadingPage()) {
            finish();
        }
    }

    /**
     * Switch to swap between two modes of this list. Either standard list of my CoffeeSites
     * is shown (i.e. my CoffeeSites on server or in local DB)
     * or list of new/updated CoffeeSites in local DB, which are not saved on server yet.
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
        switchAB.setChecked(false); // default. show user's sites already on server or downloaded ones if offline

        // Switch button to switch between list of CoffeeSites not saved on server yet  (i.e. new and modified CoffeeSites)
        // and list of all CoffeeSites created by user either loaded from server or from DB
        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setShowNotModifiedCoffeeSites(false);
                    showNotUploadedCoffeeSites();
                } else {
                    setShowNotModifiedCoffeeSites(true);
                    reloadAllUsersCoffeeSites();
                }
            }
        });

        return true;
    }

    private void showNotUploadedCoffeeSites() {
        List<CoffeeSite> notSavedOnServerCoffeeSites = getNotUploadedCoffeeSites();
        if (notSavedOnServerCoffeeSites != null) {
            uploadCoffeeSitesMenuItem.setVisible(notSavedOnServerCoffeeSites.size() > 0 && Utils.isOnline(getApplicationContext()));
            if (recyclerViewAdapter != null) {
                recyclerViewAdapter.clearList();
                content = new ArrayList<>(notSavedOnServerCoffeeSites);
                recyclerViewAdapter.setCoffeeSites(content);
            }
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_sites_activity_toolbar_tosave_label);
            setLoadingPage(false);
            hideProgressbar();
        }
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
            handleBackPressed();
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
            showUploadCoffeeSitesConfirmDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadAllUsersCoffeeSites() {
        boolean offLineModeOn = Utils.isOfflineModeOn(getApplicationContext());
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_sites_activity_toolbar_mainlabel);
        if (uploadCoffeeSitesMenuItem != null) {
            uploadCoffeeSitesMenuItem.setVisible(false);
        }
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.clearList();
        }
        if (!offLineModeOn) {
            startMyCoffeeSitesLoadOperation();
        }
        if (offLineModeOn) {
            showCoffeeSitesDownloaded();
        }
    }

    private void showCoffeeSitesDownloaded() {
        if (coffeeSitesInDBDownloaded != null) {
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
                              .observe(this, myCoffeeSites -> {
                                  coffeeSitesInDBDownloaded = myCoffeeSites;
                                  if (Utils.isOfflineModeOn(getApplicationContext())
                                        && isShowNotModifiedCoffeeSites()) {
                                      showCoffeeSitesDownloaded();
                                  }
                              });

        // All needed services connected here, download user's Coffee sites
        reloadAllUsersCoffeeSites();
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
        recyclerView = findViewById(R.id.my_coffeesite_list);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(layoutManager);
            setupRecyclerView(recyclerView);
        }
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
                && currentUser != null
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

    private final ArrayList<PendingOfflineImageUpload> pendingOfflineImageUploads = new ArrayList<>();
    private final ArrayList<String> pendingOfflineImagePathsToDelete = new ArrayList<>();
    private int pendingOfflineImageUploadIndex = -1;
    private int pendingOfflineImageUploadFailures = 0;

    private static class PendingOfflineImageUpload {
        private final CoffeeSite coffeeSite;
        private final File imageFile;
        private final String imageType;

        PendingOfflineImageUpload(CoffeeSite coffeeSite, File imageFile, String imageType) {
            this.coffeeSite = coffeeSite;
            this.imageFile = imageFile;
            this.imageType = imageType;
        }
    }

    @Override
    public void onCoffeeSitesUploaded(List<CoffeeSite> returnedCoffeeSites, String error) {
        hideProgressbar();
        setLoadingPage(false);
        Log.i(TAG, "Processing uploaded CoffeeSites. Error? :" + error);
        if (!error.isEmpty()) {
            showCoffeeSitesUploadFailure(error);
            Log.w(TAG, "CoffeeSites upload failure. Error: " + error);
            return;
        }

        Log.i(TAG, "CoffeeSites uploaded. Result: " + ((returnedCoffeeSites != null) ? "OK" : "FAILED"));

        showCoffeeSitesUploadSuccess();

        if (returnedCoffeeSites != null) {
            switchAB.setChecked(false);
            List<CoffeeSite> originalCoffeeSitesWithImages = getOriginalCoffeeSitesWithImages();
            coffeeSitesWithImageToBeUploaded = 0;
            clearPendingOfflineImageUploads();

            if (originalCoffeeSitesWithImages.isEmpty()) {
                // delete already uploaded coffee sites from local DB, if without images
                // if with images, then will be deleted after images upload
                coffeeSiteCUDOperationsService.deleteAllNotSavedCoffeeSitesFromDB();
                return;
            }

            coffeeSitesWithImageUploaded = 0;
            showProgressbar();
            List<CoffeeSite> unmatchedOriginalCoffeeSites = new ArrayList<>(originalCoffeeSitesWithImages);
            for (CoffeeSite returnedCoffeeSite : returnedCoffeeSites) {
                CoffeeSite originalCoffeeSite = findOriginalCoffeeSiteForReturnedSite(
                        returnedCoffeeSite, unmatchedOriginalCoffeeSites);
                if (originalCoffeeSite == null) {
                    continue;
                }

                queueOfflineImageUploads(returnedCoffeeSite, originalCoffeeSite);
            }

            if (coffeeSitesWithImageToBeUploaded <= 0) {
                hideProgressbar();
                coffeeSiteCUDOperationsService.deleteAllNotSavedCoffeeSitesFromDB();
                reloadAllUsersCoffeeSites();
                return;
            }

            pendingOfflineImageUploadIndex = 0;
            uploadNextPendingOfflineImage();
        }
    }

    /**
     * Returns the subset of offline CoffeeSites that still have a local image file to upload.
     */
    @NonNull
    private List<CoffeeSite> getOriginalCoffeeSitesWithImages() {
        List<CoffeeSite> originalCoffeeSites = getNotUploadedCoffeeSites();
        if (originalCoffeeSites == null || originalCoffeeSites.isEmpty()) {
            return Collections.emptyList();
        }

        List<CoffeeSite> coffeeSitesWithImages = new ArrayList<>();
        for (CoffeeSite coffeeSite : originalCoffeeSites) {
            if (coffeeSite != null && hasOfflineLocalImages(coffeeSite)) {
                coffeeSitesWithImages.add(coffeeSite);
            }
        }
        return coffeeSitesWithImages;
    }

    @Nullable
    private CoffeeSite findOriginalCoffeeSiteForReturnedSite(CoffeeSite returnedCoffeeSite,
                                                             List<CoffeeSite> unmatchedOriginalCoffeeSites) {
        if (returnedCoffeeSite == null || unmatchedOriginalCoffeeSites == null || unmatchedOriginalCoffeeSites.isEmpty()) {
            return null;
        }

        for (Iterator<CoffeeSite> iterator = unmatchedOriginalCoffeeSites.iterator(); iterator.hasNext(); ) {
            CoffeeSite originalCoffeeSite = iterator.next();
            if (isSameCoffeeSiteAfterUpload(returnedCoffeeSite, originalCoffeeSite)) {
                iterator.remove();
                return originalCoffeeSite;
            }
        }

        CoffeeSite fallbackCoffeeSite = unmatchedOriginalCoffeeSites.remove(0);
        Log.w(TAG, "Falling back to positional image pairing for CoffeeSite: " + returnedCoffeeSite.getName());
        return fallbackCoffeeSite;
    }

    private boolean isSameCoffeeSiteAfterUpload(CoffeeSite returnedCoffeeSite, CoffeeSite originalCoffeeSite) {
        if (returnedCoffeeSite == null || originalCoffeeSite == null) {
            return false;
        }

        boolean sameName = Objects.equals(returnedCoffeeSite.getName(), originalCoffeeSite.getName());
        boolean sameLatitude = Double.compare(returnedCoffeeSite.getLatitude(), originalCoffeeSite.getLatitude()) == 0;
        boolean sameLongitude = Double.compare(returnedCoffeeSite.getLongitude(), originalCoffeeSite.getLongitude()) == 0;
        boolean sameCity = Objects.equals(returnedCoffeeSite.getMesto(), originalCoffeeSite.getMesto());
        boolean sameStreet = Objects.equals(returnedCoffeeSite.getUliceCP(), originalCoffeeSite.getUliceCP());

        if (sameName && sameLatitude && sameLongitude && sameCity && sameStreet) {
            return true;
        }

        return Objects.equals(returnedCoffeeSite.getCreatedOnString(), originalCoffeeSite.getCreatedOnString());
    }

    private void queueOfflineImageUploads(CoffeeSite returnedCoffeeSite, CoffeeSite originalCoffeeSite) {
        List<String> localImagePaths = getOfflineLocalImagePaths(originalCoffeeSite);
        if (localImagePaths.isEmpty()) {
            return;
        }

        String imageFilePath = localImagePaths.get(0);
        if (imageFilePath == null || imageFilePath.isEmpty()) {
            return;
        }

        File imageFile = ImageUtil.getImageFile(imageFilePath);
        if (!imageFile.exists()) {
            Log.w(TAG, "Image file for uploaded CoffeeSite does not exist: " + imageFilePath);
            return;
        }

        returnedCoffeeSite.setImageFileName(returnedCoffeeSite.getDefaultImageFileName());
        pendingOfflineImageUploads.add(new PendingOfflineImageUpload(
                returnedCoffeeSite,
                imageFile,
                "main"));
        pendingOfflineImagePathsToDelete.add(imageFilePath);
        coffeeSitesWithImageToBeUploaded++;
    }

    private boolean hasOfflineLocalImages(CoffeeSite coffeeSite) {
        return !getOfflineLocalImagePaths(coffeeSite).isEmpty();
    }

    @NonNull
    private List<String> getOfflineLocalImagePaths(@Nullable CoffeeSite coffeeSite) {
        if (coffeeSite == null) {
            return Collections.emptyList();
        }

        List<String> localImagePaths = coffeeSite.getLocalImagePaths();
        if (localImagePaths != null && !localImagePaths.isEmpty()) {
            return localImagePaths;
        }

        if (coffeeSite.getMainImageFilePath() != null && !coffeeSite.getMainImageFilePath().isEmpty()) {
            return Collections.singletonList(coffeeSite.getMainImageFilePath());
        }

        return Collections.emptyList();
    }


    private void uploadCoffeeSiteImage(File imageFile, CoffeeSite coffeeSite) {
        if (coffeeSiteImageService != null
                && imageFile.exists() && coffeeSite != null)  {
            setLoadingPage(true);
            coffeeSiteImageService.uploadImage(imageFile, coffeeSite);
        }
    }

    private void uploadNextPendingOfflineImage() {
        if (pendingOfflineImageUploadIndex < 0
                || pendingOfflineImageUploadIndex >= pendingOfflineImageUploads.size()) {
            finishPendingOfflineImageUploads();
            return;
        }

        if (userAccountService == null || userAccountService.getLoggedInUser() == null) {
            pendingOfflineImageUploadFailures++;
            finishPendingOfflineImageUploads();
            return;
        }

        setLoadingPage(true);
        PendingOfflineImageUpload pendingUpload = pendingOfflineImageUploads.get(pendingOfflineImageUploadIndex);
        new ImageUploadNewApiAsyncTask(new CoffeeSiteImageManageListener() {
            @Override
            public void onImageObjectLoaded(ImageObject imageObject) {
            }

            @Override
            public void onImageObjectLoadFailed(Result.Error error) {
            }

            @Override
            public void onImageUploaded(String imageExtId) {
                onPendingOfflineImageUploaded(pendingUpload);
            }

            @Override
            public void onImageUploadFailed(Result.Error error) {
                onPendingOfflineImageUploadFailed(error);
            }

            @Override
            public void onImageDeleted(String imageExtId) {
            }

            @Override
            public void onImageDeleteFailed(Result.Error error) {
            }
        }, userAccountService, pendingUpload.imageFile, pendingUpload.coffeeSite.getId(), pendingUpload.imageType).execute();
    }

    private void onPendingOfflineImageUploaded(PendingOfflineImageUpload pendingUpload) {
        setLoadingPage(false);
        coffeeSitesWithImageUploaded++;
        if ("main".equalsIgnoreCase(pendingUpload.imageType)) {
            recyclerViewAdapter.invalidateImageUrl(pendingUpload.coffeeSite);
        }
        pendingOfflineImageUploadIndex++;
        uploadNextPendingOfflineImage();
    }

    private void onPendingOfflineImageUploadFailed(@Nullable Result.Error error) {
        setLoadingPage(false);
        coffeeSitesWithImageUploaded++;
        pendingOfflineImageUploadFailures++;
        Log.e(TAG, "Offline image upload failed: "
                + (error != null && error.getDetail() != null ? error.getDetail() : ""));
        pendingOfflineImageUploadIndex++;
        uploadNextPendingOfflineImage();
    }

    private void finishPendingOfflineImageUploads() {
        setLoadingPage(false);
        int uploadFailures = pendingOfflineImageUploadFailures;
        deletePendingOfflineImageFiles();
        clearPendingOfflineImageUploads();
        coffeeSiteCUDOperationsService.deleteAllNotSavedCoffeeSitesFromDB();
        if (uploadFailures == 0) {
            showCoffeeSitesImagesUploadSuccess();
        } else {
            showImageUploadFailure("");
        }
        hideProgressbar();
        reloadAllUsersCoffeeSites();
    }

    private void deletePendingOfflineImageFiles() {
        for (String imageFilePath : pendingOfflineImagePathsToDelete) {
            if (imageFilePath == null || imageFilePath.isEmpty()) {
                continue;
            }
            File imageFile = ImageUtil.getImageFile(imageFilePath);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
    }

    private void clearPendingOfflineImageUploads() {
        pendingOfflineImageUploads.clear();
        pendingOfflineImagePathsToDelete.clear();
        pendingOfflineImageUploadIndex = -1;
        pendingOfflineImageUploadFailures = 0;
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

        // Invalidate Picasso image
        recyclerViewAdapter.invalidateImageUrl(coffeeSite);
        updateCoffeeSiteInDB(coffeeSite);

        if (coffeeSitesWithImageToBeUploaded == coffeeSitesWithImageUploaded) {
            coffeeSiteCUDOperationsService.deleteAllNotSavedCoffeeSitesFromDB();
            // Now uploaded CoffeeSites are to be shown
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
     * 4) UserAccountService
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
                 * All other services connected, connect UserService and then user's CoffeeSites (in onUserAccountServiceConnected)
                 */
                doBindUserAccountService();

                /*
                 * Get CoffeeSites saved in local DB, which are not uploaded to server
                 */
                myCoffeeSitesViewModel.getCoffeeSitesNotSavedOnServer().observe(this, new Observer<List<CoffeeSite>>() {
                    @Override
                    public void onChanged(@Nullable final List<CoffeeSite> notSavedCoffeeSites) {
                        setNotUploadedCoffeeSites(notSavedCoffeeSites);
                        if (notSavedCoffeeSites != null
                                && !notSavedCoffeeSites.isEmpty()
                                && !isShowNotModifiedCoffeeSites()) {
                            showNotUploadedCoffeeSites();
                        }
                    }
                });
                myCoffeeSitesViewModel.getNumOfCoffeeSitesNotSavedOnServer().observe(this, new Observer<Integer>() {
                    @Override
                    public void onChanged(@Nullable Integer numOfSitesNotSavedOnServer) {
                        if (numOfSitesNotSavedOnServer != null) {
                            switchAB.setVisibility(numOfSitesNotSavedOnServer > 0 ? View.VISIBLE : View.GONE);
                            uploadCoffeeSitesMenuItem.setVisible(!isShowNotModifiedCoffeeSites() && numOfSitesNotSavedOnServer > 0 && Utils.isOnline(getApplicationContext()));
                            if (numOfSitesNotSavedOnServer == 0) {
                                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_sites_activity_toolbar_mainlabel);
                                setShowNotModifiedCoffeeSites(true);
                                reloadAllUsersCoffeeSites();
                            }
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult() from CreateCoffeeSiteActivity. Request code: " + requestCode + ". Result code: " + resultCode);

        // Check which request we're responding to
        // Modified CoffeeSite - update this CoffeeSite in the current recyclerViewAdapter list
        if (resultCode == RESULT_OK) {
            /*
             * Mode of CreateCoffeeSiteActivity as the original request code might be modified:
             * MODE_CREATE = 0; // only when CreateCoffeeSiteActivity was called from MainActivity, cannot appear here
             * MODE_MODIFY = 1;
             * MODE_CREATE_FROM_MYCOFFEESITESACTIVITY = 2; // originally Modify of the offline CoffeeSite was changed because CoffeeSite was saved
             */
            int mode = data.getExtras().getInt("mode");
            // New CoffeeSite - reload all CoffeeSites of User
            if (requestCode == CREATE_COFFEESITE_REQUEST || mode == MODE_CREATE_FROM_MYCOFFEESITESACTIVITY) {
                // User created new CoffeeSite, refresh the whole list of MyCoffeeSites
                if (isShowNotModifiedCoffeeSites()) {
                    reloadAllUsersCoffeeSites();
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
            if (dialog instanceof UploadCoffeeSitesDialogFragment) {
                onUploadCoffeeSitesDialogPositiveClick((UploadCoffeeSitesDialogFragment) dialog);
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
            if (dialog instanceof UploadCoffeeSitesDialogFragment) {
                onUploadCoffeeSitesDialogNegativeClick((UploadCoffeeSitesDialogFragment) dialog);
            }
        }
    }


    void onUploadCoffeeSitesDialogPositiveClick(UploadCoffeeSitesDialogFragment dialog) {
        if (!Utils.isOnline(getApplicationContext())) {
            Utils.showNoInternetToast(getApplicationContext());
            return;
        }
        // upload CoffeeSites after user's confirmation
        if (currentUser != null && getNotUploadedCoffeeSites() != null) {
            startUploadCoffeeSitesOperation(getNotUploadedCoffeeSites());
        }
    }

    void onUploadCoffeeSitesDialogNegativeClick(UploadCoffeeSitesDialogFragment dialog) {}

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

    /**
     * Show the dialog to insert CoffeeSite's author comment
     */
    private void showUploadCoffeeSitesConfirmDialog() {
        // Create an instance of the dialog fragment and show it
        // Passes CoffeeSite's author comment into the dialog
        UploadCoffeeSitesDialogFragment dialog = newInstance(getNotUploadedCoffeeSites().size());
        dialog.show(getSupportFragmentManager(), "UploadCoffeeSitesDialogFragment");
    }

    private static UploadCoffeeSitesDialogFragment newInstance(int numOfNewOrUpdatedCoffeeSites) {
        UploadCoffeeSitesDialogFragment f = new UploadCoffeeSitesDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("numOfNewOrUpdatedCoffeeSites", numOfNewOrUpdatedCoffeeSites);
        f.setArguments(args);

        return f;
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
