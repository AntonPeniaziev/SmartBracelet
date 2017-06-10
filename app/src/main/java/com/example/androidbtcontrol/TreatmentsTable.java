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

import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by avizel on 19/4/2017.
 */

public class TreatmentsTable {
    LinkedHashMap<String, Equipment> treatmentsTable;

    public TreatmentsTable(Context context) {
        treatmentsTable = new LinkedHashMap<String, Equipment>();

        new updateActivitiesTable(context).execute();

    }

    public LinkedHashMap<String, Equipment> getTreatmentsTable() {
        return treatmentsTable;
    }

    public boolean containsKey(Object key) {
        return treatmentsTable.containsKey(key);
    }

    public Equipment get(Object key) {
        return treatmentsTable.get(key);
    }

    public void putToTable(String s, Equipment d) {
        treatmentsTable.put(s, d);
    }

    private class updateActivitiesTable extends AsyncTask<String, Integer, Boolean> {

        private Context mContext;
        updateActivitiesTable(Context context) {
            mContext = context;
        }
        @Override
        protected Boolean doInBackground(String... strings) {
            MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
            MongoClient mongoClient = new MongoClient(mongoUri);
            MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
            MongoCollection<BasicDBObject> dbCollection = db.getCollection("equipment", BasicDBObject.class);

            FindIterable<BasicDBObject> treatments = dbCollection.find();
            try {
                for (BasicDBObject doc : treatments) {
                    //access documents e.g. doc.get()
                    Object number = doc.get("equipment_id");
                    Object name = doc.get("name");
                    Object type = doc.get("type");
                    //TODO add time
                    Equipment t = new Equipment(name.toString(), type.toString(), number.toString());
                    treatmentsTable.put(number.toString(), t);

                    //Log.e(MainActivity.class.getName(), number.toString());
                    //Log.e(MainActivity.class.getName(), name.toString());
                }
            } catch (MongoTimeoutException e) {
                e.printStackTrace();
                treatmentsTable = null;
                //Toast.makeText(mContext, "Something is wrong. Please check your INTERNET connection", Toast.LENGTH_LONG).show();
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
            //activitiesTable
            /*for (LinkedHashMap.Entry<String, Details> entry : treatmentsTable.entrySet()) {
                String key = entry.getKey();
                Details value = entry.getValue();

                Log.e(MainActivity.class.getName(), key);
                Log.e(MainActivity.class.getName(), value.getName());
                Log.e(MainActivity.class.getName(), value.getType());
            }*/


            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            int count = 7;
            if (!aBoolean){
                while (count > 0) {
                    Toast.makeText(mContext, "Info missing! Please check your INTERNET connection and restart", Toast.LENGTH_LONG).show();
                    count--;
                }
            }
        }
    }
}

