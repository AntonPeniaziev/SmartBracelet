package tasks;

import android.content.Context;
import android.os.AsyncTask;

import logic.Tent;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Created by user on 13/06/2017.
 */

public class CheckEvacuationTask extends AsyncTask<Tent, Integer, Boolean> {

    private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";

    private Context mContext;
    public CheckEvacuationTask(Context context) {
        mContext = context;
    }

    /**
     * check if for any of the connected patients, evacuation was called from the web
     * @param params is the Tent of the local patients
     */
    @Override
    protected Boolean doInBackground(Tent... params) {
        MongoClientURI mongoUri = new MongoClientURI(DBAdress);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

        FindIterable<BasicDBObject> soldiers = dbCollection.find();

        try{
            for (BasicDBObject soldier : soldiers) {
                Object mac = soldier.get("bracelet_id");
                Object evac = soldier.get("evacuation_request");
                if (mac == null || evac == null)
                    continue;
                if (params[0].isContain(mac.toString())){
                    if (evac.toString().equals("true") && !params[0].getUrgantEvacuation(mac.toString())){
                        params[0].setUrgantEvacuation(mac.toString(), true);
                    }
                }
                if (isCancelled())
                    return true;
            }
            //return true;
        } catch (MongoTimeoutException e) {
            e.printStackTrace();
            return false;
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
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {}

    @Override
    protected void onCancelled(){}
}
