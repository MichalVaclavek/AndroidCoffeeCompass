package cz.fungisoft.coffeecompass2.activity.ui.notification;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.NotificationSubscriptionPreferencesHelper;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.notification.NotificationSubscription;
import cz.fungisoft.coffeecompass2.asynctask.notification.NotificationSubscriptionAsyncTask;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserAccountServiceConnectionListener;
import cz.fungisoft.coffeecompass2.utils.Utils;

import static cz.fungisoft.coffeecompass2.activity.ui.notification.SelectedTownFragment.ARG_TOWN_ITEM_ID;

/**
 * Activity to subscribe for push notifications about CoffeeSites
 * created.
 */
public class NewsSubscriptionActivity extends AppCompatActivity
                                      implements NotificationSubscriptionCallListener,
                                                 UserAccountServiceConnectionListener,
                                                 FragmentRemovableListener {

    private static final String TAG = "NewsSubscriptionAct";

    private NotificationSubscriptionViewModel notificationSubscriptionViewModel;

    @BindView(R.id.town_name_dropdown)
    AutoCompleteTextView townNameEditTextDropDown;

    @BindView(R.id.town_name_input_layout)
    TextInputLayout townNameTextInputLayout;

    @BindView(R.id.btn_subscribe_for_notifications)
    Button subscribeButton;

    @BindView(R.id.progress_subscribe_for_news)
    ProgressBar subscriptionProgressBar;

    @BindView(R.id.all_towns_checkBox)
    CheckBox allTownsCheckBox;

    @BindView(R.id.selected_towns_list_vertical_layout)
    LinearLayout selectedTownsLayout; // layout to show list of fragments of the selected towns

    private final int MAX_NUM_OF_TOWNS = 5;
    /**
     * Current logged-in user
     */
    protected LoggedInUser currentUser;

    private static UserAccountService userAccountService;
    private static UserAccountServiceConnector userAccountServiceConnector;

    private NotificationSubscriptionPreferencesHelper notificationSubscriptionPreferencesHelper;

    /**
     * List of text view Fragments to show selected towns
     */
    List<SelectedTownFragment> townsFragments = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_subscription);

        notificationSubscriptionViewModel = new ViewModelProvider(this, new NotificationSubscriptionViewModelFactory())
                .get(NotificationSubscriptionViewModel.class);

        ButterKnife.bind(this);

        subscribeButton.setEnabled(false);

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline()) {
                    subscriptionProgressBar.setVisibility(View.VISIBLE);
                    subscribeButton.setEnabled(false);
                    startSubscriptionAsyncTask();
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
            }
        });

        notificationSubscriptionViewModel.getNotificationSubscriptionFormState().observe(this, new Observer<NotificationSubscriptionFormValidationState>() {
            @Override
            public void onChanged(@Nullable NotificationSubscriptionFormValidationState notificationSubscriptionFormState) {
                if (notificationSubscriptionFormState == null) {
                    return;
                }
                subscribeButton.setEnabled(notificationSubscriptionFormState.isDataValid());

                if (notificationSubscriptionFormState.getTownNameError() != null) {
                    townNameEditTextDropDown.setError(getString(notificationSubscriptionFormState.getTownNameError()));
                    return;
                }
                if (notificationSubscriptionFormState.getNameAlreadyUsedError() != null) { // name already used
                    townNameEditTextDropDown.setError(getString(notificationSubscriptionFormState.getNameAlreadyUsedError()));
                    return;
                }

                if (notificationSubscriptionFormState.isDataValid()
                    && notificationSubscriptionViewModel.getValidatedTownName() != null) { // town name is valid, add to the list
                    String townName = notificationSubscriptionViewModel.getValidatedTownName();
                    // add the validated town name to the list of towns, if not already present
                    if (townName.length() > 1) {
                        SelectedTownFragment townFragment = createGetTownFragment(townName);
                        townsFragments.add(townFragment);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.selected_towns_list_vertical_layout, townFragment)
                                .commit();
                        // disable input textView if the limit for entered towns was reached
                        if (notificationSubscriptionViewModel.getAllValidatedTownNames().size() >= MAX_NUM_OF_TOWNS) {
                            townNameTextInputLayout.setEnabled(false);
                        }
                    }
                }
            }
        });


        TownNamesArrayAdapter townNamesArrayAdapter = new TownNamesArrayAdapter(this, R.layout.dropdown_menu_popoup_item);
        townNamesArrayAdapter.setNotifyOnChange(true);

        townNameEditTextDropDown.setThreshold(2); // at least to characters must be entered before other actions

        /**
         * Handle item/town selection from the drop down list
         */
        townNameEditTextDropDown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String townName =  parent.getItemAtPosition(position).toString();
                if (townName.length() > 1) {
                    notificationSubscriptionViewModel.townDataChanged(getApplicationContext(), townName, false);
                    townNameEditTextDropDown.clearListSelection();
                    townNameEditTextDropDown.setText("");
                    hideKeyboardForTownNameInput();
                }
            }
        });

        townNameEditTextDropDown.setAdapter(townNamesArrayAdapter);

        allTownsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (allTownsCheckBox.isChecked()) {
                    selectedTownsLayout.setVisibility(View.GONE);
                    townNameTextInputLayout.setEnabled(false);
                    notificationSubscriptionViewModel.townDataChanged(getApplicationContext(), "", true);
                } else {
                    selectedTownsLayout.setVisibility(View.VISIBLE);
                    townNameTextInputLayout.setEnabled(notificationSubscriptionViewModel.getAllValidatedTownNames().size() < MAX_NUM_OF_TOWNS);
                    notificationSubscriptionViewModel.townDataChanged(getApplicationContext(), "", false);
                }
            }
        });

        subscribeButton.requestFocus();
        hideKeyboardForTownNameInput();
        townNameEditTextDropDown.setEnabled(true);

        doBindUserAccountService();
        notificationSubscriptionPreferencesHelper = new NotificationSubscriptionPreferencesHelper(this);
    }

    private void startSubscriptionAsyncTask() {
        //TODO - call AsyncTask invoking REST API call to server to subscribe for notifications
        NotificationSubscription notificationSubscription = new NotificationSubscription();
        notificationSubscription.setToken(notificationSubscriptionPreferencesHelper.getFirebaseToken());
        notificationSubscription.setTopic(notificationSubscriptionPreferencesHelper.getTopic());
        if (notificationSubscriptionViewModel.isAllTownsSelected()) {
            notificationSubscription.setTownNames(Arrays.asList(getString(R.string.all_towns_subtopic)));
        } else {
            notificationSubscription.setTownNames(notificationSubscriptionViewModel.getAllValidatedTownNames());
        }
        new NotificationSubscriptionAsyncTask(notificationSubscription, getCurrentUser(),this).execute();
    }

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserAccountService;

    private void doBindUserAccountService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other applications).
        userAccountServiceConnector = new UserAccountServiceConnector();
        userAccountServiceConnector.addUserAccountServiceConnectionListener(this);

        if (bindService(new Intent(this, UserAccountService.class),
                userAccountServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserAccountService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserAccountService() {
        if (mShouldUnbindUserAccountService) {
            // Release information about the service's state.
            unbindService(userAccountServiceConnector);
            mShouldUnbindUserAccountService = false;
        }
    }

    @Override
    public void onUserAccountServiceConnected() {
        userAccountService = userAccountServiceConnector.getUserLoginService();
    }

    /**
     * Helper method to get current logged-in user from userAccountService
     * @return
     */
    protected LoggedInUser getCurrentUser() {
        return (userAccountService != null) ? userAccountService.getLoggedInUser() : null;
    }

    /**
     * Creates and returns Fragment to show selected town
     * @param townName
     * @return
     */
    private SelectedTownFragment createGetTownFragment(String townName) {
        Bundle detailsArgs = new Bundle();
        SelectedTownFragment townFragment = new SelectedTownFragment();
        townFragment.addDeleteFragmentListener(this);
        detailsArgs.putString(ARG_TOWN_ITEM_ID, townName);
        townFragment.setArguments(detailsArgs);
        return townFragment;
    }

    @Override
    public void onDestroy() {
        for (SelectedTownFragment fragment : townsFragments) {
            fragment.removeDeleteFragmentListener(this);
        }
        doUnbindUserAccountService();
        super.onDestroy();
    }


    private void showSubscriptionFailed(String errorString) {
        subscriptionProgressBar.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }


    private void goToMainActivity() {
        // go to MainActivity
        Intent i = new Intent(NewsSubscriptionActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }


    @Override
    public void onNotificationSubscriptionSuccess(NotificationSubscriptionRequestResult subscriptionRequestResult) {
        subscriptionProgressBar.setVisibility(View.GONE);
        if (subscriptionRequestResult.getSuccess()) {
            Toast.makeText(getApplicationContext(), getString(R.string.subscription_call_succeeded), Toast.LENGTH_SHORT).show();
            if (getCurrentUser() != null) {
                notificationSubscriptionPreferencesHelper.putUserId(getCurrentUser().getUserId());
            }
            notificationSubscriptionPreferencesHelper.putTowns(notificationSubscriptionViewModel.getAllValidatedTownNames());
        }
        setResult(Activity.RESULT_OK);

        goToMainActivity();
    }

    @Override
    public void onNotificationSubscriptionFailure(NotificationSubscriptionRequestResult subscriptionRequestResult) {
        if (subscriptionRequestResult.getError() != null) {
            showSubscriptionFailed(subscriptionRequestResult.getError().getDetail());
            if (subscriptionRequestResult.getError().getRestError() != null) {
                RestError restError = subscriptionRequestResult.getError().getRestError();
                String errorParameter = restError.getErrorParameter();
                if (errorParameter != null) {
                    townNameEditTextDropDown.setError(restError.getErrorParameterValue());
                }
            }
        }
    }

    @Override
    public void onFragmentClosed(SelectedTownFragment fragment) {
        notificationSubscriptionViewModel.townDataRemoved(fragment.getTownName());
        townsFragments.remove(fragment);
        if (notificationSubscriptionViewModel.getAllValidatedTownNames().size() < MAX_NUM_OF_TOWNS) {
            //townNameEditTextDropDown.setEnabled(true);
            townNameTextInputLayout.setEnabled(true);
        }
        //subscribeButton.setEnabled(notificationSubscriptionViewModel.getAllValidatedTownNames().size() > 0);
        getSupportFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
    }


    private void hideKeyboardForTownNameInput() {
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(townNameEditTextDropDown.getWindowToken(), 0);
    }
}