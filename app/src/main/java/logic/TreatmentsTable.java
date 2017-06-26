package logic;

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

    private LinkedHashMap<String, String> equipmentNameToCodeTable;

    public TreatmentsTable(Context context) {
        codeToEquipmentTable = new LinkedHashMap<String, Equipment>();
        equipmentNameToCodeTable = new LinkedHashMap<String, String>();

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
        if (codeToEquipmentTable == null) {
            return null;
        }
        return codeToEquipmentTable.get(key);
    }

    /**
     * for a valid name of equipment, returns its code name
     * @param key
     * @return String of code name
     */
    public String getCode(Object key) { return equipmentNameToCodeTable.get(key); }

    /*public void putToTable(String s, Equipment d) {
        codeToEquipmentTable.put(s, d);
    }*/

    private class updateActivitiesTable extends AsyncTask<String, Integer, Boolean> {

        private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
        private static final String collectionName = "equipment";
        private static final String idTitle = "bracelet_id";
        private static final String nameTitle = "name";
        private static final String typeTitle = "type";

        private Context mContext;
        updateActivitiesTable(Context context) {
            mContext = context;
        }

        /**
         * updates the translation tables from the web
         * @param strings
         * @return
         */
        @Override
        protected Boolean doInBackground(String... strings) {
            MongoClientURI mongoUri = new MongoClientURI(DBAdress);
            MongoClient mongoClient = new MongoClient(mongoUri);
            MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
            MongoCollection<BasicDBObject> dbCollection = db.getCollection(collectionName, BasicDBObject.class);

            FindIterable<BasicDBObject> treatments = dbCollection.find();
            boolean result = updateTables(treatments);
            if (!result){
                codeToEquipmentTable = null;
                equipmentNameToCodeTable = null;
            }
            /*try {
                for (BasicDBObject doc : treatments) {
                    Object number = doc.get(idTitle);
                    Object name = doc.get(nameTitle);
                    Object type = doc.get(typeTitle);

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

        /**
         * updates the trables with the given treatments
         * @param treatments treatments to update
         * @return boolean for success
         */
        protected Boolean updateTables(FindIterable<BasicDBObject> treatments) {
            try {
                for (BasicDBObject doc : treatments) {
                    Object number = doc.get(idTitle);
                    Object name = doc.get(nameTitle);
                    Object type = doc.get(typeTitle);

                    Equipment t = new Equipment(name.toString(), type.toString(), number.toString());
                    codeToEquipmentTable.put(number.toString(), t);
                    equipmentNameToCodeTable.put(name.toString(), number.toString());
                }
                return true;
            } catch (MongoTimeoutException | MongoSocketReadException | MongoSecurityException | MongoSocketOpenException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}

