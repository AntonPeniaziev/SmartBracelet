package tasks;

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

import org.bson.conversions.Bson;

import activities.LoginActivity;


public class LogoutTask extends AsyncTask<Void, Void, Boolean> {

    private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
    private static final String collectionName = "users";
    private static final String numberTitle = "number";
    private static final String statusTitle = "status";
    private static final String connectedValue = "not connected";

    Context mContext;

    public LogoutTask(Context context) {
        mContext = context;
    }

    /**
     * for the connected user, changes his status in the DB into not connected and erase his name
     * @param params nothing
     */
    @Override
    protected Boolean doInBackground(Void ... params) {

        MongoClientURI mongoUri = new MongoClientURI(DBAdress);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection(collectionName, BasicDBObject.class);

        if (LoginActivity.doctorNumber.equals("") && LoginActivity.doctorName.equals(""))
            return true;

        boolean result = updateNotConnected(dbCollection);

        return result;
    }

    /**
     * if update to web doesn't succeed, it alerts the user for connection problems
     * @param aBoolean the result of the update
     */
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        int time = 5;
        if (!aBoolean) {
            while (time > 0) {
                Toast.makeText(mContext, "Connection is lost! check your INTERNET and try again", Toast.LENGTH_LONG).show();
                time--;
            }
        }
    }

    protected Boolean updateNotConnected(MongoCollection<BasicDBObject> collection) {
        try {
            Bson searchQuery = new BasicDBObject().append(numberTitle, LoginActivity.doctorNumber);
            Bson newValue1 = new BasicDBObject().append(statusTitle, connectedValue);
            Bson updateOperationDocument1 = new BasicDBObject().append("$set", newValue1);

            collection.updateOne(searchQuery, updateOperationDocument1);
            emptyLocalDoctorDetails();

            return true;
        } catch (MongoSocketReadException | MongoSocketOpenException | MongoSecurityException | MongoTimeoutException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void emptyLocalDoctorDetails() {
        LoginActivity.doctorName = "";
        LoginActivity.doctorNumber = "";
        LoginActivity.doctorDivision = "";
    }

}