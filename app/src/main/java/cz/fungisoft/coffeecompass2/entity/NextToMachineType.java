package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Not used in mobile app, yet
 */
public class NextToMachineType extends CoffeeSiteEntityParcelable {

    public NextToMachineType(int id, String entityValue) {
        super(id, entityValue);
    }

    protected NextToMachineType(Parcel in) {
        super(in);
    }

    public NextToMachineType() {
        super();
    }

    public static final Creator<NextToMachineType> CREATOR = new Creator<NextToMachineType>() {
        @Override
        public NextToMachineType createFromParcel(Parcel in) {
            return new NextToMachineType(in);
        }

        @Override
        public NextToMachineType[] newArray(int size) {
            return new NextToMachineType[size];
        }
    };

    @Override
    @SerializedName("type")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("type")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }
}
