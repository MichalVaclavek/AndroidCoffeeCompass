package cz.fungisoft.coffeecompass2.asynctask.user;

import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;

/**
 * Async task for user login REST request call
 */
public class LoginUserRESTAsyncTask {

    private final String userName;
    private final String deviceID;
    private final String password;

    private final UserAccountRepository loginRepository;

    public LoginUserRESTAsyncTask(String username, String password, String deviceID, UserAccountRepository loginRepository) {
        super();
        this.deviceID = deviceID;
        this.userName = username;
        this.password = password;
        this.loginRepository = loginRepository;
    }

    public void execute() {
        loginRepository.login(userName, password, deviceID);
    }
}
