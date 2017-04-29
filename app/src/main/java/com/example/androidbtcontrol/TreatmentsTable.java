package com.example.androidbtcontrol;

import android.os.AsyncTask;
import android.util.Log;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.LinkedHashMap;

/**
 * Created by avizel on 19/4/2017.
 */

public class TreatmentsTable {
    LinkedHashMap<String, Treatment> treatmentsTable;

    public TreatmentsTable() {
        treatmentsTable = new LinkedHashMap<String, Treatment>();

        new TreatmentsTable.updateActivitiesTable().execute();
    }

    public LinkedHashMap<String, Treatment> getTreatmentsTable() {
        return treatmentsTable;
    }

    public void putToTable(String s, Treatment d) {
        treatmentsTable.put(s, d);
    }

    private class updateActivitiesTable extends AsyncTask<String, Integer, Long> {

        @Override
        protected Long doInBackground(String... strings) {
            MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_5zpcgjgx:j3cepqrurmjohqbftooulss265@ds145220.mlab.com:45220/heroku_5zpcgjgx");
            MongoClient mongoClient = new MongoClient(mongoUri);
            MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
            MongoCollection<BasicDBObject> dbCollection = db.getCollection("treatments", BasicDBObject.class);

            FindIterable<BasicDBObject> treatments = dbCollection.find();
            if (treatments == null) {
                return null;
            }
            for(BasicDBObject doc : treatments) {
                //access documents e.g. doc.get()
                Object number = doc.get("number");
                Object name = doc.get("name");
                Object type = doc.get("type");

                Treatment t = new Treatment(name.toString(), type.toString());
                treatmentsTable.put(number.toString(), t);

                //Log.e(MainActivity.class.getName(), number.toString());
                //Log.e(MainActivity.class.getName(), name.toString());
            }
            //activitiesTable
            /*for (LinkedHashMap.Entry<String, Details> entry : treatmentsTable.entrySet()) {
                String key = entry.getKey();
                Details value = entry.getValue();

                Log.e(MainActivity.class.getName(), key);
                Log.e(MainActivity.class.getName(), value.getName());
                Log.e(MainActivity.class.getName(), value.getType());
            }*/


            return null;
        }
    }
}

