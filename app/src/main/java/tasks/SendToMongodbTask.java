package tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import activities.TentActivity;
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

    private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
    private static final String collectionName = "soldiers";
    private static final String idTitle = "Bracelet_ID";
    private static final String evacTitle = "evacuation_request";
    private static final String StatusTitle = "Status";
    private static final String heartRateTitle = "heart_rate";
    private static final String breatheRateTitle = "breathe_rate";
    private static final String bloodPressureTitle = "blood_pressure";
    private static final String bodyTempTitle = "body_temp";
    private static final String DrNameTitle = "Dr_Name";
    private static final String DrNumberTitle = "Dr_number";
    private static final String divisionTitle = "Division";
    private static final String longitudeTitle = "Longitude";
    private static final String latitudeTitle = "Latitude";
    private static final String treatmentsTitle = "treatments";

    private static final String UidTitle = "Uid";
    private static final String typeTitle = "type";
    private static final String timeTitle = "time";

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
        MongoCollection<BasicDBObject> dbCollection = db.getCollection(collectionName, BasicDBObject.class);
        boolean result = true;

        for (Patient patient : patients[0]) {
            //BasicDBObject document = new BasicDBObject();
            ArrayList<BasicDBObject> treatList = new ArrayList<>();

            for (Treatment obj : patient.getTreatmentsArray()) {
                BasicDBObject treatDoc = new BasicDBObject();
                treatDoc.put(UidTitle, obj.getName());
                treatDoc.put(typeTitle, obj.getType());
                treatDoc.put(timeTitle, obj.getLastTime());
                treatList.add(treatDoc);
            }

            result = insertToDocAndUpdate(dbCollection, patient, treatList);
            if (!result)
                return false;
            if (isCancelled())
                return true;
            /*document.put(idTitle, patient.getBtMac());
            document.put(heartRateTitle, patient.getHeartRate());
            document.put(breatheRateTitle, patient.getBreatheRate());
            document.put(bloodPressureTitle, patient.getBloodPressure());
            document.put(bodyTempTitle, patient.getBodyTemp());
            document.put(evacTitle, String.valueOf(patient.getUrgentEvacuationState()));
            document.put(StatusTitle, patient.getPatientState());
            document.put(DrNameTitle, LoginActivity.doctorName);
            document.put(DrNumberTitle, LoginActivity.doctorNumber);
            document.put(divisionTitle, LoginActivity.doctorDivision);
            document.put(longitudeTitle, TentActivity.locationListener.getLongitude());
            document.put(latitudeTitle, TentActivity.locationListener.getLatitude());
            document.put(treatmentsTitle, treatList);

            Bson searchQuery = new Document(idTitle, patient.getBtMac());
            UpdateOptions upsertDoc = new UpdateOptions();
            upsertDoc.upsert(true);

            try {
                dbCollection.replaceOne(searchQuery, document, upsertDoc);
                PostToWeb.postToWeb();
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
            }*/
        }
        PostToWeb.postToWeb();
        return result;
    }

    /**
     * if update to web doesn't succeed, it alerts the user for connection problems
     * @param aBoolean the result of the update
     */
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        int time = 5;
        if (!aBoolean) {
            while (time > 0) {
                Toast.makeText(mContext, "Connection is lost! check your INTERNET and try again", Toast.LENGTH_LONG).show();
                time--;
            }
        }
    }

    @Override
    protected void onCancelled(){}

    /**
     * updates the web a collection with the details of given patient
     * @param collection a collection to update
     * @param patient the patient to update his treatments
     * @param treatList treatment list of the given patient
     * @return boolean for success
     */
    private Boolean insertToDocAndUpdate(MongoCollection<BasicDBObject> collection, Patient patient, ArrayList<BasicDBObject> treatList) {
        BasicDBObject document = new BasicDBObject();
        document.put(idTitle, patient.getBtMac());
        document.put(heartRateTitle, patient.getHeartRate());
        document.put(breatheRateTitle, patient.getBreatheRate());
        document.put(bloodPressureTitle, patient.getBloodPressure());
        document.put(bodyTempTitle, patient.getBodyTemp());
        document.put(evacTitle, String.valueOf(patient.getUrgentEvacuationState()));
        document.put(StatusTitle, patient.getPatientState());
        document.put(DrNameTitle, LoginActivity.doctorName);
        document.put(DrNumberTitle, LoginActivity.doctorNumber);
        document.put(divisionTitle, LoginActivity.doctorDivision);
        document.put(longitudeTitle, TentActivity.locationListener.getLongitude());
        document.put(latitudeTitle, TentActivity.locationListener.getLatitude());
        document.put(treatmentsTitle, treatList);

        Bson searchQuery = new Document(idTitle, patient.getBtMac());
        UpdateOptions upsertDoc = new UpdateOptions();
        upsertDoc.upsert(true);

        try {
            collection.replaceOne(searchQuery, document, upsertDoc);
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

    /**
     * doing http POST to refresh the website for out changes
     */
    /*protected void postToWeb() {
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
    }*/
}


