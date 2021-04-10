
package cz.fungisoft.coffeecompass2.activity.data.model.rest.places;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Vygenerovano pomoci https://www.jsonschema2pojo.org/
 * z JSON odpovedi ziskane na dotaz:
 * http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/exts/GeocodeSOE/findAddressCandidates?f=json&SingleLine=Pard&maxLocations=9
 */
public class SpatialReference__1 {

    @SerializedName("wkid")
    @Expose
    private Integer wkid;

    @SerializedName("latestWkid")
    @Expose
    private Integer latestWkid;

    public Integer getWkid() {
        return wkid;
    }

    public void setWkid(Integer wkid) {
        this.wkid = wkid;
    }

    public Integer getLatestWkid() {
        return latestWkid;
    }

    public void setLatestWkid(Integer latestWkid) {
        this.latestWkid = latestWkid;
    }

}
