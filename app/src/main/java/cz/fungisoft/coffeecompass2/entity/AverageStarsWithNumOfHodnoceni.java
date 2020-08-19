package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import cz.fungisoft.coffeecompass2.utils.Utils;

public class AverageStarsWithNumOfHodnoceni extends CoffeeSiteEntity implements Parcelable {

    @Expose
    @SerializedName("avgStars")
    private float avgStars;

    @Expose
    @SerializedName("numOfHodnoceni")
    private int numOfHodnoceni;

    @Expose
    @SerializedName("common")
    private String common;

    public AverageStarsWithNumOfHodnoceni() {}

    public AverageStarsWithNumOfHodnoceni(float avgStars, int numOfHodnoceni, String common) {
        this.avgStars = avgStars;
        this.numOfHodnoceni = numOfHodnoceni;
        this.common = common;
    }

    protected AverageStarsWithNumOfHodnoceni(@NotNull Parcel in) {
        avgStars = in.readFloat();
        numOfHodnoceni = in.readInt();
        common = in.readString();
    }

    public static final Creator<AverageStarsWithNumOfHodnoceni> CREATOR = new Creator<AverageStarsWithNumOfHodnoceni>() {
        @NotNull
        @Contract("_ -> new")
        @Override
        public AverageStarsWithNumOfHodnoceni createFromParcel(Parcel in) {
            return new AverageStarsWithNumOfHodnoceni(in);
        }

        @Override
        public AverageStarsWithNumOfHodnoceni[] newArray(int size) {
            return new AverageStarsWithNumOfHodnoceni[size];
        }
    };

    public float getAvgStars() {
        return avgStars;
    }

    public void setAvgStars(float avgStars) {
        this.avgStars = avgStars;
    }

    public int getNumOfHodnoceni() {
        return numOfHodnoceni;
    }

    public void setNumOfHodnoceni(int numOfHodnoceni) {
        this.numOfHodnoceni = numOfHodnoceni;
    }

    public String getCommon() {
        return common;
    }

    public void setCommon(String common) {
        this.common = common;
    }

    @Override
    public String toString() {
        return Utils.round(avgStars, 1) + " (" + numOfHodnoceni + ")";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeFloat(avgStars);
        dest.writeInt(numOfHodnoceni);
        dest.writeString(common);
    }
}
