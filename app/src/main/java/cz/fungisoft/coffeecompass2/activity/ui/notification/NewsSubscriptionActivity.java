package cz.fungisoft.coffeecompass2.activity.ui.notification;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.NotificationSubscriptionPreferencesHelper;
import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.RestError;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.notification.NotificationSubscription;
import cz.fungisoft.coffeecompass2.asynctask.notification.CancelNotificationSubscriptionAsyncTask;
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
                                                 FragmentRemovableListener,
                                                 CancelSubscriptionDialogFragment.CancelSubscriptionDialogListener {

    private static final String TAG = "NewsSubscriptionAct";

    private NotificationSubscriptionViewModel notificationSubscriptionViewModel;

    @BindView(R.id.town_name_dropdown)
    AutoCompleteTextView townNameEditTextDropDown;

    @BindView(R.id.town_name_input_layout)
    TextInputLayout townNameTextInputLayout;

    @BindView(R.id.btn_subscribe_for_notifications)
    Button subscribeButton;

    @BindView(R.id.subscribed_town_default_TextView)
    TextView defaultNoSubscriptionTextView;

    @BindView(R.id.edit_towns_imageButton)
    MaterialButton editButton;

    @BindView(R.id.cancel_all_subscriptions_imageButton)
    MaterialButton unSubscribeAllButton;

    @BindView(R.id.close_edit_towns_view_icon)
    ImageView closeEditTownsIcon;

    @BindView(R.id.edit_towns_CardView)
    CardView editTownsCardView;

    @BindView(R.id.list_of_subscribed_towns_layout)
    LinearLayout alreadySelectedSubscriptionTownsLayout;

    @BindView(R.id.progress_subscribe_for_news)
    ProgressBar subscriptionProgressBar;

    @BindView(R.id.all_towns_checkBox)
    CheckBox allTownsCheckBox;

    // CardView with layout to show list of fragments of the selected towns
    // Used to add onClick Listener to bahave same way as Edit button
    @BindView(R.id.list_of_subscribed_towns_cardView)
    CardView selectedTownsCardView;


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
    private List<SelectedTownFragment> townsFragments = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_subscription);

        notificationSubscriptionViewModel = new ViewModelProvider(this, new NotificationSubscriptionViewModelFactory())
                .get(NotificationSubscriptionViewModel.class);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.subscribe_notification_toolbar);
        setSupportActionBar(toolbar);
        // Setup main toolbar with back button arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.notif_subscription_appbar_title));

        subscribeButton.setEnabled(false);

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline(getApplicationContext())) {
                    subscribeButton.setEnabled(false);
                    unSubscribeAllButton.setEnabled(false);
                    startSubscriptionAsyncTask();
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
            }
        });

        unSubscribeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmCancelSubscriptionsDialog();
            }
        });

        editTownsCardView.setVisibility(View.GONE);

        notificationSubscriptionPreferencesHelper = new NotificationSubscriptionPreferencesHelper(this);

        selectedTownsCardView.setOnClickListener(createEditOnClickListener());

        editButton.setOnClickListener(createEditOnClickListener());

        unSubscribeAllButton.setVisibility(View.GONE);

        // Something is subscribed, allow to unsubscribe and edit
        if (notificationSubscriptionPreferencesHelper.getAllTownsTopicSelected()
            || !notificationSubscriptionPreferencesHelper.getTowns().isEmpty()) {
            unSubscribeAllButton.setVisibility(View.VISIBLE);
            editButton.setEnabled(true);
            selectedTownsCardView.setEnabled(true);
            clearCurrentSubscriptionsCardView();
            fillInCurrentSubscriptionsCardView(notificationSubscriptionPreferencesHelper);
        }

        closeEditTownsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide card view to edit towns
                editTownsCardView.setVisibility(View.GONE);
                // enable edit button
                editButton.setEnabled(true);
                selectedTownsCardView.setEnabled(true);
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

                if (notificationSubscriptionFormState.isDataValid()) {
                    if (!notificationSubscriptionViewModel.getValidatedTownName().isEmpty()) { // only one new townName was added
                        showTownName(notificationSubscriptionViewModel.getValidatedTownName());
                        return;
                    }
                    if (!notificationSubscriptionViewModel.getAllValidatedTownNames().isEmpty()) { // more towns were added at once
                        removeAllTownsFragments();
                        for (String townName : notificationSubscriptionViewModel.getAllValidatedTownNames()) {
                            showTownName(townName);
                        }
                        return;
                    }
                    allTownsCheckBox.setChecked(notificationSubscriptionViewModel.isAllTownsSelected());
                }
                // if all Towns selected, dont allow editing of town names
                townNameEditTextDropDown.setEnabled(!notificationSubscriptionViewModel.isAllTownsSelected());
                if (notificationSubscriptionViewModel.getAllValidatedTownNames().isEmpty()) {
                    removeAllTownsFragments();
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
                    notificationSubscriptionViewModel.townDataChanged(getApplicationContext(), townName, false, null);
                    townNameEditTextDropDown.clearListSelection();
                    townNameEditTextDropDown.setText("");
                    //hideKeyboardForTownNameInput();
                    subscribeButton.requestFocus();
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
                    townNameEditTextDropDown.setText("");
                    notificationSubscriptionViewModel.townDataChanged(getApplicationContext(), "", true, null);
                } else {
                    selectedTownsLayout.setVisibility(View.VISIBLE);
                    townNameTextInputLayout.setEnabled(notificationSubscriptionViewModel.getAllValidatedTownNames().size() < MAX_NUM_OF_TOWNS);
                    notificationSubscriptionViewModel.townDataChanged(getApplicationContext(), "", false, null);
                }
            }
        });

        doBindUserAccountService();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            onBackPressed();
        }
        return true;
    }

    private void showTownName(String townName) {
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

    /**
     * Creates NotificationSubscription API request data and calls AsyncTask to perform notification
     * subscription with these data.
     */
    private void startSubscriptionAsyncTask() {
        NotificationSubscription notificationSubscription = new NotificationSubscription();
        // Firebase device token and main Topic is the part of PreferenceHelper data, not the model
        notificationSubscription.setToken(notificationSubscriptionPreferencesHelper.getFirebaseToken());
        notificationSubscription.setTopic(notificationSubscriptionPreferencesHelper.getTopic());
        if (notificationSubscriptionViewModel.isAllTownsSelected()) {
            notificationSubscription.setTownNames(Arrays.asList(getString(R.string.all_towns_subtopic)));
        } else {
            notificationSubscription.setTownNames(notificationSubscriptionViewModel.getAllValidatedTownNames());
        }
        subscriptionProgressBar.setVisibility(View.VISIBLE);
        new NotificationSubscriptionAsyncTask(notificationSubscription, getCurrentUser(),this).execute();
    }

    /**
     * Creates NotificationSubscription API request data and calls AsyncTask to perform Cancel notification
     * subscription with these data.
     */
    private void startCancelSubscriptionAsyncTask() {
        NotificationSubscription notificationSubscription = new NotificationSubscription();
        notificationSubscription.setToken(notificationSubscriptionPreferencesHelper.getFirebaseToken());
        notificationSubscription.setTopic("");
        notificationSubscription.setTownNames(null);
        subscriptionProgressBar.setVisibility(View.VISIBLE);
        new CancelNotificationSubscriptionAsyncTask(notificationSubscription, getCurrentUser(), this).execute();
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
     * Creates and returns Fragment to show selected town with cancel icon
     *
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

    /**
     * Callback method called after successful API call to new Notification subscription.
     * Saves subscription data into PreferenceHelper and updates some UI elements accordingly.
     *
     * @param subscriptionRequestResult
     */
    @Override
    public void onNotificationSubscriptionSuccess(NotificationSubscriptionRequestResult subscriptionRequestResult) {
        subscriptionProgressBar.setVisibility(View.GONE);
        if (subscriptionRequestResult.getSuccess()) {
            Toast.makeText(getApplicationContext(), getString(R.string.subscription_call_succeeded), Toast.LENGTH_SHORT).show();
            if (getCurrentUser() != null) {
                notificationSubscriptionPreferencesHelper.putUserId(getCurrentUser().getUserId());
            }
            notificationSubscriptionPreferencesHelper.putTowns(notificationSubscriptionViewModel.getAllValidatedTownNames());
            if (notificationSubscriptionViewModel.getAllValidatedTownNames().isEmpty()) {
                removeAllTownsFragments();
            }
            notificationSubscriptionPreferencesHelper.putAllTownsTopicSelected(notificationSubscriptionViewModel.isAllTownsSelected());

            clearCurrentSubscriptionsCardView();
            fillInCurrentSubscriptionsCardView(notificationSubscriptionPreferencesHelper);

            subscribeButton.setEnabled(false);
            unSubscribeAllButton.setVisibility(View.VISIBLE);
            unSubscribeAllButton.setEnabled(true);

            // hide card view to edit towns
            editTownsCardView.setVisibility(View.GONE);
            // enable edit button
            editButton.setEnabled(true);
            selectedTownsCardView.setEnabled(true);
        }

        setResult(Activity.RESULT_OK);
    }

    @Override
    public void onNotificationSubscriptionFailure(NotificationSubscriptionRequestResult subscriptionRequestResult) {
        subscriptionProgressBar.setVisibility(View.GONE);
        if (subscriptionRequestResult.getError() != null) {
            showSubscriptionFailed(subscriptionRequestResult.getError().getDetail());
            if (subscriptionRequestResult.getError().getRestError() != null) {
                RestError restError = subscriptionRequestResult.getError().getRestError();
                String errorParameter = restError.getErrorParameter();
                if (errorParameter != null) {
                    townNameEditTextDropDown.setError(restError.getErrorParameterValue());
                }
            }
            Toast.makeText(getApplicationContext(), getString(R.string.subscription_call_failed), Toast.LENGTH_SHORT).show();
            subscribeButton.setEnabled(true);
        }
    }

    /**
     * Callback method called after successful API call to Cancel all Notification subscription.
     * Clears  data in PreferenceHelper and updates some UI elements accordingly.
     *
     * @param subscriptionRequestResult
     */
    @Override
    public void onCancelNotificationSubscriptionSuccess(NotificationSubscriptionRequestResult subscriptionRequestResult) {
        subscriptionProgressBar.setVisibility(View.GONE);
        if (subscriptionRequestResult.getSuccess()) {
            Toast.makeText(getApplicationContext(), getString(R.string.cancel_subscription_call_succeeded), Toast.LENGTH_SHORT).show();
            if (getCurrentUser() != null) {
                notificationSubscriptionPreferencesHelper.putUserId(getCurrentUser().getUserId());
            }
            notificationSubscriptionPreferencesHelper.putTowns(Collections.emptyList());
            notificationSubscriptionPreferencesHelper.putAllTownsTopicSelected(false);

            clearCurrentSubscriptionsCardView();
            alreadySelectedSubscriptionTownsLayout.addView(defaultNoSubscriptionTextView);

            unSubscribeAllButton.setVisibility(View.GONE);
            unSubscribeAllButton.setEnabled(false);
            removeAllTownsFragments();
            allTownsCheckBox.setChecked(false);
            // hide card view to edit towns
            editTownsCardView.setVisibility(View.GONE);
            // enable edit button
            editButton.setEnabled(true);
            selectedTownsCardView.setEnabled(true);

            notificationSubscriptionViewModel.townDataChanged(this, "", false, null);
        }

        setResult(Activity.RESULT_OK);
        //goToMainActivity();
    }

    @Override
    public void onCancelNotificationSubscriptionFailure(NotificationSubscriptionRequestResult subscriptionRequestResult) {
        subscriptionProgressBar.setVisibility(View.GONE);
        if (subscriptionRequestResult.getError() != null) {
            showSubscriptionFailed(subscriptionRequestResult.getError().getDetail());
            if (subscriptionRequestResult.getError().getRestError() != null) {
                RestError restError = subscriptionRequestResult.getError().getRestError();
                String errorParameter = restError.getErrorParameter();
                if (errorParameter != null) {
                    townNameEditTextDropDown.setError(restError.getErrorParameterValue());
                }
            }
            Toast.makeText(getApplicationContext(), getString(R.string.cancel_subscription_call_failed), Toast.LENGTH_SHORT).show();
            unSubscribeAllButton.setEnabled(true);
        }
    }

    @Override
    public void onFragmentClosed(SelectedTownFragment fragment) {
        notificationSubscriptionViewModel.townDataRemoved(fragment.getTownName());
        if (notificationSubscriptionViewModel.getAllValidatedTownNames().size() < MAX_NUM_OF_TOWNS) {
            townNameTextInputLayout.setEnabled(true);
        }
    }


    public void removeAllTownsFragments() {
        for (SelectedTownFragment fragment : townsFragments) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        townsFragments.clear();
    }

    /**
     * Show the dialog to confirm all Subscriptions cancelation
     */
    private void showConfirmCancelSubscriptionsDialog() {
        // Create an instance of the dialog fragment and show it
        CancelSubscriptionDialogFragment dialog = new CancelSubscriptionDialogFragment();
        dialog.show(getSupportFragmentManager(), "CancelSubscriptionDialogFragment");
    }

    /*** CANCEL Subscriptions DIALOGS LISTENERS ****/

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (Utils.isOnline(getApplicationContext())) {
            unSubscribeAllButton.setEnabled(false);
            subscribeButton.setEnabled(false);
            startCancelSubscriptionAsyncTask();
        } else {
            Utils.showNoInternetToast(getApplicationContext());
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    /*** DIALOGS LISTENERS *** END ***/



    private void hideKeyboardForTownNameInput() {
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(townNameEditTextDropDown.getWindowToken(), 0);
    }

    /**
     * Shows all currently subscribed town names into respective CardView
     */
    private void fillInCurrentSubscriptionsCardView(NotificationSubscriptionPreferencesHelper notificationSubscriptionPreferencesHelper) {
        if (notificationSubscriptionPreferencesHelper.getAllTownsTopicSelected()) {
            TextView allTownsTextView = new TextView(this);
            allTownsTextView.setText(getString(R.string.all_towns));
            allTownsTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            alreadySelectedSubscriptionTownsLayout.addView(allTownsTextView);
        } else {
            List<String> townNames = notificationSubscriptionPreferencesHelper.getTowns();
            if (!townNames.isEmpty()) {
                int i=0; // to count if it is last item of the list
                StringBuilder allTowns = new StringBuilder();
                for (String townName : townNames) {
                    i++;
                    if (i < townNames.size()) {
                        allTowns.append( townName + ", ");
                    } else {
                        allTowns.append(townName);
                    }
                }
                TextView townTextView = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(10,0,0,0);
                townTextView.setLayoutParams(params);
                townTextView.setText(allTowns.toString());
                townTextView.setTypeface(Typeface.DEFAULT_BOLD);
                alreadySelectedSubscriptionTownsLayout.addView(townTextView);
            }
        }
    }

    /**
     * Clears list of currently subscribed town names within respective CardView
     * and inserts default TextView
     */
    private void clearCurrentSubscriptionsCardView() {
        alreadySelectedSubscriptionTownsLayout.removeAllViews();
    }

    private View.OnClickListener createEditOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // allow/show card view to edit towns
                editTownsCardView.setVisibility(View.VISIBLE);
                // clear list of selected towns
                removeAllTownsFragments();
                // Clear model before new Edit
                notificationSubscriptionViewModel.clear();

                // show current data saved in NotificationSubscriptionPreferencesHelper
                // enter initial data to model
                if (notificationSubscriptionPreferencesHelper.getAllTownsTopicSelected()) {
                    notificationSubscriptionViewModel.townDataChanged(getApplicationContext(), "", true, null);
                } else {
                    List<String> townNames = notificationSubscriptionPreferencesHelper.getTowns();
                    if (!townNames.isEmpty()) {
                        notificationSubscriptionViewModel.townDataChanged(getApplicationContext(), "", false, townNames);
                    }
                }
                // disable edit button
                editButton.setEnabled(false);
                // disable selectedTownsCardView to disallow onClick
                selectedTownsCardView.setEnabled(false);
            }
        };
    }
}