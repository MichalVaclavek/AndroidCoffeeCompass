package cz.fungisoft.coffeecompass2.entity;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Base class for classes, which instancies are to be persisted into DB.
 */
public abstract class CoffeeSiteEntity {

    @Expose
    @SerializedName("id")
    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CoffeeSiteEntity(int id) {
        this.id = id;
    }

    public CoffeeSiteEntity() {
        this.id = 0;
    }
}
