package cz.fungisoft.coffeecompass2.activity.data;

import cz.fungisoft.coffeecompass2.activity.data.model.LoggedInUser;
import cz.fungisoft.coffeecompass2.activity.data.model.UserPreferenceHelper;

/**
 * Class that requests authentication and user information from the remote data source UserAccountDataSource.
 */
public class UserAccountRepository {

    private static volatile UserAccountRepository instance;

    private UserAccountDataSource dataSource;
    private UserPreferenceHelper preferenceHelper;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private UserAccountRepository(UserAccountDataSource dataSource, UserPreferenceHelper preferenceHelper) {
        this.dataSource = dataSource;
        this.preferenceHelper = preferenceHelper;
    }

    public static UserAccountRepository getInstance(UserAccountDataSource dataSource, UserPreferenceHelper preferenceHelper) {
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

//    public void logout(String userName) {
//        if (userName.equals(user.getUserName())) {
//            logout();
//        }
//    }

    /**
     * Calls logou functionality of the dataSource,
     * if the user is logged-in
     */
    public void logout() {
        //user = null;
        //preferenceHelper.removeUserData();
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

//    public void delete(String userName) {
//        logout(userName);
//        dataSource.delete(user);
//    }

    public void delete() {
        if (getLoggedInUser() != null) {
            dataSource.delete(getLoggedInUser());
            //preferenceHelper.removeUserData();
        }
        //logout();
    }
}
