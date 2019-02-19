package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {

    private Integer id;

    private String commentText;

    private Date createdOn;

    private Integer coffeeSiteId;

    private String userName;

    private boolean canBeDeleted;

    public Comment(Integer id, String commentText, Date createdOn, Integer coffeeSiteId, String userName, boolean canBeDeleted) {
        this.id = id;
        this.commentText = commentText;
        this.createdOn = createdOn;
        this.coffeeSiteId = coffeeSiteId;
        this.userName = userName;
        this.canBeDeleted = canBeDeleted;
    }
}
