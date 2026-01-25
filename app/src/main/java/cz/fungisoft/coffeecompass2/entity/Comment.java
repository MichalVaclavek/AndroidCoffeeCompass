package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Comment belonging to a CoffeeSite.
 * Used for reading Comments from server.
 */
@Entity(tableName = "comment_table")
public class Comment extends CoffeeSiteEntity implements Serializable, Parcelable,  Comparable<Comment> {

    @Expose
    @SerializedName("text")
    private String text;

    @Expose
    @SerializedName("created")
    @Ignore
    private Date created;

    private String createdOnString;

    @Expose
    @SerializedName("coffeeSiteID")
    private String coffeeSiteID;

    @Expose
    @SerializedName("userName")
    private String userName;

    @Expose
    @SerializedName("userId")
    private String userId;

    /**
     * The server sends also info about stars rating from the UserName for this
     * CoffeeSite to be displayed together with comment
     */
    @Expose
    @SerializedName("starsFromUser")
    private int starsFromUser = 0;

    @Expose
    @SerializedName("canBeDeleted")
    private boolean canBeDeleted;

    @Ignore
    private final SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MM. yyyy HH:mm");

    protected Comment(Parcel in) {
        id = in.readString();
        text = in.readString();
        createdOnString = in.readString();
        coffeeSiteID = in.readString();
        userName = in.readString();
        userId = in.readString();
        starsFromUser = in.readInt();
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
        dest.writeString(id);
        dest.writeString(text);
        dest.writeString(createdOnString);
        dest.writeString(coffeeSiteID);
        dest.writeString(userName);
        dest.writeString(userId);
        dest.writeInt(starsFromUser);
        dest.writeByte((byte) (canBeDeleted ? 1 : 0));
    }

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        this.text = newText;
    }

    public Date getCreated() {
        return created;
    }

    public String getCoffeeSiteID() {
        return coffeeSiteID;
    }

    public void setCoffeeSiteID(String coffeeSiteID) {
        this.coffeeSiteID = coffeeSiteID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    public void setCanBeDeleted(boolean canBeDeleted) {
        this.canBeDeleted = canBeDeleted;
    }

    public void setCreated(Date created) {
        this.created = created;
        if (this.created != null) {
            this.createdOnString = dateFormater.format(this.created);
        }
    }

    public String getCreatedOnString() {
        if (createdOnString == null && created != null) {
            setCreated(created);
        }
        return createdOnString;
    }

    public void setCreatedOnString(String createdOnString) {

        this.createdOnString = createdOnString;

        Date created;
        try {
            created = dateFormater.parse( this.createdOnString);
        } catch (ParseException e) {
            created = new Date();
        }

        this.created = created;
    }

    public int getStarsFromUser() {
        return starsFromUser;
    }

    public void setStarsFromUser(int starsFromUser) {
        this.starsFromUser = starsFromUser;
    }

    public Comment() {}

    public Comment(String emptyText) {
        this("", emptyText, "", "", "", false, 0);
    }

    private Comment(String id, String commentText, String coffeeSiteId, String userName, String userId, boolean canBeDeleted, int starsFromUserForTheCoffeeSite) {
        this.id = id;
        this.text = commentText;
        this.coffeeSiteID = coffeeSiteId;
        this.userName = userName;
        this.userId = userId;
        this.starsFromUser = starsFromUserForTheCoffeeSite;
        this.canBeDeleted = canBeDeleted;
        setCreated(new Date());
    }

    public Comment(String id, String commentText, Date createdOn, String coffeeSiteId, String userName, String userId, boolean canBeDeleted, int starsFromUserForTheCoffeeSite) {
        this(id, commentText, coffeeSiteId, userName, userId, canBeDeleted, starsFromUserForTheCoffeeSite);
        setCreated(createdOn);
    }

    public Comment(String id, String commentText, String createdOnString, String coffeeSiteId, String userName, String userId, boolean canBeDeleted, int starsFromUserForTheCoffeeSite) {
        this(id, commentText, coffeeSiteId, userName, userId, canBeDeleted, starsFromUserForTheCoffeeSite);
        setCreatedOnString(createdOnString);
    }

    @Override
    public int compareTo(Comment o) {
        if (getCreated() == null || o.getCreated() == null) return 0;
        return getCreated().compareTo(o.getCreated());
    }
}