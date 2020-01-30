package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Not used in mobile app, yet
 */
public class NextToMachineType extends CoffeeSiteEntity implements Parcelable {

    public NextToMachineType(int id, String entityValue) {

        super(id);
        this.type = entityValue;
    }


    public NextToMachineType() {
        super();
    }

    protected NextToMachineType(Parcel in) {
        this.id = in.readInt();
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Expose
    @SerializedName("type")
    private String type;

    @Override
    public String toString() {
        return type;
    }

}
