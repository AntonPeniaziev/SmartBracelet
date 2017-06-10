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

/**
 * Created by avizel on 19/4/2017.
 */

public class TreatmentsTable {
    private LinkedHashMap<String, Equipment> codeToEquipmentTable;

    private LinkedHashMap<String, String> EquipmentNameToCodeTable;

    public TreatmentsTable(Context context) {
        codeToEquipmentTable = new LinkedHashMap<String, Equipment>();
        EquipmentNameToCodeTable = new LinkedHashMap<String, String>();

        new updateActivitiesTable(context).execute();

    }

    /*public LinkedHashMap<String, Equipment> getCodeToEquipmentTable() {
        return codeToEquipmentTable;
    }*/

    /*public boolean containsKey(Object key) {
        return codeToEquipmentTable.containsKey(key);
    }*/

    /**
     * for a valid code, returns the suitable equipment
     * @param key
     * @return Equipment
     */
    public Equipment getEquipment(Object key) {
        return codeToEquipmentTable.get(key);
    }

    /**
     * for a valid name of equipment, returns its code name
     * @param key
     * @return String of code name
     */
    public String getCode(Object key) { return EquipmentNameToCodeTable.get(key); }

    /*public void putToTable(String s, Equipment d) {
        codeToEquipmentTable.put(s, d);
    }*/

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
                    codeToEquipmentTable.put(number.toString(), t);
                    EquipmentNameToCodeTable.put(name.toString(), number.toString());

                }
            } catch (MongoTimeoutException e) {
                e.printStackTrace();
                codeToEquipmentTable = null;
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

