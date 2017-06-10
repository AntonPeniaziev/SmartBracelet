package com.example.androidbtcontrol;

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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 07/06/2017.
 */

public class LocationTask extends AsyncTask<Double, Integer, Boolean> {

//    private Context mContext;
//    LocationTask(Context context) {
//        mContext = context;
//    }

    @Override
    protected Boolean doInBackground(Double... coordinates) {
        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("users", BasicDBObject.class);

        //FindIterable<BasicDBObject> users = dbCollection.find();
        try {
            Bson searchQuery = new BasicDBObject().append("number", LoginActivity.doctorNumber);

            ArrayList<BasicDBObject> locationList = new ArrayList<>();
            BasicDBObject locationDoc = new BasicDBObject();

            locationDoc.put("longitude", coordinates[0].toString());
            locationDoc.put("latitude", coordinates[1].toString());
            locationList.add(locationDoc);

            Bson newValue = new BasicDBObject().append("location", locationList);
            Bson updateOperationDocument = new BasicDBObject().append("$set", newValue);

            dbCollection.updateOne(searchQuery, updateOperationDocument);

            return true;
        } catch (MongoTimeoutException e) {
            e.printStackTrace();
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
        return false;
    }

    /*@Override
    protected void onPostExecute(Boolean aBoolean) {
        int time = 5;
        if (!aBoolean) {
            while (time > 0) {
                Toast.makeText(mContext, "Connection is lost! check your INTERNET and try again", Toast.LENGTH_LONG).show();
                time--;
            }
        }
    }*/
}
