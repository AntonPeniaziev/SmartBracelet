package tasks;

import android.os.AsyncTask;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.conversions.Bson;

import java.util.ArrayList;

import activities.LoginActivity;

/**
 * Created by user on 07/06/2017.
 */

public class LocationTask extends AsyncTask<Double, Integer, Boolean> {

    private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
    private static final String collectionName = "users";
    private static final String numberTitle = "number";
    private static final String longitudeTitle = "longitude";
    private static final String latitudeTitle = "latitude";

    /**
     * updates the web for the location of the local doctor
     * @param coordinates an array of the new coordinates
     * @return boolean for success
     */
    @Override
    protected Boolean doInBackground(Double... coordinates) {
        MongoClientURI mongoUri = new MongoClientURI(DBAdress);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection(collectionName, BasicDBObject.class);

        boolean result = updateLocation(dbCollection, coordinates[0], coordinates[1]);
        PostToWeb.postToWeb();
        /*try {
            Bson searchQuery = new BasicDBObject().append(numberTitle, LoginActivity.doctorNumber);
            Bson newValue1 = new BasicDBObject().append(longitudeTitle, coordinates[0].toString());
            Bson newValue2 = new BasicDBObject().append(latitudeTitle, coordinates[1].toString());
            Bson updateOperationDocument1 = new BasicDBObject().append("$set", newValue1);
            Bson updateOperationDocument2 = new BasicDBObject().append("$set", newValue2);

            dbCollection.updateOne(searchQuery, updateOperationDocument1);
            dbCollection.updateOne(searchQuery, updateOperationDocument2);

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
        }*/
        return result;
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

    /**
     * for a given collection, updates the new location of the local doctor
     * @param collection the collection where to update
     * @param longitude the longitude value
     * @param latitude the latitude value
     * @return boolean for success
     */
    protected Boolean updateLocation(MongoCollection<BasicDBObject> collection, Double longitude, Double latitude) {
        try {
            Bson searchQuery = new BasicDBObject().append(numberTitle, LoginActivity.doctorNumber);
            Bson newValue1 = new BasicDBObject().append(longitudeTitle, longitude.toString());
            Bson newValue2 = new BasicDBObject().append(latitudeTitle, latitude.toString());
            Bson updateOperationDocument1 = new BasicDBObject().append("$set", newValue1);
            Bson updateOperationDocument2 = new BasicDBObject().append("$set", newValue2);

            collection.updateOne(searchQuery, updateOperationDocument1);
            collection.updateOne(searchQuery, updateOperationDocument2);

            return true;
        } catch (MongoTimeoutException | MongoSocketReadException | MongoSocketOpenException | MongoSecurityException e) {
            e.printStackTrace();
        }
        return false;
    }
}
