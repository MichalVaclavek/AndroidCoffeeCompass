package cz.fungisoft.coffeecompass2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.data.Result;
import cz.fungisoft.coffeecompass2.asynctask.comment.GetNumberOfCommentsAsyncTask;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceConnectionListener;
import cz.fungisoft.coffeecompass2.ui.fragments.CoffeeSiteDetailFragment;

/**
 * An activity representing a single CoffeeSite detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CoffeeSiteListActivity}.
 * We need userAccountService to check if a user is loged-in to show him/her
 * the Cooments button
 */
public class CoffeeSiteDetailActivity extends ActivityWithLocationService implements UserLoginServiceConnectionListener {

    private static final String TAG = "CoffeeSiteDetailAct";

    private Button commentsButton;

    private CoffeeSiteDetailFragment detailFragment;

    private CoffeeSiteMovable coffeeSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffeesite_detail);

        commentsButton = (Button) findViewById(R.id.commentsButton);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
//        }

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            coffeeSite = (CoffeeSiteMovable) bundle.getParcelable("coffeeSite");
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

            // Async task to check if the Comments are available for the site
            if (Utils.isOnline()) {
                //new GetCommentsAsyncTask(this, coffeeSite).execute();
                new GetNumberOfCommentsAsyncTask(coffeeSite.getId(), this).execute();
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

        doBindUserLoginService();
    }

    /**
     * locationService is not null here
     */
    @Override
    public void onLocationServiceConnected() {
        super.onLocationServiceConnected();
        if (coffeeSite != null) {
            coffeeSite.setLocationService(locationService);
            locationService.addPropertyChangeListener(coffeeSite);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.coffeesite_detail_container, detailFragment)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        doUnbindUserLoginService();
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
            // navigateUpTo(new Intent(this, CoffeeSiteListActivity.class));

            /*
            * The standard way, navigateUpTo(new Intent(this, CoffeeSiteListActivity.class));
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
    public void processNumberOfComments(int numberOfComments) {
        //this.comments = comments;
        if (numberOfComments > 0) {
            enableCommentsButton();
        }
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

    // ** UserLogin Service connection/disconnection ** //

    protected UserAccountService userLoginService;
    private UserAccountServiceConnector userLoginServiceConnector;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    @Override
    public void onUserLoginServiceConnected() {
        userLoginService = userLoginServiceConnector.getUserLoginService();
        if (userLoginService != null && userLoginService.isUserLoggedIn()) {
            enableCommentsButton();
        }
    }

    private void doBindUserLoginService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userLoginServiceConnector = new UserAccountServiceConnector(this);
        if (bindService(new Intent(this, UserAccountService.class),
                userLoginServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserLoginService() {
//        if (userAccountService != null) {
//            userAccountService.removeUserLoginServiceListener(this);
//        }
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            unbindService(userLoginServiceConnector);
            mShouldUnbindUserLoginService = false;
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

}
