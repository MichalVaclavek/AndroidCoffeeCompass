package cz.fungisoft.coffeecompass2.asynctask;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.CoffeeSiteListActivity;
import cz.fungisoft.coffeecompass2.activity.MainActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteListContent;
import cz.fungisoft.coffeecompass2.entity.CoffeeSiteMovable;
import cz.fungisoft.coffeecompass2.services.CoffeeSitesInRangeUpdateService;

/**
 * Class to run AsyncTask to read CoffeeSites in specified distance from coffeecompass.cz server via JSON.
 * Should not be called and run if the internet connection is not available.
 */
public class GetSitesInRangeAsyncTask extends AsyncTask<String, String, String> {

    private static final String TAG = "Read CoffeeSite list";

    // TODO vlozit do strings resources String url = getResources().getString(R.string.json_get_url);
    private static final String sURLCore = "https://coffeecompass.cz/rest/site/searchSites/";
    private String sURL;

    /**
     * An Activity which invokes this async. task
     */
    private MainActivity parentActivity;

    /**
     * A Service which invokes this asznc tsak
     */
    private CoffeeSitesInRangeUpdateService parentService;

    private double latFrom, longFrom;

    int searchRange = 500;

    private List<CoffeeSiteMovable> coffeeSites;
    private String searchCoffeeSort;

    public GetSitesInRangeAsyncTask(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public GetSitesInRangeAsyncTask(CoffeeSitesInRangeUpdateService service) {
        this.parentService = service;
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

                        cs.setCena(csObject.getJSONObject("cena").getString("priceRange"));
                        cs.setUliceCP(csObject.getString("uliceCP"));
                        cs.setTypPodniku(csObject.getJSONObject("typPodniku").getString("coffeeSiteType"));
                        cs.setTypLokality(csObject.getJSONObject("typLokality").getString("locationType"));
                        cs.setStatusZarizeni(csObject.getJSONObject("statusZarizeni").getString("status"));
                        cs.setHodnoceni(csObject.getJSONObject("averageStarsWithNumOfHodnoceni").getString("common"));
                        cs.setCreatedByUser(csObject.getString("originalUserName"));
                        cs.setCreatedOnString(csObject.getString("createdOn"));

                        cs.setUvodniKoment(csObject.getString("initialComment"));

                        cs.setOteviraciDobaDny(csObject.getString("pristupnostDny"));
                        cs.setOteviraciDobaHod(csObject.getString("pristupnostHod"));

                        JSONArray jsonCupTypesArray = csObject.getJSONArray("cupTypes");
                        StringBuilder cupTypes = new StringBuilder();
                        for (int n = 0; n < jsonCupTypesArray .length(); n++) {
                            JSONObject sortObject = jsonCupTypesArray.getJSONObject(n);
                            cupTypes.append(sortObject.getString("cupType") + ", ");
                        }
                        cs.setCupTypes(cupTypes.toString());

                        JSONArray jsonNextToMachineTypesArray = csObject.getJSONArray("nextToMachineTypes");
                        StringBuilder ntmTypes = new StringBuilder();
                        for (int m = 0; m < jsonNextToMachineTypesArray.length(); m++) {
                            JSONObject sortObject = jsonNextToMachineTypesArray.getJSONObject(m);
                            ntmTypes.append(sortObject.getString("type") + ", ");
                        }
                        cs.setNextToMachineTypes(ntmTypes.toString());

                        JSONArray jsonCoffeeSortsArray = csObject.getJSONArray("coffeeSorts");
                        StringBuilder sorts = new StringBuilder();
                        for (int j = 0; j < jsonCoffeeSortsArray.length(); j++) {
                            JSONObject sortObject = jsonCoffeeSortsArray.getJSONObject(j);
                            sorts.append(sortObject.getString("coffeeSort") + ", ");
                        }
                        cs.setCoffeeSorts(sorts.toString());

                        JSONArray jsonOtherOffersArray = csObject.getJSONArray("otherOffers");
                        StringBuilder offers = new StringBuilder();
                        for (int k = 0; k < jsonOtherOffersArray.length(); k++) {
                            JSONObject offerObject = jsonOtherOffersArray.getJSONObject(k);
                            offers.append(offerObject.getString("offer") + ", ");
                        }
                        cs.setOtherOffers(offers.toString());

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
     * If some CoffeeSites are returned from server, go to CoffeeSiteListActivity,
     * which shows the basic info about CoffeeSites in a list.
     * If no CoffeeSite found, then go back to parent activity (MainActivity)
     *
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {

        if (parentActivity != null) {
            CoffeeSiteListContent content = new CoffeeSiteListContent(coffeeSites);

            Intent csListIntent = new Intent(parentActivity, CoffeeSiteListActivity.class);

            csListIntent.putExtra("listContent", (Parcelable) content);
            csListIntent.putExtra("latLongFrom", new LatLng(this.latFrom, this.longFrom));
            csListIntent.putExtra("searchRange", this.searchRange);
            csListIntent.putExtra("coffeeSort", this.searchCoffeeSort);

            parentActivity.startActivity(csListIntent);
        }

        if (parentService != null) {
            parentService.onSitesInRangeReturnedFromServer(coffeeSites);
        }
    }

}
