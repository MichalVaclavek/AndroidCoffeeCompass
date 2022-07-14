package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

/**
 * Class to hold information fields needed for refresh Token request obtaining new access token.
 */
public class RefreshTokenRequestData {

    private String deviceID;

    private String refreshToken;

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }


    public RefreshTokenRequestData(String refreshToken, String deviceID) {
        this.refreshToken = refreshToken;
        this.deviceID = deviceID;
    }

}
