package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "coffee_site_record_status_table")
public class CoffeeSiteRecordStatus extends CoffeeSiteEntity implements Parcelable {

    public CoffeeSiteRecordStatus() {
        super();
    }

    public CoffeeSiteRecordStatus(int id, String entityValue) {
        super(id);
        this.status = entityValue;
    }

    @Expose
    @SerializedName("status")
    private String status;

    @Ignore
    protected CoffeeSiteRecordStatus(Parcel in) {
        id = in.readInt();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CoffeeSiteRecordStatus> CREATOR = new Creator<CoffeeSiteRecordStatus>() {
        @Override
        public CoffeeSiteRecordStatus createFromParcel(Parcel in) {
            return new CoffeeSiteRecordStatus(in);
        }

        @Override
        public CoffeeSiteRecordStatus[] newArray(int size) {
            return new CoffeeSiteRecordStatus[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return  status;
    }

}