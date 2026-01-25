package cz.fungisoft.coffeecompass2.entity;


import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Base class for classes, which instancies are to be persisted into DB.
 */
public abstract class CoffeeSiteEntity {

    @Expose
    @SerializedName("id")
    @PrimaryKey
    @NonNull
    protected String id;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public CoffeeSiteEntity(@NonNull String id) {
        this.id = id;
    }

    public CoffeeSiteEntity() {
        this.id = "";
    }
}
