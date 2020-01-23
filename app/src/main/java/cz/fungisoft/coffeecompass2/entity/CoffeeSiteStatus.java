package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CoffeeSiteStatus extends CoffeeSiteEntityParcelable {

    public CoffeeSiteStatus() {
        super();
    }

    public CoffeeSiteStatus(int id, String entityValue) {
        super(id, entityValue);
    }

    protected CoffeeSiteStatus(Parcel in) {
        super(in);
    }

    @Override
    @SerializedName("status")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("status")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }


}
