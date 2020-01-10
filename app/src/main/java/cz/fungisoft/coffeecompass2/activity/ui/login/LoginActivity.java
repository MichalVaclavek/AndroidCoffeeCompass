package cz.fungisoft.coffeecompass2.activity.ui.login;

import android.app.Activity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

import butterknife.BindView;
import cz.fungisoft.coffeecompass2.R;
import cz.fungisoft.coffeecompass2.Utils;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.activity.ui.register.SignupActivity;

import butterknife.ButterKnife;
import cz.fungisoft.coffeecompass2.services.UserAccountService;
import cz.fungisoft.coffeecompass2.services.UserAccountServiceConnector;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceConnectionListener;
import cz.fungisoft.coffeecompass2.services.interfaces.UserLoginServiceListener;

public class LoginActivity extends AppCompatActivity implements UserLoginServiceListener, UserLoginServiceConnectionListener {

    private LoginRegisterViewModel loginViewModel;

    private static final String TAG = "LoginActivity";
    //private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_email) EditText usernameEditText;
    @BindView(R.id.input_password) EditText passwordEditText;
    @BindView(R.id.btn_login) Button loginButton;
    @BindView(R.id.link_signup) TextView signupLink;
    @BindView(R.id.progress) ProgressBar loginProgressBar;

    //private ProgressDialog loadingProgressDialog = null;
    protected UserAccountService userAccountService;
    private UserAccountServiceConnector userLoginServiceConnector;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                                           .get(LoginRegisterViewModel.class);

        ButterKnife.bind(this);

        loginButton.setEnabled(false);

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

        // UserLogin service connection
        doBindUserLoginService();
    }

    private void onClickSignUp() {
        Intent signUpIntent = new Intent(this, SignupActivity.class);
        signUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(signUpIntent);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(String errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
    }

    // ** UserLogin Service connection/disconnection ** //

    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbindUserLoginService;

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
        //remove listeners
        if (userAccountService != null) {
            userAccountService.removeUserLoginServiceListener(this);
        }
        if (mShouldUnbindUserLoginService) {
            // Release information about the service's state.
            unbindService(userLoginServiceConnector);
            mShouldUnbindUserLoginService = false;
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindUserLoginService();
        super.onDestroy();
    }

    /**
     * Common actions after login result received from service.
     *
     * @param loginResult
     */
    private void evaluateLoginResult(LoginOrRegisterResult loginResult) {
        loginProgressBar.setVisibility(View.GONE);
        //loginButton.setEnabled(true);
        if (!usernameEditText.getText().toString().isEmpty()
                && !passwordEditText.getText().toString().isEmpty()) {
            loginButton.setEnabled(true);
        }
        if (loginResult == null) {
            Toast.makeText(getBaseContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity() {
        // go to MainActivity
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public void onUserLoggedInSuccess(LoginOrRegisterResult loginResult) {
        evaluateLoginResult(loginResult);

        if (loginResult.getSuccess() != null) {
            updateUiWithUser(loginResult.getSuccess());
        }
        setResult(Activity.RESULT_OK);
        goToMainActivity();
    }

    @Override
    public void onUserLoggedInFailure(LoginOrRegisterResult loginResult) {
        evaluateLoginResult(loginResult);

        String errorMessage = (loginResult.getError() != null
                               && loginResult.getError().getDetail() != null
                               && !loginResult.getError().getDetail().isEmpty() )
                                ? loginResult.getError().getDetail()
                                : getString(R.string.invalid_username);

        showLoginFailed(errorMessage);
    }

    @Override
    public void onUserLoginServiceConnected() {

        userAccountService = userLoginServiceConnector.getUserLoginService();
        userAccountService.addUserLoginServiceListener(this);
        Log.i(TAG, "This is UserAccountServie instance: " + userAccountService.toString());
        final String deviceID = Utils.getDeviceID(this);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (Utils.isOnline()) {
                        userAccountService.login(usernameEditText.getText().toString(),
                                               passwordEditText.getText().toString(),
                                               deviceID);
                    } else {
                        Utils.showNoInternetToast(getApplicationContext());
                    }
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOnline()) {
                    loginProgressBar.setVisibility(View.VISIBLE);
                    loginButton.setEnabled(false);

                    userAccountService.login(usernameEditText.getText().toString(),
                                           passwordEditText.getText().toString(),
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
        LoginActivity that = (LoginActivity) o;
        return Objects.equals(usernameEditText, that.usernameEditText) &&
                Objects.equals(passwordEditText, that.passwordEditText) &&
                Objects.equals(loginButton, that.loginButton);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usernameEditText, passwordEditText, loginButton);
    }
}
