package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "site_location_type_table")
public class SiteLocationType extends CoffeeSiteEntity implements Parcelable {

    public SiteLocationType(int id, String entityValue) {
        super(id);
        this.locationType = entityValue;
    }


    public SiteLocationType() {
        super();
    }

    protected SiteLocationType(Parcel in) {
        this.id = in.readInt();
        locationType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(locationType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SiteLocationType> CREATOR = new Creator<SiteLocationType>() {
        @Override
        public SiteLocationType createFromParcel(Parcel in) {
            return new SiteLocationType(in);
        }

        @Override
        public SiteLocationType[] newArray(int size) {
            return new SiteLocationType[size];
        }
    };

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    @Expose
    @SerializedName("locationType")
    private String locationType;

    @Override
    public String toString() {
        return locationType;
    }

}
