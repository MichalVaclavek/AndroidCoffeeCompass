package cz.fungisoft.coffeecompass2.activity.ui.register;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import cz.fungisoft.coffeecompass2.R;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.BindView;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoggedInUserView;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginRegisterViewModel;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginViewModelFactory;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.UserRegisterServiceListener;

/**
 * Activity to register new user. Based on LoginActivity.
 */
public class SignupActivity extends AppCompatActivity implements UserRegisterServiceListener {

    private static final String TAG = "SignupActivity";

    private LoginRegisterViewModel registerViewModel;

    @BindView(R.id.input_name) EditText userNameEditText;
    @BindView(R.id.input_email) EditText emailEditText;
    @BindView(R.id.input_password) EditText passwordEditText;
    @BindView(R.id.btn_signup) Button signupButton;
    @BindView(R.id.link_login) TextView loginLink;

    @BindView(R.id.progress_signup) ProgressBar registerProgressBar;

    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userRegisterServiceConnector;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        registerViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginRegisterViewModel.class);

        ButterKnife.bind(this);

        signupButton.setEnabled(false);

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupActivity.this.onClickLogin();
            }
        });

        registerViewModel.getRegisterFormState().observe(this, new Observer<RegisterFormState>() {
            @Override
            public void onChanged(@Nullable RegisterFormState registerFormState) {
                if (registerFormState == null) {
                    return;
                }
                signupButton.setEnabled(registerFormState.isDataValid());
                if (registerFormState.getUsernameError() != null) {
                    userNameEditText.setError(getString(registerFormState.getUsernameError()));
                }
                if (registerFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(registerFormState.getPasswordError()));
                }
                if (registerFormState.getEmailError() != null) {
                    emailEditText.setError(getString(registerFormState.getEmailError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            // Validate input in registerViewModel
            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(userNameEditText.getText().toString(),
                                                      emailEditText.getText().toString(),
                                                      passwordEditText.getText().toString()
                                                      );
            }
        };

        userNameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);

        doBindUserRegisterService();
    }

    /**
     * This Activity is accesible only from LoginActivity, and going back to LoginActivity
     * can be done using back button. No need to start LoginActivity again.
     */
    private void onClickLogin() {
//        Intent loginIntent = new Intent(this, LoginActivity.class);
//        //loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        this.startActivity(loginIntent);

        this.onBackPressed();
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegisterFailed(String errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }


    // ** UserLogin Service connection/disconnection ** //

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    private void doBindUserRegisterService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userRegisterServiceConnector = new UserAccountServiceConnector(this);
        if (bindService(new Intent(this, UserAccountService.class),
                userRegisterServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
            //userAccountService.addUserRegisterServiceListener(this);
        } else {
            Log.e(TAG, "Error: The requested 'UserAccountService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserRegisterService() {
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            unbindService(userRegisterServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (userAccountService != null) {
        //    userAccountService.removeUserRegisterServiceListener(this);
        //}
        doUnbindUserRegisterService();
    }

    /**
     * Common actions after registration result is received from Service
     * @param loginResult
     */
    private void evaluateRegisterResult(LoginOrRegisterResult loginResult) {
        registerProgressBar.setVisibility(View.GONE);
        //signupButton.setEnabled(true);
        if (!userNameEditText.getText().toString().isEmpty()
            && !emailEditText.getText().toString().isEmpty()
            && !passwordEditText.getText().toString().isEmpty()) {
            signupButton.setEnabled(true);
        }
        if (loginResult == null) {
            Toast.makeText(getBaseContext(), getString(R.string.user_register_failure), Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity() {
        // go to MainActivity
        Intent i = new Intent(SignupActivity.this, MainActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }


    @Override
    public void onUserRegisterSuccess(LoginOrRegisterResult registerResult) {

        evaluateRegisterResult(registerResult);

        if (registerResult.getSuccess() != null) {
            updateUiWithUser(registerResult.getSuccess());
        }
        setResult(Activity.RESULT_OK);

        goToMainActivity();
    }

    @Override
    public void onUserRegisterFailure(LoginOrRegisterResult registerResult) {
        evaluateRegisterResult(registerResult);

        if (registerResult.getError() != null) {
            showRegisterFailed(registerResult.getError().getDetail());
            String errorParameter = registerResult.getError().getErrorParameter();
            if (errorParameter != null) {
                if (errorParameter.equals("userName")) {
                    userNameEditText.setError(registerResult.getError().getErrorParameterValue());
                }
                if (errorParameter.equals("email")) {
                    emailEditText.setError(registerResult.getError().getErrorParameterValue());
                }
            }
        }
        //goToMainActivity();
    }

    @Override
    public void onUserRegisterServiceConnected() {

        userAccountService = userRegisterServiceConnector.getUserLoginService();
        userAccountService.addUserRegisterServiceListener(this);

        final String deviceID = Utils.getDeviceID(this);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (Utils.isOnline()) {
                        userAccountService.register(userNameEditText.getText().toString(),
                                                    passwordEditText.getText().toString(),
                                                    emailEditText.getText().toString(),
                                                    deviceID);
                    } else {
                        Utils.showNoInternetToast(getApplicationContext());
                    }
                }
                return false;
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline()) {
                    registerProgressBar.setVisibility(View.VISIBLE);
                    signupButton.setEnabled(false);
                    userAccountService.register(userNameEditText.getText().toString(),
                                                passwordEditText.getText().toString(),
                                                emailEditText.getText().toString(),
                                                deviceID);
                } else {
                    Utils.showNoInternetToast(getApplicationContext());
                }
            }
        });
    }

    /**
     * Needed to adding this class to list of Listeners of the UserAccountService
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignupActivity that = (SignupActivity) o;
        return Objects.equals(userNameEditText, that.userNameEditText) &&
                Objects.equals(emailEditText, that.emailEditText) &&
                Objects.equals(passwordEditText, that.passwordEditText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userNameEditText, emailEditText, passwordEditText);
    }
}
