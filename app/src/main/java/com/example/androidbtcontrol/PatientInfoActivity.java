package com.example.androidbtcontrol;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class PatientInfoActivity extends AppCompatActivity {

    PatientInfoAdapter _patientsAdapter;
    ListView _listView;
    String _patientMac;
    TextView hr;
    UpdateData _updateData;
    Button _saveButton;
    Button _urgentButton;
    ImageButton _backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);


        _listView = (ListView) findViewById(R.id.listView);

        _patientsAdapter = new PatientInfoAdapter(this, R.layout.patient_info_list_row);
        //_listView.setAdapter(_patientsAdapter);

        //_listView.setAdapter(_patientsAdapter);
        /******************************************************************************/
        //_listView = (ListView) findViewById(android.R.id.list);

        _listView.setAdapter(_patientsAdapter);
       // _listView.setOnItemClickListener(this);

        /******************************************************************************/
        String patientID = getIntent().getStringExtra("PATIENT_ID");
        TextView text = (TextView) findViewById(R.id.braceletID);
        Typeface army_font = Typeface.createFromAsset(getAssets(), "fonts/Army.ttf");
        text.setTypeface(army_font);
        text.setText(patientID);

        _patientMac = patientID;

        hr = (TextView) findViewById(R.id.heartRate);
        hr.setText(TentActivity._tent.getHeartrateByMac(patientID));
        _saveButton = (Button)findViewById(R.id.saveFile);
        _urgentButton = (Button) findViewById(R.id.button4);
        _saveButton.setClickable(true);
        _urgentButton.setClickable(true);
        _backButton = (ImageButton) findViewById(R.id.back_button);
        _backButton.setClickable(true);
        _backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        _updateData = new UpdateData();
        _updateData.start();
    }

    private class UpdateData extends Thread {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
//                _tent.updatePatientInfoFromBT(_bTservice.getMacToJsonList());
//                _bTservice.clearBtBuffers();

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
        hr.setText(TentActivity._tent.getHeartrateByMac(_patientMac));
        //TODO : pressure, breath ..
        updateListView(TentActivity._tent.getTreatmentsArrayByMac(_patientMac));
    }

    void updateListView(ArrayList<Treatment> treatmentsArr){
        _patientsAdapter.setData(treatmentsArr);
        _patientsAdapter.notifyDataSetChanged();
    }
}
