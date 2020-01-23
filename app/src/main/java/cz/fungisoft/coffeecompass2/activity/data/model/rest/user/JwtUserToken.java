package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.fungisoft.coffeecompass2.activity.data.model.rest.user.UserLoginOrRegisterRESTRequest;

/**
 *  REST user login or register response to {@link UserLoginOrRegisterRESTRequest} from coffeecompass.cz server
 */
public class JwtUserToken implements Serializable {

    private String accessToken;
    private Date expiryDate;
    private String tokenType; // usualy Bearer

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public String getExpiryDateFormated() {

        String retVal;
        SimpleDateFormat format = new SimpleDateFormat("dd.MM. yyyy HH:mm");
        retVal = format.format(expiryDate);
        return retVal;
    }

    public void setExpiryDate(String expiryDate) {

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try
        {
            this.expiryDate = format.parse(expiryDate);
        } catch(ParseException e)
        {
            this.expiryDate = new Date();
        }
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public JwtUserToken(String accessToken, Date expiryDate, String tokenType) {
        this.accessToken = accessToken;
        this.expiryDate = expiryDate;
        this.tokenType = tokenType;
    }

    public JwtUserToken(String accessToken, String expiryDate, String tokenType) {
        this.accessToken = accessToken;
        setExpiryDate(expiryDate);
        this.tokenType = tokenType;
    }

}
