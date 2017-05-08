package com.example.androidbtcontrol;

import android.os.AsyncTask;
import android.util.Log;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Created by avizel on 19/4/2017.
 */

//mongodb://heroku_5zpcgjgx:j3cepqrurmjohqbftooulss265@ds145220.mlab.com:45220/heroku_5zpcgjgx   my mongoDB

// mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0
public class SendToMongodbTask extends AsyncTask<Patient, Integer, Long> {
    @Override
    protected Long doInBackground(Patient... patients) {

        Log.e(MainActivity.class.getName(), "SendToMongodbTask");
        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

        BasicDBObject document = new BasicDBObject();

       /* document.put("MacNumber", patients[0].getBtMac());
        document.put("info", patients[0].getJson());
        document.put("BloodPressure", patients[0].getBloodPressure());
        document.put("BodyTemperature", patients[0].getBodyTemp());
        document.put("HeartRate", patients[0].getHeartRate());
        document.put("BreathRate", patients[0].getBreatheRate());*/

        document.put("Name", "Rami");
        document.put("MacNumber", "zxc321");
        document.put("Treatments", "[{uid: tourniquet, ts: 8}]");
        document.put("BloodPressure", "120/85");
        document.put("BodyTemperature", "37.7 ºC");
        document.put("HeartRate", "90");


        BasicDBObject locationDoc = new BasicDBObject();
        locationDoc.put("Latitude", "32.7908355");
        locationDoc.put("Longitude", "34.9607394");
        document.put("Location", locationDoc);




        dbCollection.insertOne(document);

        // dbCollection.insertOne(BasicDBObject.parse(JsonMessage));
        //DBObject jsonData = (DBObject) JSON.parse(strings[0]);
        //dbCollection.insertOne(jsonData);
        return null;
    }
}
