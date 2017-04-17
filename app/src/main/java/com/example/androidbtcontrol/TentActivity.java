package com.example.androidbtcontrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import BTservice.BTservice;

import static java.lang.System.currentTimeMillis;

public class TentActivity extends AppCompatActivity {

    TextView textInfo2;
    BTservice _bTservice;
    Tent _tent;
    //using the well-known SPP UU
    // ID
    Handler handler;
    UpdateData updateData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _tent = new Tent();
        textInfo2 = (TextView)findViewById(R.id.myView);
        //String str = "TTTTTTTTTTTTTTTTTT";
       // textInfo2.setText(str);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        _bTservice = new BTservice(textInfo2, this);
        Toast.makeText(this,
                "Tent Activity created!",
                Toast.LENGTH_SHORT).show();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                textInfo2.setText(_tent.getAllIds());
                super.handleMessage(msg);
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        _bTservice.startBT();
        updateData = new UpdateData();
        updateData.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _bTservice.destroy();
        if(updateData!=null){
          //  updateData.cancel();
        }
    }

    private class UpdateData extends Thread {

        @Override
        public void run() {
            // Moves the current Thread into the background
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                _tent.updatePatientInfoFromBT(_bTservice.getMacToJsonList());
                _bTservice.clearBtBuffers();
               // Log.d("ALL:",_tent.getAllIds());
              //  handler.sendEmptyMessage(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // textInfo2.setText(Long.toString(currentTimeMillis()));
                        textInfo2.setText(_tent.getAllIds());
                       // textInfo2.setText("hello");
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }
    }

}


