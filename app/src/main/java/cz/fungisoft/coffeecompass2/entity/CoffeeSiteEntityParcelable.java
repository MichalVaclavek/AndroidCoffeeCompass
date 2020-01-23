package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class CoffeeSiteEntityParcelable extends CoffeeSiteEntity implements Parcelable {

    public CoffeeSiteEntityParcelable() {
        super();
    }

    public CoffeeSiteEntityParcelable(int id, String entityValue) {
        super(id, entityValue);
    }

    protected CoffeeSiteEntityParcelable(Parcel in) {
        super();
        id = in.readInt();
        entityValue = in.readString();
    }

    public static final Creator<CoffeeSiteEntityParcelable> CREATOR = new Creator<CoffeeSiteEntityParcelable>() {
        @Override
        public CoffeeSiteEntityParcelable createFromParcel(Parcel in) {
            return new CoffeeSiteEntityParcelable(in);
        }

        @Override
        public CoffeeSiteEntityParcelable[] newArray(int size) {
            return new CoffeeSiteEntityParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(entityValue);
    }

    @Override
    public String toString() {
        return entityValue;
    }

}
