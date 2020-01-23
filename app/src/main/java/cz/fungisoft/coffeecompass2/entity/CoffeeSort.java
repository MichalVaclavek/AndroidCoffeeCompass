package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CoffeeSort extends CoffeeSiteEntityParcelable {

    public CoffeeSort(int id, String entityValue) {
        super(id, entityValue);
    }

    protected CoffeeSort(Parcel in) {
        super(in);
    }

    public CoffeeSort() {
        super();
    }

    public static final Creator<CoffeeSort> CREATOR = new Creator<CoffeeSort>() {
        @Override
        public CoffeeSort createFromParcel(Parcel in) {
            return new CoffeeSort(in);
        }

        @Override
        public CoffeeSort[] newArray(int size) {
            return new CoffeeSort[size];
        }
    };

    @Override
    @SerializedName("coffeeSort")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("coffeeSort")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }
}
