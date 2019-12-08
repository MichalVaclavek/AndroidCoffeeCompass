package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A comment belonging to a CoffeeSite
 */
public class Comment implements Serializable, Parcelable {

    private Integer id;

    private String commentText;

    private Date createdOn;

    private String createdOnString;

    private Integer coffeeSiteId;

    private String userName;

    private boolean canBeDeleted;

    protected Comment(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        commentText = in.readString();
        createdOnString = in.readString();
        if (in.readByte() == 0) {
            coffeeSiteId = null;
        } else {
            coffeeSiteId = in.readInt();
        }
        userName = in.readString();
        canBeDeleted = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(commentText);
        dest.writeString(createdOnString);
        if (coffeeSiteId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(coffeeSiteId);
        }
        dest.writeString(userName);
        dest.writeByte((byte) (canBeDeleted ? 1 : 0));
    }


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