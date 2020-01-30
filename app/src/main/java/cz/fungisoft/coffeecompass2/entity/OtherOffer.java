package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherOffer extends CoffeeSiteEntity implements Parcelable {

    public OtherOffer(int id, String entityValue) {
        super(id);
        this.offer = entityValue;
    }

    public OtherOffer() {
        super();
    }

    @Expose
    @SerializedName("offer")
    private String offer;

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    protected OtherOffer(Parcel in) {
        this.id = in.readInt();
        offer = in.readString();
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
        return offer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(offer);
    }
}
