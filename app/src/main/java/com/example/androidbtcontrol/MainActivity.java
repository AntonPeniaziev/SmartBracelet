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


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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



public class MainActivity extends AppCompatActivity {


    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;

    ImageButton  btbTent, testBtn;


    //LinkedHashMap<String, String> activitiesTable = new LinkedHashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btbTent = (ImageButton)findViewById(R.id.buttonTent);
        btbTent.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent TentIntent = new Intent(MainActivity.this, TentActivity.class);

                startActivity(TentIntent);
            }});

        testBtn = (ImageButton)findViewById(R.id.testButton);
        testBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(MainActivity.this, TestActivity.class);

                startActivity(newIntent);
            }});

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (false == bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }


    /**
     *
     */
    /*
    final LocationManager locationManager=    (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        MyCurrentLocationListener locationListener = new MyCurrentLocationListener();
        //locationManager.requestLocationUpdates(GPS_PROVIDER, 0, minDistance, /*(LocationListener)*/ //locationListener);
    //locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, /*(LocationListener)*/ locationListener);
    /*
    btnTst = (Button)findViewById(R.id.myTestBtn);
        btnTst.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v) {
                /*for (LinkedHashMap.Entry<String, Treatment> entry : tTable.getTreatmentsTable().entrySet()) {
                    String key = entry.getKey();
                    Treatment value = entry.getValue();

                    Log.e(MainActivity.class.getName(), key);
                    Log.e(MainActivity.class.getName(), value.getName());
                    Log.e(MainActivity.class.getName(), value.getType());
                }*/
           /* Location tempLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
        /*if(tempLocation == null)
            tempLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);*/

           /* if(tempLocation != null) {
                String myLocation = "Latitude = " + tempLocation.getLatitude() + " Longitude = " + tempLocation.getLongitude();

                //I make a log to see the results
                Log.e("MY CURRENT LOCATION", myLocation);
            }
        }

    });*/

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
