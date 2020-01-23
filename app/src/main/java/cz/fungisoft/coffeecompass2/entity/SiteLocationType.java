package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class SiteLocationType extends CoffeeSiteEntityParcelable {

    public SiteLocationType(int id, String entityValue) {
        super(id, entityValue);
    }

    protected SiteLocationType(Parcel in) {
        super(in);
    }

    public SiteLocationType() {
        super();
    }

    @Override
    @SerializedName("locationType")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("locationType")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }
}
