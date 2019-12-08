package cz.fungisoft.coffeecompass2.activity.data.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.data.model.rest.JwtUserToken;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository.
 * Based on server REST API available items for current logged-in user.
 * authProvider": "string",
 *   "createdOn": {
 *     "date": 0,
 *
 *   },
 *   "createdSites": 0,
 *   "deletedSites": 0,
 *   "email": "string",
 *   "firstName": "string",
 *   "id": 0,
 *   "lastName": "string",
 *   "toManageItself": true,
 *   "updatedSites": 0,
 *   "userName": "string",
 *   "userProfiles": [
 *     {
 *       "id": 0,
 *       "type": "string"
 *     }
 *   ]
 */
public class LoggedInUser {

    private String userId;
    private String displayName;

    private String userName;

    private String email;

    private int numOfCreatedSites;
    private int numOfUpdatedSites;
    private int numOfDeletedSites;

    private String firstName;
    private String lastName;

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
    public void setCreatedOn(String createdOn) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM. yyyy HH:mm");
        try {
            this.createdOn = format.parse (createdOn);
        } catch (ParseException e) {
            this.createdOn = new Date();
        }
    }

    private Date createdOn;

    private List<String> userRoles;
    /**
     * ID to identify device to keep user loged-in
     * when he/her opens app. again.
     */
    private String deviceID;

    /**
     * Token created and sent by server to mobile app after
     * successful login. This is used for next login as identity
     * of user/device instead of username/password.
     */
    private JwtUserToken loginToken;


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getNumOfUpdatedSites() {
        return numOfUpdatedSites;
    }

    public void setNumOfUpdatedSites(int numOfUpdatedSites) {
        this.numOfUpdatedSites = numOfUpdatedSites;
    }

    public int getNumOfDeletedSites() {
        return numOfDeletedSites;
    }

    public void setNumOfDeletedSites(int numOfDeletedSites) {
        this.numOfDeletedSites = numOfDeletedSites;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<String> userRoles) {
        this.userRoles = userRoles;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public LoggedInUser()
    {}

    /**
     * Basic constructor which is used during parsing answer to login/register request.
     *
     * @param userName
     * @param email
     * @param userJwtToken
     */
    public LoggedInUser(String userName, String email, JwtUserToken userJwtToken) {
        this.userName = userName;
        this.displayName = userName;
        this.email = email;
        this.loginToken = userJwtToken;
        userRoles = new ArrayList<>();
    }

    public LoggedInUser(JwtUserToken userJwtToken) {
        this.loginToken = userJwtToken;
        userRoles = new ArrayList<>();
    }


    /* Getters and Setters */

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public JwtUserToken getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(JwtUserToken loginToken) {
        this.loginToken = loginToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNumOfCreatedSites() {
        return numOfCreatedSites;
    }

    public void setNumOfCreatedSites(int numOfCreatedSites) {
        this.numOfCreatedSites = numOfCreatedSites;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets all the attributes of this object based on currentUser
     * i.e creates clone clone of currentUser.
     * Used to get current user from inner class of JsonObjectRequest.Response.Listener<JSONObject>
     * @param currentUser
     */
    public void setUserData(LoggedInUser currentUser) {
        setUserId(currentUser.getUserId());
        setUserRoles(currentUser.getUserRoles());
        setNumOfCreatedSites(currentUser.getNumOfCreatedSites());
        setCreatedOn(currentUser.getCreatedOn());
        setUserName(currentUser.getUserName());
        setDeviceID(currentUser.getDeviceID());
        setDisplayName(currentUser.getDisplayName());
        setEmail(currentUser.getEmail());
        setFirstName(currentUser.getFirstName());
        setLastName(currentUser.getLastName());
        setLoginToken(currentUser.getLoginToken());
        setNumOfDeletedSites(currentUser.getNumOfDeletedSites());
        setNumOfUpdatedSites(currentUser.getNumOfUpdatedSites());
    }
}
