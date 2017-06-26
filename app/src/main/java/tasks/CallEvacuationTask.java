package tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class CallEvacuationTask extends AsyncTask<String, Integer, Boolean> {

    private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
    private static final String webUrl = "https://firstaidbracelet.herokuapp.com/soldiersChange";

    private Context mContext;
    public CallEvacuationTask(Context context) {
        mContext = context;
    }

/**
 * AsyncTask to notify the web to update evacuation for specific patient
 * @param params is an array contains bracelet_id and a boolean string
 */
    @Override
    protected Boolean doInBackground(String... params) {
        MongoClientURI mongoUri = new MongoClientURI(DBAdress);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

        try {
            Bson searchQuery = new Document("bracelet_id", params[1]);
            Bson newValue = new BasicDBObject().append("evacuation_request", params[0]);
            Bson updateOperationDocument = new BasicDBObject().append("$set", newValue);
            dbCollection.updateOne(searchQuery, updateOperationDocument);
            postToWeb();
            return true;
        } catch (MongoTimeoutException e) {
            e.printStackTrace();
        } catch (MongoSocketReadException e) {
            e.printStackTrace();
            return false;
        } catch (MongoSocketOpenException e) {
            e.printStackTrace();
            return false;
        } catch (MongoSecurityException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * if update to web doesn't succeed, it alerts the user for connection problems
     * @param aBoolean the result of the update
     */
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        int time = 3;
        if (!aBoolean) {
            while (time > 0) {
                Toast.makeText(mContext, "Connection is lost! check your INTERNET and try again", Toast.LENGTH_LONG).show();
                time--;
            }
        }
    }

    /**
     * doing http POST to refresh the website for out changes
     */
    protected void postToWeb() {
        HttpURLConnection client = null;
        try {
            // Defined URL  where to send data
            URL url = new URL(webUrl);
            client = (HttpURLConnection) url.openConnection();

            String msg = "dbUpdate";
            client.setRequestProperty("Accept","text/html;charset=utf-8");
            client.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            client.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
            client.setRequestMethod("POST");
            client.setDoOutput(true);

            DataOutputStream printout = new DataOutputStream(client.getOutputStream());
            printout.writeBytes(msg);
            printout.flush();
            printout.close();

            String responseMsg = client.getResponseMessage();
            int responseCode = client.getResponseCode();

            //Log.e("Post: ", "response is " + Integer.toString(responseCode) + " " + responseMsg);
        }
        catch(Exception ex) {

        } finally {
            try {
                if(client != null) // Make sure the connection is not null.
                    client.disconnect();
            }
            catch(Exception ex) {}
        }
    }
}
