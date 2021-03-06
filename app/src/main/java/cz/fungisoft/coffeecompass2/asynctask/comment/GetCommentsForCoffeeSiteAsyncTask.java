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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass2.activity.ui.comments.CommentsListActivity;
import cz.fungisoft.coffeecompass2.entity.Comment;

/**
 * Class to asynchronously load all the comments for the
 * specified CoffeeSite
 */
public class GetCommentsForCoffeeSiteAsyncTask extends AsyncTask<String, String, String>  {

    private static final String TAG = "Read comments async.";

    private static final String sURLCore = "https://coffeecompass.cz/rest/public/starsAndComments/comments/";
    private String sURL;

    private WeakReference<CommentsListActivity> parentActivity;

    private List<Comment> comments;

    private int coffeeSiteID;

    public GetCommentsForCoffeeSiteAsyncTask(CommentsListActivity parentActivity, int coffeeSiteID) {
        this.parentActivity = new WeakReference<>(parentActivity);
        this.coffeeSiteID = coffeeSiteID;

        sURL = sURLCore + this.coffeeSiteID;
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
                        int userId = commentObject.getInt("userId");
                        int starsFromUserForCS = commentObject.getInt("starsFromUser");
                        boolean canBeDeleted = commentObject.getBoolean("canBeDeleted");

                        Comment comment = new Comment(id, commentText, createdOn, coffeeSiteId, userName, userId, canBeDeleted, starsFromUserForCS);

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

        if (comments != null) {
            if (parentActivity.get() != null) {
                parentActivity.get().processComments(comments);
            }
        }
    }

}
