package cz.fungisoft.coffeecompass2.asynctask.coffeesite;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.BuildConfig;
import cz.fungisoft.coffeecompass2.activity.ui.coffeesite.FoundCoffeeSitesListActivity;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.entity.AverageStarsWithNumOfHodnoceni;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovableListContent;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass2.entity.CoffeeSort;
import cz.fungisoft.coffeecompass2.entity.CupType;
import cz.fungisoft.coffeecompass2.entity.NextToMachineType;
import cz.fungisoft.coffeecompass2.entity.OtherOffer;
import cz.fungisoft.coffeecompass2.entity.PriceRange;
import cz.fungisoft.coffeecompass2.entity.SiteLocationType;
import cz.fungisoft.coffeecompass2.entity.repository.CoffeeSiteEntitiesFactory;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateService;

/**
 * Class to run AsyncTask to read CoffeeSites in specified distance from coffeecompass.cz server via JSON.
 * Should not be called and run if the internet connection is not available.
 */
public class GetSitesInRangeAsyncTask extends AsyncTask<String, String, String> {

    private static final String TAG = "GetCoffeeSites range";

    private static final String sURLCore = BuildConfig.COFFEESITE_API_PUBLIC_SEARCH_URL;
    private String sURL;

    /**
     * An Activity which invokes this async. task
     */
    private WeakReference<MainActivity> parentActivity;

    /**
     * A Service which invokes this asznc tsak
     */
    private WeakReference<CoffeeSitesInRangeUpdateService> parentService;

    private double latFrom, longFrom;

    int searchRange = 500;

    private List<CoffeeSiteMovable> coffeeSites;
    private String searchCoffeeSort;

    private GetSitesInRangeAsyncTask(MainActivity parentActivity) {
        this.parentActivity = new WeakReference<>(parentActivity);
    }

    private GetSitesInRangeAsyncTask(CoffeeSitesInRangeUpdateService service) {
        this.parentService = new WeakReference<>(service);
    }


    /**
     * Basic Constructor
     *
     * @param parentActivity - what activity created and run this task
     * @param latFrom - latitude of searching from point
     * @param longFrom - longitude of searching from point
     * @param range - range of meters from searching point
     * @param coffeeSort - coffee sort (espresso, instant and so on) as filter for searched Coffee sites
     */
    public GetSitesInRangeAsyncTask(MainActivity parentActivity, Double latFrom, Double longFrom, Integer range, String coffeeSort) {
        this(parentActivity);
        initSearchParameters(latFrom, longFrom, range, coffeeSort);
    }

    public GetSitesInRangeAsyncTask(CoffeeSitesInRangeUpdateService parentService, Double latFrom, Double longFrom, Integer range, String coffeeSort) {
        this(parentService);
        initSearchParameters(latFrom, longFrom, range, coffeeSort);
    }

    /**
     * Method to perform initialization of search parameters before REST request is sent to
     * server.
     */
    private void initSearchParameters(Double latFrom, Double longFrom, Integer range, String coffeeSort) {
        this.latFrom = latFrom;
        this.longFrom = longFrom;
        this.searchRange = range;

        this.searchCoffeeSort = coffeeSort.isEmpty() ? "?" : coffeeSort;

        // Creates actual REST request for CoffeeSites
        sURL = sURLCore + "?lat1=" + latFrom + "&lon1=" + longFrom + "&range=" + this.searchRange + "&sort=" + this.searchCoffeeSort;
    }

    @Override
    protected String doInBackground(String... strings) {

        String sJSON = null;
        InputStream inpStream = null;

        //TODO - ověření, že je k dispozici připojení k internetu ??

        try {
            URL url = new URL(sURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            Log.i(TAG, "URL=" + sURL);
            conn.connect();

            int statusCode = conn.getResponseCode();

            if (statusCode == 200) {

                inpStream = new BufferedInputStream(conn.getInputStream());

                if (inpStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inpStream, "UTF-8"));

                    StringBuilder sb = new StringBuilder();
                    String radek = null;

                    while ((radek = reader.readLine()) != null) {
                        sb.append(radek + "\n");
                    }

                    sJSON = sb.toString();
                }
            } else {
                if (statusCode >= 400) { // resource not found, napr. 404, kdy nebyl nalezen prislusny Controller na serveru
                    Log.e(TAG, "Server error, status code 40X.");
                }
                if (statusCode >= 500) { // internal server error
                    Log.e(TAG, "Server error, status code 50X.");
                }
                // je mozno precist a analyzovat error stream - conn.getErrorStream();
            }
        } catch(MalformedURLException e){
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch(ProtocolException e){
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        }
        catch(IOException e){
            Log.e(TAG, "IOException: " + e.getMessage());
        }
        catch(Exception e){
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        finally{
            try {
                if (inpStream != null) inpStream.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing input stream: " + e.getMessage());
            }
        }

        coffeeSites = parseJSONWithFoundSitesResult(sJSON);

        return sJSON;
    }

    /**
     * Private method to process JSON string returned from server.
     * Finds expected list of found CoffeeSites
     *
     * @param sJSON JSON string returned from server containing list of CoffeeSites
     * @return list of CoffeeSites parsed from sJSON string returned from server
     */
    private List<CoffeeSiteMovable>  parseJSONWithFoundSitesResult(String sJSON) {

        List<CoffeeSiteMovable> retSites = new ArrayList<>();

        if (sJSON != null) {
            JSONArray jsonCoffeeSiteArray;

            try {
                jsonCoffeeSiteArray = new JSONArray(sJSON);

                if (jsonCoffeeSiteArray != null) {
                    for (int i = 0; i < jsonCoffeeSiteArray.length(); i++) {

                        JSONObject csObject = jsonCoffeeSiteArray.getJSONObject(i);
                        CoffeeSiteMovable cs = new CoffeeSiteMovable(csObject.getInt("id"),
                                                        csObject.getString("siteName"),
                                                        csObject.getLong("distFromSearchPoint"));

                        cs.setLatitude(csObject.getDouble("zemSirka"));
                        cs.setLongitude(csObject.getDouble("zemDelka"));

                        cs.setMainImageURL(csObject.getString("mainImageURL"));

                        cs.setCena((PriceRange) CoffeeSiteEntitiesFactory.getEntity("priceRange", csObject.getJSONObject("cena")));

                        cs.setUliceCP(csObject.getString("uliceCP"));
                        cs.setMesto(csObject.getString("mesto"));
                        cs.setTypPodniku((CoffeeSiteType) CoffeeSiteEntitiesFactory.getEntity("CoffeeSiteType", csObject.getJSONObject("typPodniku")));
                        cs.setTypLokality((SiteLocationType) CoffeeSiteEntitiesFactory.getEntity("SiteLocationType", csObject.getJSONObject("typLokality")));
                        cs.setStatusZarizeni((CoffeeSiteStatus) CoffeeSiteEntitiesFactory.getEntity("CoffeeSiteStatus", csObject.getJSONObject("statusZarizeni")));
                        //cs.setHodnoceni(csObject.getJSONObject("averageStarsWithNumOfHodnoceni").getString("common"));
                        AverageStarsWithNumOfHodnoceni hodnoceni = new AverageStarsWithNumOfHodnoceni(
                                csObject.getJSONObject("averageStarsWithNumOfHodnoceni").getInt("avgStars"),
                                csObject.getJSONObject("averageStarsWithNumOfHodnoceni").getInt("numOfHodnoceni"),
                                csObject.getJSONObject("averageStarsWithNumOfHodnoceni").getString("common")
                        );
                        cs.setHodnoceni(hodnoceni);
                        cs.setCreatedByUserName(csObject.getString("originalUserName"));
                        cs.setCreatedOnString(csObject.getString("createdOn"));

                        cs.setUvodniKoment(csObject.getString("initialComment"));

                        cs.setOteviraciDobaDny(csObject.getString("pristupnostDny"));
                        cs.setOteviraciDobaHod(csObject.getString("pristupnostHod"));

                        JSONArray jsonCupTypesArray = csObject.getJSONArray("cupTypes");
                        List<CupType> cupTypes = new ArrayList<>();
                        for (int n = 0; n < jsonCupTypesArray.length(); n++) {
                            JSONObject cupJsonObject = jsonCupTypesArray.getJSONObject(n);
                            CupType cupType = (CupType) CoffeeSiteEntitiesFactory.getEntity("CupType", cupJsonObject);
                            cupTypes.add(cupType);
                        }
                        cs.setCupTypes(cupTypes);

                        JSONArray jsonNextToMachineTypesArray = csObject.getJSONArray("nextToMachineTypes");
                        List<NextToMachineType> ntmTypes = new ArrayList<>();
                        for (int m = 0; m < jsonNextToMachineTypesArray.length(); m++) {
                            JSONObject ntmtJsonObject = jsonNextToMachineTypesArray.getJSONObject(m);
                            NextToMachineType ntmt = (NextToMachineType) CoffeeSiteEntitiesFactory.getEntity("NextToMachineType", ntmtJsonObject);
                            ntmTypes.add(ntmt);
                        }
                        cs.setNextToMachineTypes(ntmTypes);

                        JSONArray jsonCoffeeSortsArray = csObject.getJSONArray("coffeeSorts");
                        List<CoffeeSort> coffeeSorts = new ArrayList<>();
                        for (int j = 0; j < jsonCoffeeSortsArray.length(); j++) {
                            JSONObject sortObject = jsonCoffeeSortsArray.getJSONObject(j);
                            CoffeeSort coffeeSort = (CoffeeSort) CoffeeSiteEntitiesFactory.getEntity("CoffeeSort", sortObject);
                            coffeeSorts.add(coffeeSort );
                        }
                        cs.setCoffeeSorts(coffeeSorts);

                        JSONArray jsonOtherOffersArray = csObject.getJSONArray("otherOffers");
                        List<OtherOffer> otherOffers = new ArrayList<>();
                        for (int k = 0; k < jsonOtherOffersArray.length(); k++) {
                            JSONObject otherOfferJsonObject = jsonOtherOffersArray.getJSONObject(k);
                            OtherOffer otherOffer = (OtherOffer) CoffeeSiteEntitiesFactory.getEntity("OtherOffer", otherOfferJsonObject);
                            otherOffers.add(otherOffer);
                        }
                        cs.setOtherOffers(otherOffers);

                        retSites.add(cs);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception during parsing JSON : " + e.getMessage());
            }
        }

        return retSites;
    }

    /**
     * If some CoffeeSites are returned from server, go to FoundCoffeeSitesListActivity,
     * which shows the basic info about CoffeeSites in a list.
     * If no CoffeeSite found, then go back to parent activity (MainActivity)
     *
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {

        if (parentActivity != null && parentActivity.get() != null) {
            CoffeeSiteMovableListContent content = new CoffeeSiteMovableListContent(coffeeSites);


            Intent csListIntent = new Intent(parentActivity.get(), FoundCoffeeSitesListActivity.class);

            csListIntent.putExtra("listContent", (Parcelable) content);
            csListIntent.putExtra("latLongFrom", new LatLng(this.latFrom, this.longFrom));
            csListIntent.putExtra("searchRange", this.searchRange);
            csListIntent.putExtra("coffeeSort", this.searchCoffeeSort);

            parentActivity.get().startActivity(csListIntent);
        }

        if (parentService != null && parentService.get() != null) {
            parentService.get().onSitesInRangeReturnedFromServer(coffeeSites);
        }
    }

}
