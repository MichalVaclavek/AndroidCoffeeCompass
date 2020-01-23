package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

public class StarsQualityDescription extends CoffeeSiteEntityParcelable {

    public StarsQualityDescription(int id, String entityValue) {
        super(id, entityValue);
    }

    protected StarsQualityDescription(Parcel in) {
        super(in);
    }

    public StarsQualityDescription() {
        super();
    }

    public static final Creator<StarsQualityDescription> CREATOR = new Creator<StarsQualityDescription>() {
        @Override
        public StarsQualityDescription createFromParcel(Parcel in) {
            return new StarsQualityDescription(in);
        }

        @Override
        public StarsQualityDescription[] newArray(int size) {
            return new StarsQualityDescription[size];
        }
    };

    @Override
    @SerializedName("quality")
    public String getEntityValue() {
        return entityValue;
    }

    @Override
    @SerializedName("quality")
    public void setEntityValue(String value) {
        this.entityValue = value;
    }

    @Override
    @SerializedName("numOfStars")
    public int getId() {
        return id;
    }

    @Override
    @SerializedName("numOfStars")
    public void setId(int id) {
        this.id = id;
    }

}
