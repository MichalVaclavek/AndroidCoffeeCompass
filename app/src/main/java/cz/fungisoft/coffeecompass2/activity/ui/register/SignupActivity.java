package cz.fungisoft.coffeecompass2.activity.ui.register;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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

import butterknife.ButterKnife;
import butterknife.BindView;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.model.UserPreferenceHelper;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoggedInUserView;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginActivity;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginRegisterViewModel;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginOrRegisterResult;
import cz.fungisoft.coffeecompass2.activity.ui.login.LoginViewModelFactory;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateService;
import cz.fungisoft.coffeecompass2.services.UserLoginAndRegisterService;
import cz.fungisoft.coffeecompass2.services.UserLoginRegisterServiceConnector;
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

    private Intent userRegisterServiceIntent;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        UserPreferenceHelper preferenceHelper = new UserPreferenceHelper(this);

        registerViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginRegisterViewModel.class);

        ButterKnife.bind(this);

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

//        registerViewModel.getLoginResult().observe(this, new Observer<LoginOrRegisterResult>() {
//            @Override
//            public void onChanged(@Nullable LoginOrRegisterResult registerResult) {
//
//                registerProgressBar.setVisibility(View.GONE);
//                if (registerResult == null) {
//                    Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();
//                    return;
//                }
//
//                if (registerResult.getError() != null) {
//                    showRegisterFailed(registerResult.getError());
//                }
//                if (registerResult.getSuccess() != null) {
//                    updateUiWithUser(registerResult.getSuccess());
//                }
//                setResult(Activity.RESULT_OK);
//
//                // go to MainActivity
//                Intent i = new Intent(SignupActivity.this, MainActivity.class);
//                startActivity(i);
//                finish();
//            }
//        });

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


        //userRegisterServiceIntent = new Intent(this, UserLoginAndRegisterService.class);
        //startService(userRegisterServiceIntent);
        doBindUserRegisterService();
    }

    private void onClickLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        this.startActivity(loginIntent);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegisterFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }


    // ** UserLogin Service connection/disconnection ** //

    protected UserLoginAndRegisterService userRegisterService;
    private UserLoginRegisterServiceConnector userRegisterServiceConnector;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    private void doBindUserRegisterService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userRegisterServiceConnector = new UserLoginRegisterServiceConnector(this);
        if (bindService(new Intent(this, UserLoginAndRegisterService.class),
                userRegisterServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserLoginAndRegisterService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserRegisterService() {
        if (userRegisterService != null) {
            userRegisterService.removeUserRegisterServiceListener(this);
        }
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            unbindService(userRegisterServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindUserRegisterService();
    }

    private void evaluateRegisterResult(LoginOrRegisterResult loginResult) {
        registerProgressBar.setVisibility(View.GONE);
        if (loginResult == null) {
            Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity() {
        // go to MainActivity
        Intent i = new Intent(SignupActivity.this, MainActivity.class);
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
            showRegisterFailed(registerResult.getError());
        }
        goToMainActivity();
    }

    @Override
    public void onUserRegisterServiceConnected() {

        userRegisterService = userRegisterServiceConnector.getUserLoginService();
        userRegisterService.addUserRegisterServiceListener(this);

        final String deviceID = Utils.getDeviceID(this);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    userRegisterService.register(userNameEditText.getText().toString(),
                            passwordEditText.getText().toString(),
                            emailEditText.getText().toString(),
                            deviceID);
                }
                return false;
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerProgressBar.setVisibility(View.VISIBLE);

                userRegisterService.register(userNameEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        emailEditText.getText().toString(),
                        deviceID);
            }
        });
    }

}
