package com.example.androidbtcontrol;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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

// mongodb://heroku_5zpcgjgx:j3cepqrurmjohqbftooulss265@ds145220.mlab.com:45220/heroku_5zpcgjgx    ALON HEROKU
// mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0       WEB TEAM HEROKU
public class LoginTask extends AsyncTask<String, Integer, Boolean> {

    Context mContext;
    LoginTask(Context context) {
        mContext = context;
    }
    @Override
    protected Boolean doInBackground(String... doctor) {

        MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0");
        MongoClient mongoClient = new MongoClient(mongoUri);
        MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
        MongoCollection<BasicDBObject> dbCollection = db.getCollection("users", BasicDBObject.class);

        FindIterable<BasicDBObject> users = dbCollection.find();

        try {
            if (doctor.length == 2) {
                return checkUserAndPass(users, dbCollection, doctor);
            } else if (doctor.length == 1) {
                return checkUser(users, doctor[0]);
            }
        } catch (MongoTimeoutException e) {
            e.printStackTrace();
            return false;
        } catch (MongoSocketReadException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private boolean checkUserAndPass(FindIterable<BasicDBObject> users, MongoCollection<BasicDBObject> collection, String... doctor) {
        try {
            for (BasicDBObject doc : users) {
                Object user = doc.get("user");
                Object passw = doc.get("password");

                if (doctor[0].equals(user.toString()) && doctor[1].equals(passw.toString())) {
                    String number = doc.get("number").toString();
                    String name = doc.get("name").toString();

                    Bson searchQuery = new BasicDBObject().append("number", number);
                    Bson newValue = new BasicDBObject().append("status", "connected");
                    Bson updateOperationDocument = new BasicDBObject().append("$set", newValue);
                    collection.updateOne(searchQuery, updateOperationDocument);

                    LoginActivity.doctorName = name;
                    LoginActivity.doctorNumber = number;

                    return true;
                }
            }
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

    private boolean checkUser(FindIterable<BasicDBObject> users, String doctor) {
        try {
            for (BasicDBObject doc : users) {
                Object user = doc.get("user");

                if (doctor.equals(user.toString())) {
                    return true;
                }
            }
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


}
