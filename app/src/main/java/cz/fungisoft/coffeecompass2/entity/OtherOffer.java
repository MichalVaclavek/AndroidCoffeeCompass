package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "other_offer_table")
public class OtherOffer extends CoffeeSiteEntity implements Parcelable {

    public OtherOffer(String id, String entityValue) {
        super(id);
        this.otherOffer = entityValue;
    }

    public OtherOffer() {
        super();
    }

    @Expose
    @SerializedName("otherOffer")
    private String otherOffer;

    public String getOtherOffer() {
        return otherOffer == null ? "" : otherOffer;
    }

    public void setOtherOffer(String otherOffer) {
        this.otherOffer = otherOffer;
    }

    protected OtherOffer(Parcel in) {
        this.id = in.readString();
        otherOffer = in.readString();
    }

    public static final Creator<OtherOffer> CREATOR = new Creator<OtherOffer>() {
        @Override
        public OtherOffer createFromParcel(Parcel in) {
            return new OtherOffer(in);
        }

        @Override
        public OtherOffer[] newArray(int size) {
            return new OtherOffer[size];
        }
    };

    @Override
    public String toString() {
        return getOtherOffer();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(otherOffer);
    }
}
