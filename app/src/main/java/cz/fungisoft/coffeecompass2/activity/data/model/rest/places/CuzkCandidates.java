
package cz.fungisoft.coffeecompass2.activity.data.model.rest.places;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Hlavni trida z odpovedi na dotaz CUZK (Cesky urad zememericsky a katastralni),
 * ktery poskytuje 'Places API' z jejich databazi.
 *
 * Vygenerovano pomoci https://www.jsonschema2pojo.org/
 * z JSON odpovedi ziskane na dotaz:
 * http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/exts/GeocodeSOE/findAddressCandidates?f=json&SingleLine=Pard&maxLocations=9
 */
public class CuzkCandidates {

    @SerializedName("spatialReference")
    @Expose
    private SpatialReference spatialReference;

    @SerializedName("candidates")
    @Expose
    private List<Candidate> candidates = null; // hlavni seznam 'kandidatu' s adresami

    public SpatialReference getSpatialReference() {
        return spatialReference;
    }

    public void setSpatialReference(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

}
