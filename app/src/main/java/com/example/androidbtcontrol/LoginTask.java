package com.example.androidbtcontrol;

/**
 * Created by avizel on 17/5/2017.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

// mongodb://heroku_5zpcgjgx:j3cepqrurmjohqbftooulss265@ds145220.mlab.com:45220/heroku_5zpcgjgx    ALON HEROKU
// mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0       WEB TEAM HEROKU
public class LoginTask extends AsyncTask<String, Integer, Boolean> {
    @Override
    protected Boolean doInBackground(String... doctor) {

        //Log.e(MainActivity.class.getName(), "SendToMongodbTask");
        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("users", BasicDBObject.class);

        FindIterable<BasicDBObject> users = dbCollection.find();
        int i = 1;
        long count = dbCollection.count();

        for(BasicDBObject doc : users){
            publishProgress((int) ((i / (long) count) * 100));
            Object user = doc.get("user");
            Object passw = doc.get("password");
            i++;

            if (doctor[0].equals(user.toString()) && doctor[1].equals(passw.toString())) {
                //Log.e(TestActivity.class.getName(), doctor[0] + " is a valid user");
                //Toast.makeText(, "valid user", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        //Log.e(TestActivity.class.getName(), doctor[0] + " is not a valid user");

        return false;
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(boolean result) {
        super.onPostExecute(result);
    }


}
