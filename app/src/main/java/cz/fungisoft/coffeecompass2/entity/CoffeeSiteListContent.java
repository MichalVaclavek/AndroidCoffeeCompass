package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List of CoffeeSites to be displayed in the list
 */
public class CoffeeSiteListContent implements Parcelable {
//    public class CoffeeSiteListContent implements Serializable {

    /**
     * An array of CoffeeSite items.
     */
    private List<CoffeeSiteMovable> items = new ArrayList<>();

    protected CoffeeSiteListContent(Parcel in) {
        items = in.createTypedArrayList(CoffeeSiteMovable.CREATOR);
    }

    public static final Creator<CoffeeSiteListContent> CREATOR = new Creator<CoffeeSiteListContent>() {
        @Override
        public CoffeeSiteListContent createFromParcel(Parcel in) {
            return new CoffeeSiteListContent(in);
        }

        @Override
        public CoffeeSiteListContent[] newArray(int size) {
            return new CoffeeSiteListContent[size];
        }
    };

    public List<CoffeeSiteMovable> getItems() {
        return items;
    }

    /**
     * A map of CoffeeSite items, by ID. Used by RecyclerView in the CoffeeSiteListActivity
     */
    private Map<String, CoffeeSiteMovable> items_map = new HashMap<>();

    public Map<String, CoffeeSiteMovable> getItemsMap() {
        return items_map;
    }

    public CoffeeSiteListContent(List<CoffeeSiteMovable> coffeeSiteList) {
        this.items = coffeeSiteList;
        for (CoffeeSiteMovable csm : items) {
            items_map.put(String.valueOf(csm.getId()), csm);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(items);
    }
}
