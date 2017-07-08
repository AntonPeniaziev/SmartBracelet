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

import activities.TentActivity;


public class CallEvacuationTask extends AsyncTask<String, Integer, Boolean> {

    private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
    private static final String collectionName = "soldiers";
    private static final String idTitle = "Bracelet_ID";
    private static final String evacTitle = "evacuation_request";

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
        MongoCollection<BasicDBObject> dbCollection = db.getCollection(collectionName, BasicDBObject.class);

        boolean result;
        result = updateEvac(dbCollection, params[1], params[0]);

        return result;
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
        TentActivity.updateEvacFromWeb = true;
    }

    /**
     * updates the status of the evacuation for a given patient BTmac
     * @param collection the DB collection where to update
     * @param id BT mac of the patient
     * @param status the new evacuation status
     * @return boolean for success
     */
    protected Boolean updateEvac(MongoCollection<BasicDBObject> collection, String id, String status) {
        try {
            Bson searchQuery = new Document(idTitle, id);
            Bson newValue = new BasicDBObject().append(evacTitle, status);
            Bson updateOperationDocument = new BasicDBObject().append("$set", newValue);
            collection.updateOne(searchQuery, updateOperationDocument);
            PostToWeb.postToWeb();
            return true;
        } catch (MongoTimeoutException | MongoSocketReadException | MongoSocketOpenException | MongoSecurityException e) {
            e.printStackTrace();
        }
        return false;
    }
}
