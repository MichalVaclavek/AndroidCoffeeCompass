package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Not used yet in mobile app
 */
@Entity(tableName = "cup_type_table")
public class CupType extends CoffeeSiteEntity implements Parcelable {

    public CupType(int id, String entityValue) {

        super(id);
        this.cupType = entityValue;
    }


    public CupType() {
        super();
    }

    @Expose
    @SerializedName("cupType")
    private String cupType;

    protected CupType(Parcel in) {
        this.id = in.readInt();
        cupType = in.readString();
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

    public String getCupType() {
        return cupType;
    }

    public void setCupType(String cupType) {
        this.cupType = cupType;
    }

    @Override
    public String toString() {
        return  cupType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(cupType);
    }
}
