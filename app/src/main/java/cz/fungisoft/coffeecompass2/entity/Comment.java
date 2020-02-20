package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Comment belonging to a CoffeeSite.
 * Used for reading Comments from server.
 */
public class Comment implements Serializable, Parcelable {

    private Integer id;

    private String text;

    private Date created;

    private String createdOnString;

    private Integer coffeeSiteID;

    private String userName;

    /**
     * The server sends also info about stars rating from the UserName for this
     * CoffeeSite to be displayed together with comment
     */
    private int starsFromUser = 0;

    private boolean canBeDeleted;

    private SimpleDateFormat dateFormater = new SimpleDateFormat("dd.MM. yyyy HH:mm");

    protected Comment(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        text = in.readString();
        createdOnString = in.readString();
        if (in.readByte() == 0) {
            coffeeSiteID = null;
        } else {
            coffeeSiteID = in.readInt();
        }
        userName = in.readString();
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
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(text);
        dest.writeString(createdOnString);
        if (coffeeSiteID == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(coffeeSiteID);
        }
        dest.writeString(userName);
        dest.writeInt(starsFromUser);
        dest.writeByte((byte) (canBeDeleted ? 1 : 0));
    }


    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Date getCreated() {
        return created;
    }

    public Integer getCoffeeSiteID() {
        return coffeeSiteID;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    public void setCreated(Date created) {
        this.created = created;
        this.createdOnString = dateFormater.format(this.created);
    }

    public String getCreatedOnString() {
        if (createdOnString == null) {
            setCreated(created);
        }
        return createdOnString;
    }

    public void setCreatedOnString(String createdOnString) {

        this.createdOnString = createdOnString;

        Date created;
        //SimpleDateFormat format = new SimpleDateFormat("dd.MM. yyyy HH:mm");
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

    public Comment(String emtpyText) {
        this(0, emtpyText, 0, "", false, 0);
    }


    private Comment(Integer id, String commentText, Integer coffeeSiteId, String userName, boolean canBeDeleted, int starsFromUserForTheCoffeeSite) {
        this.id = id;
        this.text = commentText;
        this.coffeeSiteID = coffeeSiteId;
        this.userName = userName;
        this.starsFromUser = starsFromUserForTheCoffeeSite;
        this.canBeDeleted = canBeDeleted;
        setCreated(new Date());
    }

    public Comment(Integer id, String commentText, Date createdOn, Integer coffeeSiteId, String userName, boolean canBeDeleted, int starsFromUserForTheCoffeeSite) {
        this(id, commentText, coffeeSiteId, userName, canBeDeleted, starsFromUserForTheCoffeeSite);
        setCreated(createdOn);
    }

    public Comment(Integer id, String commentText, String createdOnString, Integer coffeeSiteId, String userName, boolean canBeDeleted, int starsFromUserForTheCoffeeSite) {
        this(id, commentText, coffeeSiteId, userName, canBeDeleted, starsFromUserForTheCoffeeSite);
        setCreatedOnString(createdOnString);
    }

}