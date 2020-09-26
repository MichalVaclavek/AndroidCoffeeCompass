package cz.fungisoft.coffeecompass2.activity.data;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;

/**
 * Class that holds data about LoggedInUser. Also requests authentication attempts and user information
 * from the remote data source {@link UserAccountDataSource}.
 */
public class UserAccountRepository {

    private static volatile UserAccountRepository instance;

    private UserAccountDataSource dataSource;
    private UserPreferencesHelper preferenceHelper;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private UserAccountRepository(UserAccountDataSource dataSource, UserPreferencesHelper preferenceHelper) {
        this.dataSource = dataSource;
        this.preferenceHelper = preferenceHelper;
    }

    public static UserAccountRepository getInstance(UserAccountDataSource dataSource, UserPreferencesHelper preferenceHelper) {
        if (instance == null) {
            instance = new UserAccountRepository(dataSource, preferenceHelper);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        getLoggedInUser();
        return user != null;
    }

    public LoggedInUser getLoggedInUser() {
        if (user == null) {
            user = preferenceHelper.getSavedUserData();
        }
        return user;
    }


    /**
     * Calls logou functionality of the dataSource,
     * if the user is logged-in
     */
    public void logout() {
        if (getLoggedInUser() != null) {
            dataSource.logout(user);
        }
    }

    public void setLoggedInUser(LoggedInUser user) {
        this.user = user;

        if (this.user != null) {
            preferenceHelper.saveUserData(user);
        } else {
            preferenceHelper.removeUserData();
        }
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

    public void delete() {
        if (getLoggedInUser() != null) {
            dataSource.delete(getLoggedInUser());
        }
    }

}
