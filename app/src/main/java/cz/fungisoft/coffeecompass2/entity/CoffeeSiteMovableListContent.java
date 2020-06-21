package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List of CoffeeSites to be displayed in the list
 */
public class CoffeeSiteMovableListContent implements Parcelable {

    /**
     * An array of CoffeeSite items.
     */
    private List<CoffeeSiteMovable> items = new ArrayList<>();

    protected CoffeeSiteMovableListContent(Parcel in) {
        items = in.createTypedArrayList(CoffeeSiteMovable.CREATOR);
    }

    public static final Creator<CoffeeSiteMovableListContent> CREATOR = new Creator<CoffeeSiteMovableListContent>() {
        @Override
        public CoffeeSiteMovableListContent createFromParcel(Parcel in) {
            return new CoffeeSiteMovableListContent(in);
        }

        @Override
        public CoffeeSiteMovableListContent[] newArray(int size) {
            return new CoffeeSiteMovableListContent[size];
        }
    };

    public List<CoffeeSiteMovable> getItems() {
        return items;
    }

    public void setItems(List<CoffeeSiteMovable> coffeeSiteList) {
        this.items = coffeeSiteList;
        for (CoffeeSiteMovable csm : items) {
            items_map.put(String.valueOf(csm.getId()), csm);
        }
    }

    /**
     * A map of CoffeeSite items, by ID. Used by RecyclerView in the FoundCoffeeSitesListActivity
     */
    private Map<String, CoffeeSiteMovable> items_map = new HashMap<>();

    public Map<String, CoffeeSiteMovable> getItemsMap() {
        return items_map;
    }

    public CoffeeSiteMovableListContent() {
    }

    public CoffeeSiteMovableListContent(List<CoffeeSiteMovable> coffeeSiteList) {
        setItems(coffeeSiteList);
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
