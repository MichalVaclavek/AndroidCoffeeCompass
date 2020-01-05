package cz.fungisoft.coffeecompass2.activity.data.model;

import java.io.Serializable;

/**
 * Class to hold REST error response mapped from JSON.
 * According coffeecompass.cz REST API
 *
 * Based on RFC7807 and https://www.baeldung.com/rest-api-error-handling-best-practices
 *  * <p>
 *  * This schema is composed of five parts:<br>
 *
 *  *   type — A URI identifier that categorizes the error<br>
 *  *   title — A brief, human-readable message about the error<br>
 *  *   status — The HTTP response code (optional)<br>
 *  *   detail — A human-readable explanation of the error<br>
 *  *   instance — A URI that identifies the specific occurrence of the error<br>
 *  *   Instead of using our custom error response body, we can convert our body to:<br>
 *  * <p>
 *  *   Example:
 *  *
 *  *    {
 *  *       "type": "/errors/incorrect-user-pass",<br>
 *  *       "title": "Incorrect username or password.",<br>
 *  *       "status": 403,<br>
 *  *       "detail": "Authentication failed due to incorrect username or password.",<br>
 *  *       "instance": "/login/log/abc123"<br>
 *  *   }
 *  *
 *  *   Doplneno o parametry: errorParameter, errorParameterValue
 *  *   pro pripad chyb pri validaci vstupnich parametru POST a PUT requestu
 *
 */
public class RestError implements Serializable {

    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;

    private String errorParameter;
    private String errorParameterValue;


    public RestError(String type, String title, int status, String detail, String instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public RestError() {
        this.type = "Unknown";
        this.title = "Not Available";
        this.status = 0;
        this.detail = "Not Available";
        this.instance = "Not Available";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getErrorParameter() {
        return errorParameter;
    }

    public void setErrorParameter(String errorParameter) {
        this.errorParameter = errorParameter;
    }

    public String getErrorParameterValue() {
        return errorParameterValue;
    }

    public void setErrorParameterValue(String errorParameterValue) {
        this.errorParameterValue = errorParameterValue;
    }

}
