package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

public class CoffeeSiteRecordStatus extends CoffeeSiteEntityParcelable {

    public CoffeeSiteRecordStatus() {
        super();
    }

    public CoffeeSiteRecordStatus(int id, String entityValue) {
        super(id, entityValue);
    }

    protected CoffeeSiteRecordStatus(Parcel in) {
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