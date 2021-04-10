
package cz.fungisoft.coffeecompass2.activity.data.model.rest.places;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Obsahuje hlavni data, ktere nas zajimaji pro nase pouziti.
 *
 * Vygenerovano pomoci https://www.jsonschema2pojo.org/
 * z JSON odpovedi ziskane na dotaz:
 * http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/exts/GeocodeSOE/findAddressCandidates?f=json&SingleLine=Pard&maxLocations=9
 */
public class Candidate {

    @SerializedName("address")
    @Expose
    private String address; // hlani zajimavy atribut, obshauje znaky vlozene v dotazu v parametru SingleLine viz vyse

    @SerializedName("location")
    @Expose
    private Location location;

    @SerializedName("score")
    @Expose
    private Integer score;

    @SerializedName("attributes")
    @Expose
    private Attributes attributes;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

}
