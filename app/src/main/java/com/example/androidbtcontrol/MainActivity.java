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
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.LinkedHashMap;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;



public class MainActivity extends AppCompatActivity{



    BluetoothAdapter bluetoothAdapter;

    Button  btbTent, btnWeb, btnTst;

    TreatmentsTable tTable;
    //LinkedHashMap<String, String> activitiesTable = new LinkedHashMap<String, String>();

    static final float minDistance = 500; // the minimum distance for GPS updates


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tTable = new TreatmentsTable();

        btbTent = (Button)findViewById(R.id.buttonTent);
        btbTent.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent TentIntent = new Intent(MainActivity.this, TentActivity.class);

                startActivity(TentIntent);
            }});


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnWeb = (Button)findViewById(R.id.buttonWeb);// a button to test updates to the web
        btnWeb.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                new SendToMongodbTask().execute();
            }
        });

        final LocationManager locationManager=    (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        MyCurrentLoctionListener locationListener = new MyCurrentLoctionListener();
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, minDistance, /*(LocationListener)*/ locationListener);
        locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, minDistance, /*(LocationListener)*/ locationListener);

        btnTst = (Button)findViewById(R.id.myTestBtn);//a button to test location
        btnTst.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Location tempLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
                if(tempLocation == null)
                    tempLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);

                if(tempLocation != null) {
                    String myLocation = "Latitude = " + tempLocation.getLatitude() + " Longitude = " + tempLocation.getLongitude();

                    //I make a log to see the results
                    Log.e("MY CURRENT LOCATION", myLocation);
                }
            }

        });



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
