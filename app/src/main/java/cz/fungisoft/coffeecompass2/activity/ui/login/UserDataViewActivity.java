package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLogoutAndDeleteServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLogoutAndDeleteServiceListener;

/**
 * Activity to show logged-in user profile details.
 * Allows to log-out or delete user's account.
 */
public class UserDataViewActivity extends AppCompatActivity implements UserLogoutAndDeleteServiceListener, UserLogoutAndDeleteServiceConnectionListener,
                                                                       DeleteUserAccountDialogFragment.DeleteUserAccountDialogListener {

    private static final String TAG = "UserDataViewActivity";

    private LoggedInUser userProfileToShow;

    @BindView(R.id.userName) TextView usernameTextView;
    @BindView(R.id.userProfileEmail) TextView userEmailTextView;
    @BindView(R.id.userProfileNumOfCreatedSites) TextView numOfCreatedSitesTextView;

    @BindView(R.id.firstNameLayout) LinearLayout firstNameRow;
    @BindView(R.id.firstName) TextView firstNameTextView;

    @BindView(R.id.lastNameLayout) LinearLayout lastNameRow;
    @BindView(R.id.lastName) TextView lastNameTextView;

    @BindView(R.id.userCreatedOn) TextView userCratedOnTextView;

    @BindView(R.id.progress) ProgressBar logoutDeleteProgressBar;

    @BindView(R.id.btn_logout) Button logoutButton;
    @BindView(R.id.btn_deleteUser) Button deleteUserButton;

    @BindView(R.id.user_profile_toolbar) Toolbar toolbar;
    @BindView(R.id.user_profile_toolbarLayout) CollapsingToolbarLayout appBarLayout;

    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userAccountServiceConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_view);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        logoutButton.setEnabled(true);
        deleteUserButton.setEnabled(true);

        userProfileToShow = (LoggedInUser) this.getIntent().getExtras().get("currentUserProfile");

        if (userProfileToShow != null) {

            if (appBarLayout != null) {
                appBarLayout.setTitle(getString(R.string.user_profile_label));
            }

            usernameTextView.setText(userProfileToShow.getUserName());
            userEmailTextView.setText(userProfileToShow.getEmail());
            numOfCreatedSitesTextView.setText(String.valueOf(userProfileToShow.getNumOfCreatedSites()));

            if (userProfileToShow.getFirstName() != null && !userProfileToShow.getFirstName().isEmpty()) {
                firstNameRow.setVisibility(View.VISIBLE);
                firstNameTextView.setText(userProfileToShow.getFirstName());
            } else {
                firstNameRow.setVisibility(View.GONE);
            }
            if (userProfileToShow.getLastName() != null && !userProfileToShow.getLastName().isEmpty()) {
                lastNameRow.setVisibility(View.VISIBLE);
                lastNameTextView.setText(userProfileToShow.getLastName());
            } else {
                lastNameRow.setVisibility(View.GONE);
            }

            userCratedOnTextView.setText(userProfileToShow.getCreatedOnFormated());

            doBindUserLogoutAndDeleteService();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            //goToMainActivityAndFinish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* **** Connecting UserLogoutAndDeleteService ****** */

    private boolean mShouldUnbindUserDeleteAndRegisterService;

    private void doBindUserLogoutAndDeleteService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userAccountServiceConnector = new UserAccountServiceConnector(this);

        if (bindService(new Intent(this, UserAccountService.class),
                userAccountServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserDeleteAndRegisterService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserLogoutAndDeleteService() {
        if (mShouldUnbindUserDeleteAndRegisterService) {
            // Release information about the service's state.
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserDeleteAndRegisterService = false;
        }
    }

    @Override
    protected void onDestroy() {
        if (userAccountService != null) {
            userAccountService.removeLogoutAndDeleteServiceListener(this);
        }
        doUnbindUserLogoutAndDeleteService();
        super.onDestroy();
    }

    private void goToMainActivity() {
        Intent i = new Intent(UserDataViewActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    private void goToMainActivityAndFinish() {
        // go to MainActivity
        goToMainActivity();
        finish();
    }

    private void allowButtonAndGoneProgressBar() {
        logoutDeleteProgressBar.setVisibility(View.GONE);
        logoutButton.setEnabled(true);
        deleteUserButton.setEnabled(true);
    }

    @Override
    public void onUserLogoutSuccess() {
        logoutDeleteProgressBar.setVisibility(View.GONE);
        Toast.makeText(getBaseContext(), getString(R.string.logout_success), Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_OK);

        goToMainActivityAndFinish();
    }

    @Override
    public void onUserLogoutFailure(String errorMessage) {
        //allowButtonAndGoneProgressBar();
        logoutDeleteProgressBar.setVisibility(View.GONE);
        Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUserDeleteSuccess() {
        //allowButtonAndGoneProgressBar();
        logoutDeleteProgressBar.setVisibility(View.GONE);
        Toast.makeText(getBaseContext(), getString(R.string.delete_user_success), Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_OK);

        goToMainActivityAndFinish();
    }

    @Override
    public void onUserDeleteFailure(String errorMessage) {
        //allowButtonAndGoneProgressBar();
        logoutDeleteProgressBar.setVisibility(View.GONE);
        Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLogoutAndDeleteServiceConnected() {
        userAccountService = userAccountServiceConnector.getUserLoginService();
        userAccountService.addLogoutAndDeleteServiceListener(this);
        Log.i(TAG, "This is UserAccountServie instance: " + userAccountService.toString());
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline()) {
                    logoutDeleteProgressBar.setVisibility(View.VISIBLE);
                    logoutButton.setEnabled(false);
                    deleteUserButton.setEnabled(false);
                    userAccountService.logout();
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
            }
        });
        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDeleteAccountDialog();
            }
        });
    }

    /**
     * Handling of the result of the Confirmation dialog for User account delete action
     */

    /**
     * Show the dialog
     */
    private void showConfirmDeleteAccountDialog() {
        // Create an instance of the dialog fragment and show it
        DeleteUserAccountDialogFragment dialog = new DeleteUserAccountDialogFragment();
        dialog.show(getSupportFragmentManager(), "DeleteUserAccountDialogFragment");
    }

    /**
     * Process positive response, i.e. try to delete account
     * @param dialog
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (Utils.isOnline()) {
            logoutDeleteProgressBar.setVisibility(View.VISIBLE);
            logoutButton.setEnabled(false);
            deleteUserButton.setEnabled(false);
            userAccountService.delete();
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    /**
     * Process negative response. nothing to do here
     * @param dialog
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }


    /**
     * Needed to adding this Activity to list of Listeners of the UserAccountService
     * only once per this Activity instance.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDataViewActivity that = (UserDataViewActivity) o;
        return Objects.equals(usernameTextView, that.usernameTextView) &&
                Objects.equals(userEmailTextView, that.userEmailTextView) &&
                Objects.equals(numOfCreatedSitesTextView, that.numOfCreatedSitesTextView) &&
                Objects.equals(firstNameTextView, that.firstNameTextView) &&
                Objects.equals(lastNameTextView, that.lastNameTextView) &&
                Objects.equals(userCratedOnTextView, that.userCratedOnTextView);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usernameTextView, userEmailTextView, numOfCreatedSitesTextView, firstNameTextView, lastNameTextView, userCratedOnTextView);
    }

}
