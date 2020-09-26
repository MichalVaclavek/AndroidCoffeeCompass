package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "coffee_site_status_table")
public class CoffeeSiteStatus extends CoffeeSiteEntity implements Parcelable {

    public CoffeeSiteStatus() {
        super();
    }

    public CoffeeSiteStatus(int id, String entityValue) {
        super(id);
        this.status = entityValue;
    }

    @Ignore
    protected CoffeeSiteStatus(Parcel in) {
        this.id = in.readInt();
        status = in.readString();
    }

    public static final Creator<CoffeeSiteStatus> CREATOR = new Creator<CoffeeSiteStatus>() {
        @Override
        public CoffeeSiteStatus createFromParcel(Parcel in) {
            return new CoffeeSiteStatus(in);
        }

        @Override
        public CoffeeSiteStatus[] newArray(int size) {
            return new CoffeeSiteStatus[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Expose
    @SerializedName("status")
    private String status;


    @Override
    public String toString() {
        return  status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(status);
    }
}
