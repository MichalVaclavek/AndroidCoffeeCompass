package cz.fungisoft.coffeecompass2.entity;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

//    protected String entityValue;
//
//    public abstract String getEntityValue();
//
//    public abstract void setEntityValue(String entityValue);

    public CoffeeSiteEntity(int id) {
        this.id = id;
        //this.entityValue = entityValue;
    }

    public CoffeeSiteEntity() {
        this.id = 0;
        //this.entityValue = "";
    }
}
