package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PriceRange extends CoffeeSiteEntity implements Parcelable {

    public PriceRange(int id, String entityValue) {

        super(id);
        this.priceRange = entityValue;
    }

    public PriceRange() {
        super();
    }

    protected PriceRange(Parcel in) {
        this.id = in.readInt();
        priceRange = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(priceRange);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PriceRange> CREATOR = new Creator<PriceRange>() {
        @Override
        public PriceRange createFromParcel(Parcel in) {
            return new PriceRange(in);
        }

        @Override
        public PriceRange[] newArray(int size) {
            return new PriceRange[size];
        }
    };

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    @Expose
    @SerializedName("priceRange")
    private String priceRange;

    @Override
    public String toString() {
        return priceRange;
    }
}
