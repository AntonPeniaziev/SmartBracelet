package tasks;

import android.content.Context;
import android.os.AsyncTask;

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

import activities.LoginActivity;

public class LoginTask extends AsyncTask<String, Integer, Boolean> {

    private static final String DBAdress = "mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0";
    private static final String collectionName = "users";
    private static final String numberTitle = "number";
    private static final String nameTitle = "name";
    private static final String divisionTitle = "division";
    private static final String statusTitle = "status";
    private static final String userTitle = "user";
    private static final String passwordTitle = "password";
    private static final String connectedValue = "connected";

    private Context mContext;
    private boolean _value = false;

    public LoginTask(Context context) {
        mContext = context;
        _value = true;
    }

    /**
     * for a given login details, checks for a matching user
     * @param doctor and array with login details
     * @return
     */
    @Override
    protected Boolean doInBackground(String... doctor) {
        MongoClientURI mongoUri = new MongoClientURI(DBAdress);
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection(collectionName, BasicDBObject.class);

        FindIterable<BasicDBObject> users = dbCollection.find();

        if (doctor.length == 2) {
            _value = checkUserAndPass(users, dbCollection, doctor);
            return _value;
        }

        return _value;
    }

    /**
     * iterate over the users in the DB. if one match for the login details, updates his status to connected
     * @param users iterable object of existing users
     * @param collection DB collection of the users
     * @param doctor login details String array
     * @return
     */
    private boolean checkUserAndPass(FindIterable<BasicDBObject> users, MongoCollection<BasicDBObject> collection, String... doctor) {
        try {
            for (BasicDBObject doc : users) {
                Object user = doc.get(userTitle);
                Object passw = doc.get(passwordTitle);
                if (user == null || passw == null)
                    continue;

                if (doctor[0].equals(user.toString()) && doctor[1].equals(passw.toString())) {
                    String name = doc.get(nameTitle).toString();
                    String number = doc.get(numberTitle).toString();
                    Object divisionObj = doc.get(divisionTitle);        // not all users belong to a division

                    String division = "";
                    if (divisionObj != null)
                        division = divisionObj.toString();

                    Bson searchQuery = new BasicDBObject().append(numberTitle, number);
                    Bson newValue = new BasicDBObject().append(statusTitle, connectedValue);
                    Bson updateOperationDocument = new BasicDBObject().append("$set", newValue);
                    collection.updateOne(searchQuery, updateOperationDocument);

                    updateLocalDoctorDetails(name, number, division);
                    return true;
                }
            }
        } catch (MongoTimeoutException | MongoSocketReadException | MongoSocketOpenException | MongoSecurityException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onPostExecute(Boolean value){
        super.onPostExecute(value);
        if (!_value) {
            LoginActivity._errorMsg = "Please enter a valid USER & PASSWORD or check your INTERNET connection";
            LoginActivity._loginButton.setEnabled(true);
            LoginActivity._passwordText.setText("");
            LoginActivity._progressDialog.dismiss();
            LoginActivity.getInstance().onLoginFailed();

            return;
        }

        LoginActivity._progressDialog.dismiss();
        LoginActivity.getInstance().onLoginSuccess();
    }

    private void updateLocalDoctorDetails(String name, String number, String division) {
        LoginActivity.doctorName = name;
        LoginActivity.doctorNumber = number;
        LoginActivity.doctorDivision = division;
    }

}
