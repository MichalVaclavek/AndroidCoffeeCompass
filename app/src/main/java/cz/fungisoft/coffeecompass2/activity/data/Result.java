package cz.fungisoft.coffeecompass2.activity.data;

import cz.fungisoft.coffeecompass2.activity.data.model.RestError;

/**
 * A generic class that holds a result success w/ data or an exception error
 * or an REST Error.
 */
public class Result<T> {
    // hide the private constructor to limit subclass types (Success, Error)
    private Result() {
    }

    @Override
    public String toString() {
        if (this instanceof Result.Success) {
            Result.Success success = (Result.Success) this;
            return "Success[data=" + success.getData().toString() + "]";
        } else if (this instanceof Result.Error) {
            Result.Error error = (Result.Error) this;
            return "Error[exception=" + error.getException().toString() + "]";
        }
        return "";
    }

    // Success sub-class
    public final static class Success<T> extends Result {

        private T data;

        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return this.data;
        }
    }

    // Error sub-class
    public final static class Error extends Result {

        private Exception exception;
        private RestError error;

        private String detailToDisplay;

        public Error(Exception error) {

            this.exception = error;
            this.detailToDisplay = error.getMessage();
        }
        public Error(RestError error) {

            this.error = error;
            this.detailToDisplay = error.getDetail();
        }

        public Exception getException() {

            return this.exception;
        }

        public RestError getRestError() {
            return this.error;
        }

        public String getDetail() {
            return detailToDisplay;
        }
    }
}
