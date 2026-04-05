
package cz.fungisoft.coffeecompass2.activity.data.model.rest.places;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Vygenerovano pomoci https://www.jsonschema2pojo.org/
 *
 * z JSON odpovedi ziskane na dotaz:
 * http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/exts/GeocodeSOE/findAddressCandidates?f=json&SingleLine=Pard&maxLocations=9
 */
public class Location {

    @SerializedName("x")
    @Expose
    private Double x;

    @SerializedName("y")
    @Expose
    private Double y;

    @SerializedName("spatialReference")
    @Expose
    private SpatialReference__1 spatialReference;

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public SpatialReference__1 getSpatialReference() {
        return spatialReference;
    }

    public void setSpatialReference(SpatialReference__1 spatialReference) {
        this.spatialReference = spatialReference;
    }

}
