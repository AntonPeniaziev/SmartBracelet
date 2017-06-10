package com.example.androidbtcontrol;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ExecutionException;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class TestActivity extends AppCompatActivity {

    static final float minDistanceForGpsUpdate = 500; // the minimum distance for GPS updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final LocationManager locationManager=    (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        MyCurrentLocationListener locationListener = new MyCurrentLocationListener();
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, minDistanceForGpsUpdate, (LocationListener)locationListener);
        locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, minDistanceForGpsUpdate, (LocationListener) locationListener);

        Button testButton = (Button) findViewById(R.id.testMe);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Location tempLocation = locationManager.getLastKnownLocation(GPS_PROVIDER);
                if (tempLocation == null)
                    tempLocation = locationManager.getLastKnownLocation(NETWORK_PROVIDER);

                if (tempLocation != null) {
                    String myLocation = "Latitude = " + tempLocation.getLatitude() + " Longitude = " + tempLocation.getLongitude();

                   // new SendToMongodbTask().execute(myLocation);
                    //Add a function to test
                }*/

                String[] userAndPass = {"doctor5", "123"};
                boolean output = false;
                try {
                    output = new LoginTask(TestActivity.this).execute(userAndPass).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Log.e(TestActivity.class.getName(), "result for doctor5 is " + output);
            }
        });


    }
}
