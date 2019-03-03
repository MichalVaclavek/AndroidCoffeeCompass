package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A comment belonging to a CoffeeSite
 */
public class Comment implements Serializable {

    private Integer id;

    private String commentText;

    private Date createdOn;

    private String createdOnString;

    private Integer coffeeSiteId;

    private String userName;

    private boolean canBeDeleted;

    public Integer getId() {
        return id;
    }

    public String getCommentText() {
        return commentText;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Integer getCoffeeSiteId() {
        return coffeeSiteId;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    public String getCreatedOnString() {
        return createdOnString;
    }

    public void setCreatedOnString(String createdOnString) {
        this.createdOnString = createdOnString;
    }

    private Comment(Integer id, String commentText, Integer coffeeSiteId, String userName, boolean canBeDeleted) {
        this.id = id;
        this.commentText = commentText;
        this.coffeeSiteId = coffeeSiteId;
        this.userName = userName;
        this.canBeDeleted = canBeDeleted;
    }

    public Comment(Integer id, String commentText, Date createdOn, Integer coffeeSiteId, String userName, boolean canBeDeleted) {
        this(id, commentText, coffeeSiteId, userName, canBeDeleted);
        this.createdOn = createdOn;
    }

    public Comment(Integer id, String commentText, String createdOnString, Integer coffeeSiteId, String userName, boolean canBeDeleted) {
        this(id, commentText, coffeeSiteId, userName, canBeDeleted);
        this.createdOnString = createdOnString;

        Date created;
        SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy HH:mm");
        try {
            created  = format.parse ( this.createdOnString);
        } catch (ParseException e) {
            created = new Date();
        }

        this.createdOn = created;
    }

}
