package com.example.androidbtcontrol;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avizel on 19/4/2017.
 */


//mongodb://heroku_5zpcgjgx:j3cepqrurmjohqbftooulss265@ds145220.mlab.com:45220/heroku_5zpcgjgx
// mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0
public class SendToMongodbTask extends AsyncTask</*List<Treatment>*/Patient, Integer, Boolean> {

    private Context mContext;
    SendToMongodbTask(Context context) {
        mContext = context;
    }
    @Override
    protected Boolean doInBackground(/*List<Treatment>*/Patient ... patients) {

        //Log.e(MainActivity.class.getName(), "SendToMongodbTask");
        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

        BasicDBObject document = new BasicDBObject();
        ArrayList<BasicDBObject> treatList = new ArrayList<>();

        for(Treatment obj : patients[0].getTreatmentsArray()){
            BasicDBObject treatDoc = new BasicDBObject();
            treatDoc.put("Uid", obj.getName());
            treatDoc.put("type", obj.getType());
            treatDoc.put("time", obj.getLastTime());

            treatList.add(treatDoc);
        }

        document.put("bracelet_id", patients[0].getBtMac());
        document.put("Heart_Rate", patients[0].getHeartRate());
        document.put("Breathe_Rate", patients[0].getBreatheRate());
        document.put("Blood_Pressure", patients[0].getBloodPressure());
        document.put("Body_Temp", patients[0].getBodyTemp());
        document.put("treatments", treatList);

        //document.put("name", "alon");

        try {
            dbCollection.insertOne(document);
        } catch (MongoTimeoutException e) {
            e.printStackTrace();
            return false;
        }

        // dbCollection.insertOne(BasicDBObject.parse(JsonMessage));
        //DBObject jsonData = (DBObject) JSON.parse(strings[0]);
        //dbCollection.insertOne(jsonData);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        int time = 7;
        if (!aBoolean) {
            while (time > 0) {
                Toast.makeText(mContext, "Connection is lost! check your INTERNET and try again", Toast.LENGTH_LONG).show();
                time--;
            }
        }
    }
}
