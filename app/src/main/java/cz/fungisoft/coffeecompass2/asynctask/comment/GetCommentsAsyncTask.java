package cz.fungisoft.coffeecompass2.asynctask.comment;

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

import cz.fungisoft.coffeecompass2.activity.CoffeeSiteDetailActivity;
import cz.fungisoft.coffeecompass2.activity.CommentsListActivity;
import cz.fungisoft.coffeecompass2.entity.CoffeeSite;
import cz.fungisoft.coffeecompass2.entity.Comment;

/**
 * Class to asynchronously load all the comments for the
 * specified CoffeeSite
 */
public class GetCommentsAsyncTask extends AsyncTask<String, String, String>  {

    private static final String TAG = "Read comments async.";

    private static final String sURLCore = "https://coffeecompass.cz/rest/public/starsAndComments/comments/";
    private String sURL;

    private CommentsListActivity parentActivity;

    private List<Comment> comments;

    //private CoffeeSite coffeeSite;
    private int coffeeSiteID;

    //public GetCommentsAsyncTask(CommentsListActivity parentActivity, CoffeeSite cs) {
    public GetCommentsAsyncTask(CommentsListActivity parentActivity, int coffeeSiteID) {
        this.parentActivity = parentActivity;
        //this.coffeeSite = cs;
        this.coffeeSiteID = coffeeSiteID;

        sURL = sURLCore + this.coffeeSiteID;

//        if (this.coffeeSite != null) {
//            sURL = sURLCore + this.coffeeSite.getId();
//        }
    }

    @Override
    protected String doInBackground(String... strings) {

        String sJSON = null;
        InputStream inpStream = null;

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

        comments = parseJSONWithFoundCommentsResult(sJSON);

//        if (this.coffeeSite != null) {
//            this.coffeeSite.setComments(comments);
//        }

        return sJSON;
    }

    private List<Comment> parseJSONWithFoundCommentsResult(String sJSON) {

        List<Comment> retComments = new ArrayList<>();

        if (sJSON != null) {
            JSONArray jsonCommentsArray;

            try {
                jsonCommentsArray = new JSONArray(sJSON);

                if (jsonCommentsArray != null) {
                    for (int i = 0; i < jsonCommentsArray.length(); i++) {

                        JSONObject commentObject = jsonCommentsArray.getJSONObject(i);

                        Integer id = commentObject.getInt("id");
                        String commentText = commentObject.getString("text");
                        String createdOn = commentObject.getString("created");

                        Integer coffeeSiteId = commentObject.getInt("coffeeSiteID");
                        String userName = commentObject.getString("userName");
                        boolean canBeDeleted = commentObject.getBoolean("canBeDeleted");

                        Comment comment = new Comment(id, commentText, createdOn, coffeeSiteId, userName, canBeDeleted);

                        retComments.add(comment);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception during parsing JSON : " + e.getMessage());
            }
        }

        return retComments;
    }

    /**
     * Po dokonceni enable commentButton na CoffeeSiteDetailActivity
     *
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {

        if (comments != null && comments.size() > 0) {
            parentActivity.processComments(comments);
        }
    }

}
