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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;

/**
 * AsyncTask to notify the web to update evacuation for specific patient
 * @param params is an array contains bracelet_id and a boolean string
 */

public class CallEvacuationTask extends AsyncTask<String, Integer, Boolean> {

    private Context mContext;
    CallEvacuationTask(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

        try {
            Bson searchQuery = new Document("bracelet_id", params[1]);
            Bson newValue = new BasicDBObject().append("evacuation_request", params[0]);
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

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        int time = 3;
        if (!aBoolean) {
            while (time > 0) {
                Toast.makeText(mContext, "Connection is lost! check your INTERNET and try again", Toast.LENGTH_LONG).show();
                time--;
            }
        }
    }
}
