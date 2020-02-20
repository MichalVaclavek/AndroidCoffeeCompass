package cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.utils.Utils;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.CreateCoffeeSiteActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;

import static cz.fungisoft.coffeecompass2.activity.ui.coffeesite.ui.mycoffeesiteslist.MyCoffeeSiteItemRecyclerViewAdapter.EDIT_COFFEESITE_REQUEST;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITES_FROM_CURRENT_USER_LOAD;

/**
 * Activity to load and show list of CoffeeSites created, not Canceled, by logged-in user.
 * Allows to Activate, Deactivate and Cancel every single CoffeeSite in the list.
 * Allows to go back to MainActivity, to go to CreateCoffeeSiteActivity to create new CoffeeSite,
 * and reload the list of sites.
 */
public class MyCoffeeSitesListActivity extends AppCompatActivity
                                       implements UserAccountServiceConnectionListener,
                                                  CancelCoffeeSiteDialogFragment.CancelCoffeeSiteDialogListener {

    private static final String TAG = "MyCoffeeSitesListAct";

    /**
     * The main attribute of activity containing all the CoffeeSites to show
     * on this or child Activities
     */
    private List<CoffeeSite> content;

    /**
     * CoffeeSite selected by user from List to be working on
     */
    //private CoffeeSite selectedCoffeeSite;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private MyCoffeeSiteItemRecyclerViewAdapter recyclerViewAdapter;
    private Parcelable mListState;

    private static final String LIST_STATE_KEY = "CoffeeSiteList";

    private MyCoffeeSitesListActivity.CoffeeSiteServiceOperationsReceiver coffeeSiteServiceReceiver;

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

    private Toolbar myCoffeeSitesToolbar;

    private MenuItem addCoffeeSiteMenuItem;
    private MenuItem reloadMyCoffeeSitesMenuItem;

    /**
     * Request type to ask CreateCoffeeSiteActivity to create new CoffeeSite
     */
    static final int CREATE_COFFEESITE_REQUEST = 2;


    /**
     * Flag to indicato if the list of user's CoffeeSites is being loading
     * to check if the MenuItems should be enabled or disable
     */
    private boolean listOfSitesIsBeingLoading = false;

    /**
     * Getter and setter to be synchronized to update status correctly when
     * CoffeeSites load async task is running.
     * Really needed?
     * @return
     */
    public synchronized boolean isListOfSitesIsBeingLoading() {
        return listOfSitesIsBeingLoading;
    }

    public synchronized void setListOfSitesIsBeingLoading(boolean listOfSitesIsBeingLoading) {
        this.listOfSitesIsBeingLoading = listOfSitesIsBeingLoading;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_coffee_sites_list);

        contextView = findViewById(R.id.my_coffeesite_frameLayout);

        loadMyCoffeeSitesProgressBar = findViewById(R.id.progress_my_coffeesites_load);

        myCoffeeSitesToolbar = findViewById(R.id.my_sitesList_Toolbar);

        // If called from MainActivity, we can pass the number of CoffeeSites from user
        // to be shown/loaded
        // Not used yet
        //int numberOfMyCoffeeSites = getIntent().getIntExtra("myCoffeeSitesNumber", 0);

        // Load content CoffeeSites in case of returnign back to this activity?
        // Usually the content is loaded after the UserAccountService is connected
        content = getIntent().getParcelableExtra("myCoffeeSites");

        // Only CoffeeSites not in CANCELED state are to be shown
        if (content != null) {
            content = removeCanceledElements(content);
        }

        if (savedInstanceState != null) { // i.e. after orientation was changed
            Collections.sort(content);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_sitesList_Toolbar);
        setSupportActionBar(toolbar);

        layoutManager = new LinearLayoutManager(this);

        // Must be called here onCreate() as after successful connection to UserAccountService
        // loading of users CoffeeSites starts. We need this loading only if this Activity
        // is created, not in case we returned to it from another Activity
        doBindUserAccountService();

        /**
         * Registers receiver for all operations results performed by
         * CoffeeSiteService with current CoffeeSite of this Activity
         */
        registerCoffeeSiteOperationsReceiver();
    }

    @Override
    public void onBackPressed() {
        if (!listOfSitesIsBeingLoading) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_coffeesites_list, menu);

        addCoffeeSiteMenuItem = menu.findItem(R.id.action_go_to_create_coffeesite);
        reloadMyCoffeeSitesMenuItem = menu.findItem(R.id.action_refresh_list);

        // Disable menu options when the list of user's sites
        // is being loading, otherwise enable
        // this method is invoked by invalidateOptionsMenu
        // called before and after loading i.e. in showProgressBar
        // and hideProgressBar metods
        if (isListOfSitesIsBeingLoading()) {
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
            if (currentUser != null && recyclerViewAdapter != null) {
                // Delete current list
                recyclerViewAdapter.clearList();
                // Load list again
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
        if (currentUser != null) {
            startMyCoffeeSitesLoadOperation();
        }
    }


    private void prepareAndActivateRecyclerView() {
        Bundle extras = getIntent().getExtras();
        recyclerView = findViewById(R.id.my_coffeesite_list);
        assert recyclerView != null;

        recyclerView.setLayoutManager(layoutManager);
        if (extras != null || content != null) {
            //Collections.sort(content);
            content = removeCanceledElements(content);
            //myCoffeeSitesToolbar.setTitle(myCoffeeSitesToolbar.getTitle() + " (" + content.size() + ")");
            setupRecyclerView((RecyclerView) recyclerView, content);
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<CoffeeSite> listContent) {
        recyclerViewAdapter = new MyCoffeeSiteItemRecyclerViewAdapter(this, listContent);
        recyclerView.setAdapter(recyclerViewAdapter);
    }


    /** ------------ CoffeeSiteService calls and callback ------------------------------- **/

    /**
     * Starts operation to load all CoffeeSites created by current user
     *
     */
    private void startMyCoffeeSitesLoadOperation() {
        if (Utils.isOnline()) {
            Log.i(TAG, "startCoffeeSiteServiceLoadOperation");
            Intent cfServiceIntent = new Intent();
            cfServiceIntent.setClass(this, CoffeeSiteService.class);
            cfServiceIntent.putExtra("operation_type", COFFEE_SITES_FROM_CURRENT_USER_LOAD);
            //cfServiceIntent.putExtra("countDownLatch", countDownLatch);
            showProgressbarAndDisableMenuItems();

            startService(cfServiceIntent);
            Log.i(TAG, "startCoffeeSiteServiceOperation, End");
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }


    private void registerCoffeeSiteOperationsReceiver() {
        Log.i(TAG, "registerCoffeeSiteOperationsReceiver() start");
        coffeeSiteServiceReceiver = new CoffeeSiteServiceOperationsReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoffeeSiteService.COFFEE_SITE_LOADING);

        LocalBroadcastManager.getInstance(this).registerReceiver(coffeeSiteServiceReceiver, intentFilter);
        Log.i(TAG, "registerCoffeeSiteOperationsReceiver() end");
    }

    /**
     * Receiver callbacks for CoffeeSiteService operations invoked earlier
     */
    private class CoffeeSiteServiceOperationsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            hideProgressbarAndEnableMenuItems();

            Log.i(TAG, "onReceive start");
            String result = intent.getStringExtra("operationResult");
            String error = intent.getStringExtra("operationError");
            int operationType = intent.getIntExtra("operationType", 0);
            Log.i(TAG, "Result: " + result + " Error: " + error + ", PperationType: " + operationType);

            switch (operationType) {

                case COFFEE_SITES_FROM_CURRENT_USER_LOAD: {

                    if (!error.isEmpty()) {
                        showMyCoffeeSitesLoadFailure(error);
                        Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_LOAD. Error: " + error);
                    }
                    else {
                        showMyCoffeeSitesLoadSuccess();
                        Log.i(TAG, "COFFEE_SITES_FROM_CURRENT_USER_LOAD. Result: " + result);
                        content = intent.getParcelableArrayListExtra("coffeeSitesList");
                        if (content != null) {
                            // Only CoffeeSites not in CANCELED state are to be shown
                            prepareAndActivateRecyclerView();
                        }
                    }
                }
                break;

                default: break;
            }
        }
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void showProgressbarAndDisableMenuItems() {
        setListOfSitesIsBeingLoading(true);
        invalidateOptionsMenu();
        loadMyCoffeeSitesProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to be called also from RecyclerViewAdapter
     */
    public void hideProgressbarAndEnableMenuItems() {
        setListOfSitesIsBeingLoading(false);
        invalidateOptionsMenu();
        loadMyCoffeeSitesProgressBar.setVisibility(View.GONE);
    }

    private void showMyCoffeeSitesLoadSuccess()
    {}


    private void showMyCoffeeSitesLoadFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.my_coffeesites_load_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    /**
     * Pomocna metoda to remove CANCELED elements from the list
     */
    private List<CoffeeSite> removeCanceledElements(List<CoffeeSite> inputCoffeeSiteList) {
        Iterator<CoffeeSite> iter = inputCoffeeSiteList.iterator();
        while (iter.hasNext()) {
            CoffeeSite p = iter.next();
            if (p.getStatusZaznamu().toString().equalsIgnoreCase("CANCELED")) {
                iter.remove();
            }
        }
        return inputCoffeeSiteList;
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
                    Picasso.get().invalidate(Uri.parse(editedCoffeeSite.getMainImageURL()));
                    recyclerViewAdapter.updateEditedCoffeeSite(editedCoffeeSite, coffeeSitePositionInRecyclerView);
                }
            }
        }
        // Check which request we're responding to
        if (requestCode == CREATE_COFFEESITE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user created new CoffeeSite
                if (currentUser != null) {
                    startMyCoffeeSitesLoadOperation();
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.onDialogPositiveClick();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.onDialogNegativeClick();
        }
    }


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

        /**
         * Registers receiver for all operations results performed by
         * CoffeeSiteService with current CoffeeSite of this Activity
         */
        //registerCoffeeSiteOperationsReceiver();

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


    private void doUnbindUserLoginService() {
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            userAccountServiceConnector.removeUserAccountServiceConnectionListener(this);
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

    @Override
    protected void onStop() {
        
        LocalBroadcastManager.getInstance(this).unregisterReceiver(coffeeSiteServiceReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        doUnbindUserLoginService();
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.onDestroy();
        }
        super.onDestroy();
    }
}
