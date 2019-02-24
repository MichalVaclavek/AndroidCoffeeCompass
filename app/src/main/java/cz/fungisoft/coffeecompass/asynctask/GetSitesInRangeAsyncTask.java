package cz.fungisoft.coffeecompass.asynctask;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

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

import cz.fungisoft.coffeecompass.activity.CoffeeSiteListActivity;
import cz.fungisoft.coffeecompass.activity.MainActivity;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteListContent;


public class GetSitesInRangeAsyncTask extends AsyncTask<String, String, String> {

    private static final String TAG = "Read list async.";

//    private static String sURL = "http://coffeecompass.cz/rest/site/getSitesInRange/?lat1=50.1669497&lon1=14.7657927&range=5000";
//    http://coffeecompass.cz/rest/site/searchSites/?lat1=50.1669497&lon1=14.7657927&range=50000&status=V%20provozu&sort=espresso // %20 je mezera v parametru HTTP headeru
//    private static String sURL = "http://coffeecompass.cz/rest/site/getSitesInRange/";

      private static final String sURLCore = "http://coffeecompass.cz/rest/site/searchSites/";
      private String sURL;

    private MainActivity parentActivity;

    double latFrom, longFrom;

    private List<CoffeeSite> coffeeSites;

    public GetSitesInRangeAsyncTask(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public GetSitesInRangeAsyncTask(MainActivity parentActivity, String latFrom, String longFrom, String range, String coffeeSort) {
        this(parentActivity);

        this.latFrom = Double.valueOf(latFrom);
        this.longFrom = Double.valueOf(longFrom);

        String sort = coffeeSort.isEmpty() ? "?" : coffeeSort;

        sURL = sURLCore + "?lat1=" + latFrom + "&lon1=" + longFrom + "&range=" + range + "&sort=" + sort;
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

                        cs.setImageAvailable(csObject.getBoolean("imageAvailable"));

                        cs.setCena(csObject.getJSONObject("cena").getString("priceRange"));
                        cs.setUliceCP(csObject.getString("uliceCP"));
                        cs.setTypPodniku(csObject.getJSONObject("typPodniku").getString("coffeeSiteType"));
                        cs.setTypLokality(csObject.getJSONObject("typLokality").getString("locationType"));
                        cs.setStatusZarizeni(csObject.getJSONObject("statusZarizeni").getString("status"));
                        cs.setHodnoceni(csObject.getJSONObject("averageStarsWithNumOfHodnoceni").getString("common"));
                        cs.setCreatedByUser(csObject.getString("originalUserName"));
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
            parentActivity.showNothingFoundStatus();
    }

}
