package cz.fungisoft.coffeecompass2.activity.data.model.rest.notification;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Class for holding info about list of towns for notification subscription, or
 * for Cancel of notification subscription.
 * <p>
 * Used by REST interface to perform subscribe operation on server coffeecompass.cz
 * Name of fields according coffeecompass.cz REST API
 */
public class NotificationSubscription {

    /**
     * Token string assigned by Firebase to the device
     */
    @Expose
    private String token;
    /**
     * Main topic of subscription (for example: 'new_coffeeSite')
     */
    @Expose
    private String topic;


    /**
     * List of towns for new CoffeeSites notification subscription as selected by user in respetive Activity
     */
    @Expose
    private List<String> subTopics;

    public NotificationSubscription() {
    }

    public NotificationSubscription(String topic) {
        setTopic(topic);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTownNames(List<String> towns) {
        this.subTopics = towns;
    }

}
