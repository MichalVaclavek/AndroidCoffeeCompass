package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
public class CoffeeSort extends CoffeeSiteEntity implements Parcelable {

    public CoffeeSort(int id, String entityValue) {
        super(id);
    }


    public CoffeeSort() {
        super();
    }

    protected CoffeeSort(Parcel in) {
        this.id = in.readInt();
        coffeeSort = in.readString();
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

    public String getCoffeeSort() {
        return coffeeSort;
    }

    public void setCoffeeSort(String coffeeSort) {
        this.coffeeSort = coffeeSort;
    }

    @Expose
    @SerializedName("coffeeSort")
    private String coffeeSort;

    @Override
    public String toString() {
        return  coffeeSort;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(coffeeSort);
    }
}
