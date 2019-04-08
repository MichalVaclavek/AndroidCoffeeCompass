package cz.fungisoft.coffeecompass.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List of CoffeeSites to be displayed in the list
 */
public class CoffeeSiteListContent implements Serializable {

    /**
     * An array of CoffeeSite items.
     */
    private List<CoffeeSiteMovable> items = new ArrayList<>();

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

}
