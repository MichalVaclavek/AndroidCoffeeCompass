package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PriceRange extends CoffeeSiteEntityParcelable {

    public PriceRange(int id, String entityValue) {
        super(id, entityValue);
    }

    protected PriceRange(Parcel in) {
        super(in);
    }

    public PriceRange() {
        super();
    }

    @Override
    @SerializedName("priceRange")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("priceRange")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }
}
