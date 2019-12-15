package cz.fungisoft.coffeecompass2.activity.data;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.UserPreferenceHelper;

/**
 * Class that requests authentication and user information from the remote data source LoginAndRegisterDataSource.
 */
public class LoginAndRegisterRepository {

    private static volatile LoginAndRegisterRepository instance;

    private LoginAndRegisterDataSource dataSource;
    private UserPreferenceHelper preferenceHelper;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginAndRegisterRepository(LoginAndRegisterDataSource dataSource, UserPreferenceHelper preferenceHelper) {
        this.dataSource = dataSource;
        this.preferenceHelper = preferenceHelper;
    }

    public static LoginAndRegisterRepository getInstance(LoginAndRegisterDataSource dataSource, UserPreferenceHelper preferenceHelper) {
        if (instance == null) {
            instance = new LoginAndRegisterRepository(dataSource, preferenceHelper);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public LoggedInUser getLoggedInUser() {
        return user;
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

    public void register(String username, String password, String email, String deviceID) {
        dataSource.register(username, password, email, deviceID);
    }
}
