package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

/**
 * Class to hold information fields needed for user login or register.
 * Used as an input for REST requests to coffeecompass.cz server.
 */
public class UserLoginOrRegisterInputData {

    private String userName;
    private String deviceID;
    private String email;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserLoginOrRegisterInputData(String userName, String deviceID, String email, String password) {
        this.userName = userName;
        this.deviceID = deviceID;
        this.password = password;
        this.email = "";
        if (email != null && !email.isEmpty()) {
            this.email = email;
        }
    }

}
