package com.example.androidbtcontrol;

import android.os.AsyncTask;
import android.util.Log;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avizel on 19/4/2017.
 */


//mongodb://heroku_5zpcgjgx:j3cepqrurmjohqbftooulss265@ds145220.mlab.com:45220/heroku_5zpcgjgx
// mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0
public class SendToMongodbTask extends AsyncTask<List<Treatment>, Integer, Long> {
    @Override
    protected Long doInBackground(List<Treatment>... treatments) {

        //Log.e(MainActivity.class.getName(), "SendToMongodbTask");
        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

        BasicDBObject document = new BasicDBObject();
        //BasicDBObject treatDoc = new BasicDBObject();

        ArrayList<BasicDBObject> treatList = new ArrayList<>();

        for(Treatment obj : treatments[0]){
            BasicDBObject treatDoc = new BasicDBObject();
            treatDoc.put("Uid", obj.getName());
            treatDoc.put("type", obj.getType());
            treatDoc.put("time", obj.getLastTime());

            treatList.add(treatDoc);
        }

        document.put("treatments", treatList);

        //document.put("name", "alon");

        dbCollection.insertOne(document);

        // dbCollection.insertOne(BasicDBObject.parse(JsonMessage));
        //DBObject jsonData = (DBObject) JSON.parse(strings[0]);
        //dbCollection.insertOne(jsonData);
        return null;
    }
}
