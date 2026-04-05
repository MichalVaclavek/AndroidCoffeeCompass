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
public class CoffeeSiteListContent implements Parcelable {

    /**
     * An array of CoffeeSite items.
     */
    private List<CoffeeSite> items = new ArrayList<>();

    protected CoffeeSiteListContent(Parcel in) {
        items = in.createTypedArrayList(CoffeeSite.CREATOR);
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

    public List<CoffeeSite> getItems() {
        return items;
    }

    public void add(CoffeeSite cs) {
        items.add(cs);
        items_map.put(String.valueOf(cs.getId()), cs);
    }

    public void setItems(List<CoffeeSite> coffeeSiteList) {
        this.items = coffeeSiteList;
        for (CoffeeSite cs : items) {
            items_map.put(String.valueOf(cs.getId()), cs);
        }
    }

    /**
     * A map of CoffeeSite items, by ID. Used by RecyclerView in the FoundCoffeeSitesListActivity
     */
    private final Map<String, CoffeeSite> items_map = new HashMap<>();

    public Map<String, CoffeeSite> getItemsMap() {
        return items_map;
    }

    public CoffeeSiteListContent() {
    }

    public CoffeeSiteListContent(List<CoffeeSite> coffeeSiteList) {
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
