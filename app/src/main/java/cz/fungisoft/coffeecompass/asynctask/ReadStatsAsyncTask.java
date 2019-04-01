package cz.fungisoft.coffeecompass.asynctask;

import android.os.AsyncTask;
import android.util.Log;

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

import cz.fungisoft.coffeecompass.activity.MainActivity;
import cz.fungisoft.coffeecompass.entity.Statistics;

/**
 * AsyncTask to read basic statistics from coffeecompass.cz about saved CoffeeSites.
 * The task runs at the start of the MainActivity.
 */
public class ReadStatsAsyncTask extends AsyncTask<String, String, String> {

    private static final String TAG = "Read statistics";

    private static final String bURL = "http://coffeecompass.cz/rest/home";

    private MainActivity parentActivity;

    private Statistics stats;

    public ReadStatsAsyncTask(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Override
    protected String doInBackground(String... strings) {

        String sJSON = null;
        InputStream inpStream = null;

        try {
            URL url = new URL(bURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            inpStream = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(inpStream, "UTF-8"));

            StringBuilder sb = new StringBuilder();
            String radek = null;

            while((radek = reader.readLine()) != null) {
                sb.append(radek + "\n");
            }

            sJSON = sb.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        }
        catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
        catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        finally {
            try { if (inpStream != null) inpStream.close();}
            catch (Exception e) {
                Log.e(TAG, "Error closing input stream: " + e.getMessage());
            }
        }

        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(sJSON);

            stats = new Statistics(jsonObject.getString("numOfAllSites"),
                    jsonObject.getString("numOfNewSitesLast7Days"),
                    jsonObject.getString("numOfNewSitesToday"),
                    jsonObject.getString("numOfAllUsers"));

        } catch (JSONException e) {
            Log.e(TAG, "Parsing JSON Exception: " + e.getMessage());
        }

        return sJSON;
    }

    @Override
    protected void onPostExecute(String result) {
        parentActivity.zobrazStatistiky(stats);
    }
}