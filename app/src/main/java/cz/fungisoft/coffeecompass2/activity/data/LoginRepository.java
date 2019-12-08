package cz.fungisoft.coffeecompass2.activity.data;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.UserPreferenceHelper;

/**
 * Class that requests authentication and user information from the remote data source LoginDataSource.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;
    private UserPreferenceHelper preferenceHelper;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource, UserPreferenceHelper preferenceHelper) {
        this.dataSource = dataSource;
        this.preferenceHelper = preferenceHelper;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource, UserPreferenceHelper preferenceHelper) {
        if (instance == null) {
            instance = new LoginRepository(dataSource, preferenceHelper);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    public void setLoggedInUser(LoggedInUser user) {
        this.user = user;

        //TODO ulozit do "UserPreferenceHelper"
        // vyzaduje Context
        preferenceHelper.saveUserData(user);
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    /**

     * @param username
     * @param password
     * @return
     */
    public void login(String username, String password, String deviceID) {
        dataSource.login(username, password, deviceID);
    }
}
