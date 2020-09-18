package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "stars_quality_description_table")
public class StarsQualityDescription extends CoffeeSiteEntity implements Parcelable {

    public StarsQualityDescription(int id, String entityValue, int numOfStars) {
        super(numOfStars);
        this.quality = entityValue;
        this.setNumOfStars(numOfStars);
    }

    public StarsQualityDescription() {
        super();
    }

    @Expose(serialize = false)
    @SerializedName("quality")
    private String quality;

    @Expose(serialize = false)
    @SerializedName("numOfStars")
    private int numOfStars;

    protected StarsQualityDescription(Parcel in) {
        this.id = in.readInt();
        quality = in.readString();
        numOfStars = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(quality);
        dest.writeInt(numOfStars);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public int getNumOfStars() {
        return numOfStars;
    }

    public void setNumOfStars(int numOfStars) {
        this.numOfStars = numOfStars;
    }
}
