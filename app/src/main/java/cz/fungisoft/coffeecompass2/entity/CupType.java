package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Not used yet in mobile app
 */
public class CupType extends CoffeeSiteEntityParcelable {

    public CupType(int id, String entityValue) {
        super(id, entityValue);
    }

    protected CupType(Parcel in) {
        super(in);
    }

    public CupType() {
        super();
    }

    public static final Creator<CupType> CREATOR = new Creator<CupType>() {
        @Override
        public CupType createFromParcel(Parcel in) {
            return new CupType(in);
        }

        @Override
        public CupType[] newArray(int size) {
            return new CupType[size];
        }
    };

    @Override
    @SerializedName("cupType")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("cupType")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }
}
