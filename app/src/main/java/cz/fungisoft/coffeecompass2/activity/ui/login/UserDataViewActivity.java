package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.UserLogoutAndDeleteServiceListener;

public class UserDataViewActivity extends AppCompatActivity implements UserLogoutAndDeleteServiceListener {

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

        //Toolbar toolbar = (Toolbar) findViewById(R.id.userProfileToolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        userProfileToShow = (LoggedInUser) this.getIntent().getExtras().get("currentUserProfile");

        if (userProfileToShow != null) {

            if (appBarLayout != null) {
                //appBarLayout.setTitle(userProfileToShow.getUserName());
                appBarLayout.setTitle(getString(R.string.user_profile_label));
            }

            usernameTextView.setText(userProfileToShow.getUserName());
            userEmailTextView.setText(userProfileToShow.getEmail());
            numOfCreatedSitesTextView.setText(String.valueOf(userProfileToShow.getNumOfCreatedSites()));

            if (userProfileToShow.getFirstName() != null && !userProfileToShow.getFirstName().isEmpty()) {
                firstNameTextView.setText(userProfileToShow.getFirstName());
            } else {
                firstNameRow.setEnabled(false);
            }
            if (userProfileToShow.getLastName() != null && !userProfileToShow.getLastName().isEmpty()) {
                lastNameTextView.setText(userProfileToShow.getLastName());
            } else {
                lastNameRow.setEnabled(false);
            }

            userCratedOnTextView.setText(userProfileToShow.getCreatedOnFormated());

            doBindUserLogoutAndDeleteService();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //this.onBackPressed();
            goToMainActivityAndFinish();
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
        if (userAccountService != null) {
            userAccountService.removeLogoutAndDeleteServiceListener(this);
        }
        if (mShouldUnbindUserDeleteAndRegisterService) {
            // Release information about the service's state.
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserDeleteAndRegisterService = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindUserLogoutAndDeleteService();
    }

    private void goToMainActivity() {
        // go to MainActivity
        Intent i = new Intent(UserDataViewActivity.this, MainActivity.class);
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
        //allowButtonAndGoneProgressBar();
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
        //setResult(Activity.RESULT_OK);
        //goToMainActivityAndFinish();
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
        //setResult(Activity.RESULT_OK);
        //goToMainActivityAndFinish();
    }

    @Override
    public void onLogoutAndDeleteServiceConnected() {
        userAccountService = userAccountServiceConnector.getUserLoginService();
        userAccountService.addLogoutAndDeleteServiceListener(this);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDeleteProgressBar.setVisibility(View.VISIBLE);
                logoutButton.setEnabled(false);
                deleteUserButton.setEnabled(false);
                userAccountService.logout();
            }
        });
        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDeleteProgressBar.setVisibility(View.VISIBLE);
                logoutButton.setEnabled(false);
                deleteUserButton.setEnabled(false);
                userAccountService.delete();
            }
        });
    }

}
