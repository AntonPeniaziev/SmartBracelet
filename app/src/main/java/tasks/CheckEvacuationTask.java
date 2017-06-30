package tasks;

import android.content.Context;
import android.os.AsyncTask;

import ArduinoParsingUtils.ArduinoParsingUtils;
import activities.TentActivity;
import logic.Bracelet;
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


public class CheckEvacuationTask extends AsyncTask<Tent, Integer, Boolean> {

    private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
    private static final String collectionName = "soldiers";
    private static final String idTitle = "Bracelet_ID";
    private static final String evacTitle = "evacuation_request";

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
        MongoCollection<BasicDBObject> dbCollection = db.getCollection(collectionName, BasicDBObject.class);

        FindIterable<BasicDBObject> soldiers = dbCollection.find();

        boolean result = checkAndUpdate(soldiers, params[0]);

        return result;
    }

    /**
     * checks in the given collation if any local soldier has a new evacuation status
     * @param soldiers the collection where to check
     * @param tent our local patients data structure
     * @return boolean for success
     */
    protected Boolean checkAndUpdate(FindIterable<BasicDBObject> soldiers, Tent tent) {
        try{
            for (BasicDBObject soldier : soldiers) {
                Object mac = soldier.get(idTitle);
                Object evac = soldier.get(evacTitle);
                if (mac == null || evac == null)
                    continue;
                if (tent.isContain(mac.toString())){
                    if (evac.toString().equals("true") && !tent.getUrgantEvacuation(mac.toString())){
                        tent.setUrgantEvacuation(mac.toString(), true);
                        TentActivity.sendRecordToBracelet(mac.toString(),  ArduinoParsingUtils.EVAC_SENT_RECORD);
                    } else if (evac.toString().equals("false") && tent.getUrgantEvacuation(mac.toString())) {
                        tent.setUrgantEvacuation(mac.toString(), false);
                        TentActivity.sendRecordToBracelet(mac.toString(),  ArduinoParsingUtils.EVAC_SENT_RECORD);
                    }
                }
                if (isCancelled())
                    return true;
            }
            return true;
        } catch (MongoTimeoutException | MongoSocketReadException | MongoSocketOpenException | MongoSecurityException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {}

    @Override
    protected void onCancelled(){}
}
