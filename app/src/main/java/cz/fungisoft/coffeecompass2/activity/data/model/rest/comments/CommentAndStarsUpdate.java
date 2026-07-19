package cz.fungisoft.coffeecompass2.activity.data.model.rest.comments;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Request body for updating an existing Comment and Stars for CoffeeSite.
 */
public class CommentAndStarsUpdate {

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("text")
    private String text;

    @Expose
    @SerializedName("coffeeSiteId")
    private String coffeeSiteId;

    @Expose
    @SerializedName("userId")
    private String userId;

    @Expose
    @SerializedName("starsFromUser")
    private int starsFromUser;

    public CommentAndStarsUpdate(String id, String text, String coffeeSiteId, String userId,
                                 int starsFromUser) {
        this.id = id;
        this.text = text;
        this.coffeeSiteId = coffeeSiteId;
        this.userId = userId;
        this.starsFromUser = starsFromUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCoffeeSiteId() {
        return coffeeSiteId;
    }

    public void setCoffeeSiteId(String coffeeSiteId) {
        this.coffeeSiteId = coffeeSiteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStarsFromUser() {
        return starsFromUser;
    }

    public void setStarsFromUser(int starsFromUser) {
        this.starsFromUser = starsFromUser;
    }
}
