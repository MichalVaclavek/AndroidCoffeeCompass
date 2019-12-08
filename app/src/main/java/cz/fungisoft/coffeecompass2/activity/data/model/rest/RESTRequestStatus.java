package cz.fungisoft.coffeecompass2.activity.data.model.rest;

/**
 * Class to indicate if REST request thread already finished
 */
public class RESTRequestStatus {

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    private boolean finished;

    public RESTRequestStatus() {
        this.finished = false;
    }
}
