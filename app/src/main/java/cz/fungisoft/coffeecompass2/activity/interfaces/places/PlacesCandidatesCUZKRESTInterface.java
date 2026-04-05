package cz.fungisoft.coffeecompass2.activity.interfaces.places;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.activity.data.model.rest.places.CuzkCandidates;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit calls interface for CUZK Places REST API calls
 */
public interface PlacesCandidatesCUZKRESTInterface {

    String CUZK_PLACES_API_SEARCH_URL = BuildConfig.CUZK_PLACES_API_SEARCH_URL;

    /**
     * REST call for obtaining places Candidates from CUZK API.
     *
     * URL example http://ags.cuzk.cz/arcgis/rest/services/RUIAN/Vyhledavaci_sluzba_nad_daty_RUIAN/MapServer/exts/GeocodeSOE/findAddressCandidates?f=json&SingleLine=Pard&maxLocations=9
     *
     * @param placeName name we want to get place candidates for
     * @param maxLocations max. number of Candidates to be returned
     *
     * @return list of 'Candidate' i.e. places matching given 'placeName'
     */
    @GET("findAddressCandidates?f=json")
    Call<CuzkCandidates> getPlacesCandidates(@Query("SingleLine") String placeName, @Query("maxLocations") int maxLocations);

}
