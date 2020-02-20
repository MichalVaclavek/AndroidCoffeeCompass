package cz.fungisoft.coffeecompass2.activity.ui.coffeesite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.BindView;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.ActivityWithLocationService;
import cz.fungisoft.coffeecompass2.activity.MapsActivity;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesRepository;
import cz.fungisoft.coffeecompass2.services.CoffeeSiteService;
import cz.fungisoft.coffeecompass2.ui.fragments.CoffeeSiteDetailFragment;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_ACTIVATE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_DELETE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_LOAD;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_SAVE;
import static cz.fungisoft.coffeecompass2.services.CoffeeSiteService.COFFEE_SITE_UPDATE;

/**
 * An activity representing a single CoffeeSite detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link FoundCoffeeSitesListActivity}.
 * We need CoffeeSiteService to load current instance of CoffeeSite ...
 */
public class CoffeeSiteDetailActivity extends ActivityWithLocationService {

    private static final String TAG = "CoffeeSiteDetailAct";

    private Button commentsButton;

    private CoffeeSiteDetailFragment detailFragment;

    private CoffeeSite coffeeSite;

    private CoffeeSiteServiceLoadReceiver coffeeSiteServiceReceiver;

    private ProgressBar loadCoffeeSiteProgressBar;

    /**
     * To show snackbar
     */
    private View contextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_detail);

        contextView = findViewById(R.id.coffeesite_detaill_main_layout);

        commentsButton = (Button) findViewById(R.id.commentsButton);

        loadCoffeeSiteProgressBar = findViewById(R.id.load_coffeeSite_progressBar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
//        }

        // Read coffee site data from calling activity
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            if (coffeeSite instanceof CoffeeSiteMovable) {
                coffeeSite = (CoffeeSiteMovable) bundle.getParcelable("coffeeSite");
            } else {
                // Activity is opened from another Activity which uses only CoffeeSite instances
                coffeeSite = (CoffeeSite) bundle.getParcelable("coffeeSite");
            }
        }

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.detail_toolbar_layout);
        if (appBarLayout != null && coffeeSite != null) {
            appBarLayout.setTitle(coffeeSite.getName());
        }

        if (coffeeSite != null) {
            boolean imageAvail = !coffeeSite.getMainImageURL().isEmpty();

            if (imageAvail) {
                Button imageButton = (Button) findViewById(R.id.imageButton);
                imageButton.setVisibility(View.VISIBLE);
                imageButton.setEnabled(true);
            }
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
    }

    /**
     * locationService is not null here
     */
    @Override
    public void onLocationServiceConnected() {

        super.onLocationServiceConnected();
        if (coffeeSite != null ) {
            if (coffeeSite instanceof CoffeeSiteMovable) {
                ((CoffeeSiteMovable)coffeeSite).setLocationService(locationService);
                locationService.addPropertyChangeListener(((CoffeeSiteMovable)coffeeSite));
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.coffeesite_detail_container, detailFragment)
                    .commit();
        }
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(coffeeSiteServiceReceiver);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Always try to load actual instance of CoffeeSite and convert it to CoffeeSiteMovable in
        // CoffeeSiteServiceLoadReceiver.onReceive()
        registerCoffeeSiteLoadReceiver();
        startCoffeeSiteLoad();
    }

    /**
     * Register this Activity to be inform about results of CoffeeSitesService results
     * for Actions of the CoffeeSiteService.COFFEE_SITE_OPERATION (Save, Update, Delete)
     * and CoffeeSiteService.COFFEE_SITE_STATUS  (Activate, Deactivate, Cancel)
     * types
     */
    private void registerCoffeeSiteLoadReceiver() {
        Log.i("CreateCoffeeSiteAct", "registerCoffeeSiteOperationsReceiver(), start");
        coffeeSiteServiceReceiver = new CoffeeSiteServiceLoadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoffeeSiteService.COFFEE_SITE_LOADING);
        LocalBroadcastManager.getInstance(this).registerReceiver(coffeeSiteServiceReceiver, intentFilter);
        Log.i("CreateCoffeeSiteAct", "registerCoffeeSiteOperationsReceiver(), end");
    }

    public void startCoffeeSiteLoad() {

        if (Utils.isOnline()) {
            Intent cfServiceIntent = new Intent();
            cfServiceIntent.setClass(this, CoffeeSiteService.class);
            cfServiceIntent.putExtra("operation_type", CoffeeSiteService.COFFEE_SITE_LOAD);
            cfServiceIntent.putExtra("coffeeSiteId", coffeeSite.getId());
            startService(cfServiceIntent);
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
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
        loadCoffeeSiteProgressBar.setVisibility(View.GONE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
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
        if (locationService != null) {
            Intent mapIntent = new Intent(this, MapsActivity.class);
            mapIntent.putExtra("currentLocation", locationService.getCurrentLatLng());
            mapIntent.putExtra("site", (Parcelable) coffeeSite);
            startActivity(mapIntent);
        }
    }

    /**
     * Method to be called from async task after call to obtain number of comments for
     * the CoffeeSite within this Activity
     * @param numberOfComments
     */
    // Not needed in current implementation as the Comments button is still visible/enabled
    public void processNumberOfComments(int numberOfComments) {
//        this.comments = comments;
//         Comments button is still available, even there is no comment or user is not logged in
//        if (numberOfComments > 0) {
//            enableCommentsButton();
//        }
    }

    public void showRESTCallError(Result.Error error) {
        if (error.getRestError() != null) {
            Toast.makeText(getApplicationContext(),
                    error.getRestError().getDetail(),
                    Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Server connection error.",
                    Toast.LENGTH_SHORT);
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


    /**
     * Receiver callbacks for CoffeeSiteService operations invoked earlier
     */
    private class CoffeeSiteServiceLoadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            hideProgressbar();

            Log.i(TAG, "onReceive start");
            String result = intent.getStringExtra("operationResult");
            String error = intent.getStringExtra("operationError");
            int operationType = intent.getIntExtra("operationType", 0);

            CoffeeSite loadedCoffeeSite = (CoffeeSite) intent.getExtras().getParcelable("coffeeSite");

            Log.i(TAG,"Operation type: " + operationType + ". Result:" + result + ". Error: " + error);

            switch (operationType) {
                case COFFEE_SITE_LOAD: {
                    Log.i(TAG, "Save result: " + result);
                    if (error.isEmpty()) {
                        // Loaded actual instance of CoffeeSite - transform it to CoffeeSiteMovable
                        if (loadedCoffeeSite != null) {
                            // Keep already known distance if available
                            if (coffeeSite != null) {
                                loadedCoffeeSite.setDistance(coffeeSite.getDistance());
                            }
                            coffeeSite = new CoffeeSiteMovable(loadedCoffeeSite);
                            if (locationService != null) {
                                ((CoffeeSiteMovable) coffeeSite).setLocationService(locationService);
                                locationService.addPropertyChangeListener(((CoffeeSiteMovable) coffeeSite));
                            }
                        }
                        // Refresh detailFragment to update View with the new CoffeeSiteMovable
                        detailFragment.setCoffeeSite(coffeeSite);
                        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.detach(detailFragment);
                        ft.attach(detailFragment);
                        ft.commit();
                    } else {
                        // Not needed to change current coffeeSite, only show some message?
                        showCoffeeSiteLoadFailure(error);
                    }
                } break;

                default: break;
            }
        }
    }

    private void showCoffeeSiteLoadFailure(String error) {
        error = !error.isEmpty() ? error : getString(R.string.coffee_site_load_failure);
        Snackbar mySnackbar = Snackbar.make(contextView, error, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

}
