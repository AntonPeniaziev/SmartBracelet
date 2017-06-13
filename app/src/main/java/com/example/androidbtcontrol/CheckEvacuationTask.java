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

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * Created by user on 13/06/2017.
 */

public class CheckEvacuationTask extends AsyncTask<Tent, Integer, Boolean> {

    private Context mContext;
    CheckEvacuationTask(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(Tent... params) {
        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

        FindIterable<BasicDBObject> soldiers = dbCollection.find();

        try{
            for (BasicDBObject soldier : soldiers) {
                Object mac = soldier.get("bracelet_id");
                Object evac = soldier.get("evacuation_request");
                if (params[0].isContain(mac.toString())){
                    if (evac.toString().equals("true") && !params[0].getUrgantEvacuation(mac.toString())){
                        params[0].setUrgantEvacuation(mac.toString(), true);
                        //Toast.makeText(mContext, "Evacuation was set for " + mac.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                if (isCancelled())
                    return true;
            }
            //return true;
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

    @Override
    protected void onPostExecute(Boolean aBoolean) {
//        int time = 5;
//        if (!aBoolean) {
//            while (time > 0) {
//                Toast.makeText(mContext, "Connection is lost! check your INTERNET and try again", Toast.LENGTH_LONG).show();
//                time--;
//            }
//        }
    }

    @Override
    protected void onCancelled(){

    }
}
