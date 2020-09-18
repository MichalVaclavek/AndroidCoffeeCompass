package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "coffee_site_type_table")
public class CoffeeSiteType extends CoffeeSiteEntity implements Parcelable {

    public CoffeeSiteType(int id, String entityValue) {
        super(id);
        this.coffeeSiteType = entityValue;
    }

    public CoffeeSiteType() {
        super();
    }

    @Expose
    @SerializedName("coffeeSiteType")
    private String coffeeSiteType;

    public String getCoffeeSiteType() {
        return coffeeSiteType;
    }

    public void setCoffeeSiteType(String coffeeSiteType) {
        this.coffeeSiteType = coffeeSiteType;
    }

    protected CoffeeSiteType(Parcel in) {
        this.id = in.readInt();
        coffeeSiteType = in.readString();
    }

    public static final Creator<CoffeeSiteType> CREATOR = new Creator<CoffeeSiteType>() {
        @Override
        public CoffeeSiteType createFromParcel(Parcel in) {
            return new CoffeeSiteType(in);
        }

        @Override
        public CoffeeSiteType[] newArray(int size) {
            return new CoffeeSiteType[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(coffeeSiteType);
    }

    @Override
    public String toString() {
        return  coffeeSiteType;
    }
}
