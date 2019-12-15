package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
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

import butterknife.BindView;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.data.LoginAndRegisterDataSource;
import cz.fungisoft.coffeecompass2.activity.data.LoginAndRegisterRepository;
import cz.fungisoft.coffeecompass2.activity.ui.register.SignupActivity;

import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.services.UserLoginAndRegisterService;
import cz.fungisoft.coffeecompass2.services.UserLoginRegisterServiceConnector;
import cz.fungisoft.coffeecompass2.services.UserLoginServiceListener;

public class LoginActivity extends AppCompatActivity implements UserLoginServiceListener {

    private LoginRegisterViewModel loginViewModel;

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_email) EditText usernameEditText;
    @BindView(R.id.input_password) EditText passwordEditText;
    @BindView(R.id.btn_login) Button loginButton;
    @BindView(R.id.link_signup) TextView signupLink;
    @BindView(R.id.progress) ProgressBar loginProgressBar;


    //private Intent userLoginServiceIntent;

    //private ProgressDialog loadingProgressDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                                           .get(LoginRegisterViewModel.class);

        ButterKnife.bind(this);

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.onClickSignUp();
            }
        });

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
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

            // Validate input in loginViewModel
            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        //final String deviceID = Utils.getDeviceID(this);

        // UserLogin service connection
        //userLoginServiceIntent = new Intent(this, UserLoginAndRegisterService.class);
        //startService(userLoginServiceIntent);
        doBindUserLoginService();
    }

    private void onClickSignUp() {
        Intent signUpIntent = new Intent(this, SignupActivity.class);
        this.startActivity(signUpIntent);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    // ** UserLogin Service connection/disconnection ** //

    protected UserLoginAndRegisterService userLoginService;
    private UserLoginRegisterServiceConnector userLoginServiceConnector;

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

    private void doBindUserLoginService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        userLoginServiceConnector = new UserLoginRegisterServiceConnector(this);
        if (bindService(new Intent(this, UserLoginAndRegisterService.class),
                userLoginServiceConnector, Context.BIND_AUTO_CREATE)) {
            mShouldUnbindUserLoginService = true;
        } else {
            Log.e(TAG, "Error: The requested 'UserLoginAndRegisterService' service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    private void doUnbindUserLoginService() {
        if (userLoginService != null) {
            userLoginService.removeUserLoginServiceListener(this);
        }
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            unbindService(userLoginServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindUserLoginService();
    }

    private void evaluateLoginResult(LoginOrRegisterResult loginResult) {
        loginProgressBar.setVisibility(View.GONE);
        if (loginResult == null) {
            Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity() {
        // go to MainActivity
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onUserLoggedInSuccess(LoginOrRegisterResult loginResult) {
        //loginViewModel.getLoginResult().observe(this, new Observer<LoginOrRegisterResult>() {
            //@Override
            //public void onChanged(@Nullable LoginOrRegisterResult loginResult) {
        evaluateLoginResult(loginResult);

        if (loginResult.getSuccess() != null) {
            updateUiWithUser(loginResult.getSuccess());
        }
        setResult(Activity.RESULT_OK);

        goToMainActivity();
           // }
      //  });
    }

    @Override
    public void onUserLoggedInFailure(LoginOrRegisterResult loginResult) {
        evaluateLoginResult(loginResult);

        if (loginResult.getError() != null) {
            showLoginFailed(loginResult.getError());
        }
        goToMainActivity();
    }

    @Override
    public void onUserLoggedOut() {

    }

    @Override
    public void onUserLoginServiceConnected() {

        userLoginService = userLoginServiceConnector.getUserLoginService();
        userLoginService.addUserLoginServiceListener(this);

        final String deviceID = Utils.getDeviceID(this);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    userLoginService.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), deviceID);
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgressBar.setVisibility(View.VISIBLE);

                userLoginService.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), deviceID);
            }
        });
    }
}
