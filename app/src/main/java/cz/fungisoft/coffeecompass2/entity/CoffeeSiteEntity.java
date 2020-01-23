package cz.fungisoft.coffeecompass2.entity;

import android.os.Parcelable;

public abstract class CoffeeSiteEntity {

    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    protected String entityValue;

    public String getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(String entityValue) {
        this.entityValue = entityValue;
    }

    public CoffeeSiteEntity(int id, String entityValue) {
        this.id = id;
        this.entityValue = entityValue;
    }

    public CoffeeSiteEntity() {
        this.id = 0;
        this.entityValue = "";
    }
}
