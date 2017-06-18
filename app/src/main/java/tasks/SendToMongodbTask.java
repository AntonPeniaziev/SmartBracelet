package tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import logic.Patient;
import logic.Treatment;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import activities.LoginActivity;


public class SendToMongodbTask extends AsyncTask<ArrayList<Patient>, Integer, Boolean> {

    static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
    static final String myUrl = "https://firstaidbracelet.herokuapp.com/soldiersChange";

    private Context mContext;
    public SendToMongodbTask(Context context) {
        mContext = context;
    }
    @Override

    /**
     * update the DB for changes in local patients
     * @param patients is the list of patients to update
     */
    protected Boolean doInBackground(ArrayList<Patient> ... patients) {

        MongoClientURI mongoUri = new MongoClientURI(DBAdress);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

        for (Patient patient : patients[0]) {
            BasicDBObject document = new BasicDBObject();
            ArrayList<BasicDBObject> treatList = new ArrayList<>();

            for (Treatment obj : patient.getTreatmentsArray()) {
                BasicDBObject treatDoc = new BasicDBObject();
                treatDoc.put("Uid", obj.getName());
                treatDoc.put("type", obj.getType());
                treatDoc.put("time", obj.getLastTime());

                treatList.add(treatDoc);
            }

            document.put("bracelet_id", patient.getBtMac());
            document.put("heart_rate", patient.getHeartRate());
            document.put("breathe_rate", patient.getBreatheRate());
            document.put("blood_pressure", patient.getBloodPressure());
            document.put("body_temp", patient.getBodyTemp());
            document.put("evacuation_request", String.valueOf(patient.getUrgentEvacuationState()));
            document.put("doctor_name", LoginActivity.doctorName);
            document.put("doctor_number", LoginActivity.doctorNumber);
            document.put("treatments", treatList);

            Bson searchQuery = new Document("bracelet_id", patient.getBtMac());
            UpdateOptions upsertDoc = new UpdateOptions();
            upsertDoc.upsert(true);

            try {
                dbCollection.replaceOne(searchQuery, document, upsertDoc);
                postToWeb();
                if (isCancelled())
                    return true;
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
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        int time = 5;
        BufferedReader reader=null;
        if (!aBoolean) {
            while (time > 0) {
                Toast.makeText(mContext, "Connection is lost! check your INTERNET and try again", Toast.LENGTH_LONG).show();
                time--;
            }
        }
    }

    @Override
    protected void onCancelled(){

    }

    /**
     * doing http POST to refresh the website for out changes
     */
    protected void postToWeb() {
        HttpURLConnection client = null;
        try {
            // Defined URL  where to send data
            URL url = new URL(myUrl);
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


