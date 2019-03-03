package cz.fungisoft.coffeecompass.asynctask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import cz.fungisoft.coffeecompass.activity.CoffeeSiteListActivity;
import cz.fungisoft.coffeecompass.activity.MainActivity;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;

/**
 * Class to run AsyncTask to read CoffeeSites in specified distance from coffeecompass.cz server via JSON.
 * Should not be called and run if the internet connection is not available.
 */
public class GetSitesInRangeAsyncTask extends AsyncTask<String, String, String> {

    private static final String TAG = "Read CoffeeSite list ";

    private static final String sURLCore = "http://coffeecompass.cz/rest/site/searchSites/";
    private String sURL;

    private MainActivity parentActivity;

    double latFrom, longFrom;

    private List<CoffeeSite> coffeeSites;
    private String searchCoffeeSort;

    public GetSitesInRangeAsyncTask(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
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
    public GetSitesInRangeAsyncTask(MainActivity parentActivity, String latFrom, String longFrom, String range, String coffeeSort) {
        this(parentActivity);

        this.latFrom = Double.valueOf(latFrom);
        this.longFrom = Double.valueOf(longFrom);

        this.searchCoffeeSort = coffeeSort.isEmpty() ? "?" : coffeeSort;

        // Creates actual REST request for CoffeeSites
        sURL = sURLCore + "?lat1=" + latFrom + "&lon1=" + longFrom + "&range=" + range + "&sort=" + this.searchCoffeeSort;
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
                    BufferedReader reader = new BufferedReader(new
                            InputStreamReader(inpStream, "UTF-8"));

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
    private List<CoffeeSite>  parseJSONWithFoundSitesResult(String sJSON) {

        List<CoffeeSite> retSites = new ArrayList<>();

        if (sJSON != null) {
            JSONArray jsonCoffeeSiteArray;

            try {
                jsonCoffeeSiteArray = new JSONArray(sJSON);

                if (jsonCoffeeSiteArray != null) {
                    for (int i = 0; i < jsonCoffeeSiteArray.length(); i++) {

                        JSONObject csObject = jsonCoffeeSiteArray.getJSONObject(i);
                        CoffeeSite cs = new CoffeeSite(csObject.getInt("id"),
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

        if (coffeeSites.size() > 0) {

            CoffeeSiteListContent content = new CoffeeSiteListContent(coffeeSites);

            Intent csListIntent = new Intent(parentActivity, CoffeeSiteListActivity.class);

            csListIntent.putExtra("listContent", content);
            csListIntent.putExtra("latFrom", this.latFrom);
            csListIntent.putExtra("longFrom", this.longFrom);
            parentActivity.startActivity(csListIntent);

        } else
            parentActivity.showNothingFoundStatus(this.searchCoffeeSort);
    }

}
