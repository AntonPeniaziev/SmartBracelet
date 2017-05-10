/*
Android Example to connect to and communicate with Bluetooth
In this exercise, the target is a Arduino Due + HC-06 (Bluetooth Module)

Ref:
- Make BlueTooth connection between Android devices
http://android-er.blogspot.com/2014/12/make-bluetooth-connection-between.html
- Bluetooth communication between Android devices
http://android-er.blogspot.com/2014/12/bluetooth-communication-between-android.html
 */
package com.example.androidbtcontrol;


import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.LinkedHashMap;



public class MainActivity extends AppCompatActivity{



    //BluetoothAdapter bluetoothAdapter;

    Button  btbTent;



    LinkedHashMap<String, String> activitiesTable = new LinkedHashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btbTent = (Button)findViewById(R.id.buttonTent);
        btbTent.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent TentIntent = new Intent(MainActivity.this, TentActivity.class);

                startActivity(TentIntent);
            }});


//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null) {
//            Toast.makeText(this,
//                    "Bluetooth is not supported on this hardware platform",
//                    Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }

    }



    //mongodb://heroku_8lwbv1x0:hlus7a54o0lnapqd2nhtlkaet7@dbh73.mlab.com:27737/heroku_8lwbv1x0
    private class SendToMongodbTask extends AsyncTask<String, Integer, Long> {

        @Override
        protected Long doInBackground(String... strings) {

            Log.e(MainActivity.class.getName(), "SendToMongodbTask");
            MongoClientURI mongoUri = new MongoClientURI("mongodb://heroku_5zpcgjgx:j3cepqrurmjohqbftooulss265@ds145220.mlab.com:45220/heroku_5zpcgjgx");
            MongoClient mongoClient = new MongoClient(mongoUri);
            MongoDatabase db = mongoClient.getDatabase(mongoUri.getDatabase());
            MongoCollection<BasicDBObject> dbCollection = db.getCollection("soldiers", BasicDBObject.class);

            //   BasicDBObject document = new BasicDBObject();

            //document.put("name", strings[0]);
            //  document.put("number", 7789);

            // dbCollection.insertOne(BasicDBObject.parse(JsonMessage));
            //DBObject jsonData = (DBObject) JSON.parse(strings[0]);
            //dbCollection.insertOne(jsonData);
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  unregisterReceiver(mReceiver);

    }



}
