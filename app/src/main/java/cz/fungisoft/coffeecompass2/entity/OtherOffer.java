package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class OtherOffer extends CoffeeSiteEntityParcelable {

    public OtherOffer(int id, String entityValue) {
        super(id, entityValue);
    }

    protected OtherOffer(Parcel in) {
        super(in);
    }

    public OtherOffer() {
        super();
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
    @SerializedName("offer")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("offer")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }
}
