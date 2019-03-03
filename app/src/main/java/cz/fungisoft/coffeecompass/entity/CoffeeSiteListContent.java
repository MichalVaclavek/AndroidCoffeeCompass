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
    private List<CoffeeSite> items = new ArrayList<CoffeeSite>();

    public List<CoffeeSite> getItems() {
        return items;
    }

    /**
     * A map of CoffeeSite items, by ID. Used by RecyclerView in the CoffeeSiteListActivity
     */
    private Map<String, CoffeeSite> items_map = new HashMap<String, CoffeeSite>();

    public Map<String, CoffeeSite> getItemsMap() {
        return items_map;
    }

    public CoffeeSiteListContent(List<CoffeeSite> coffeeSiteList) {
        this.items = coffeeSiteList;
        for (CoffeeSite cs : items) {
            items_map.put(String.valueOf(cs.getId()), cs);
        }
    }

}
