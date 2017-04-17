package com.example.androidbtcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import BTservice.BTservice;

public class TentActivity extends AppCompatActivity {

    TextView textInfo2;
    BTservice _bTservice;
    Tent _tent;
    UpdateData updateData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _tent = new Tent();
        textInfo2 = (TextView)findViewById(R.id.myView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        _bTservice = new BTservice(textInfo2, this);


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
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                _tent.updatePatientInfoFromBT(_bTservice.getMacToJsonList());
                _bTservice.clearBtBuffers();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUI();
                    }
                });
                //release for UI
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    void runOnUI() {
        textInfo2.setText(_tent.getAllIds());
    }

}


