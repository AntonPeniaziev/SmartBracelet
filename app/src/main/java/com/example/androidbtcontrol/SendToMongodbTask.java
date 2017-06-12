package com.example.androidbtcontrol;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;


//mongodb://heroku_5zpcgjgx:j3cepqrurmjohqbftooulss265@ds145220.mlab.com:45220/heroku_5zpcgjgx
// mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0
public class SendToMongodbTask extends AsyncTask<ArrayList<Patient>, Integer, Boolean> {

    private Context mContext;
    SendToMongodbTask(Context context) {
        mContext = context;
    }
    @Override
    protected Boolean doInBackground(ArrayList<Patient> ... patients) {

        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
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

            ArrayList<BasicDBObject> doctorList = new ArrayList<>();
            BasicDBObject doctorDoc = new BasicDBObject();
            doctorDoc.put("name", LoginActivity.doctorName);
            doctorDoc.put("number", LoginActivity.doctorNumber);
            doctorList.add(doctorDoc);

            document.put("bracelet_id", patient.getBtMac());
            document.put("Heart_Rate", patient.getHeartRate());
            document.put("Breathe_Rate", patient.getBreatheRate());
            document.put("Blood_Pressure", patient.getBloodPressure());
            document.put("Body_Temp", patient.getBodyTemp());
            document.put("evacuation_request", String.valueOf(patient.is_urgantEvacuation()));
            document.put("treatments", treatList);
            document.put("doctor", doctorList);

            Bson searchQuery = new Document("bracelet_id", patient.getBtMac());
            UpdateOptions upsertDoc = new UpdateOptions();
            upsertDoc.upsert(true);

            try {
                dbCollection.replaceOne(searchQuery, document, upsertDoc);
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
}
