package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CoffeeSiteType extends CoffeeSiteEntityParcelable {

    public CoffeeSiteType(int id, String entityValue) {
        super(id, entityValue);
    }

    protected CoffeeSiteType(Parcel in) {
        super(in);
    }

    public CoffeeSiteType() {
        super();
    }

    @Override
    @SerializedName("coffeeSiteType")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("coffeeSiteType")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }

}
