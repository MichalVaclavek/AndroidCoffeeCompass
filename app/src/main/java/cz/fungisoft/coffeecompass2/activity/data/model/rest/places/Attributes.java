
package cz.fungisoft.coffeecompass2.activity.data.model.rest.places;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Vygenerovano pomoci https://www.jsonschema2pojo.org/
 * z JSON odpovedi ziskane na dotaz:
 * http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/exts/GeocodeSOE/findAddressCandidates?f=json&SingleLine=Pard&maxLocations=9
 */
public class Attributes {

    @SerializedName("Addr_type")
    @Expose
    private String addrType;

    @SerializedName("Loc_name")
    @Expose
    private String locName;

    @SerializedName("Type")
    @Expose
    private String type;

    @SerializedName("City")
    @Expose
    private String city;

    @SerializedName("Country")
    @Expose
    private String country;

    @SerializedName("Xmin")
    @Expose
    private Double xmin;

    @SerializedName("Xmax")
    @Expose
    private Double xmax;

    @SerializedName("Ymin")
    @Expose
    private Double ymin;

    @SerializedName("Ymax")
    @Expose
    private Double ymax;

    @SerializedName("Match_addr")
    @Expose
    private String matchAddr;

    @SerializedName("Score")
    @Expose
    private Integer score;

    public String getAddrType() {
        return addrType;
    }

    public void setAddrType(String addrType) {
        this.addrType = addrType;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getXmin() {
        return xmin;
    }

    public void setXmin(Double xmin) {
        this.xmin = xmin;
    }

    public Double getXmax() {
        return xmax;
    }

    public void setXmax(Double xmax) {
        this.xmax = xmax;
    }

    public Double getYmin() {
        return ymin;
    }

    public void setYmin(Double ymin) {
        this.ymin = ymin;
    }

    public Double getYmax() {
        return ymax;
    }

    public void setYmax(Double ymax) {
        this.ymax = ymax;
    }

    public String getMatchAddr() {
        return matchAddr;
    }

    public void setMatchAddr(String matchAddr) {
        this.matchAddr = matchAddr;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

}
