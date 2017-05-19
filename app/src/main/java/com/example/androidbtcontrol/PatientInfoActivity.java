package com.example.androidbtcontrol;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class PatientInfoActivity extends AppCompatActivity {

    PatientInfoAdapter _patientsAdapter;
    ListView _listView;
    String _patientMac;
    TextView hr;
    UpdateData _updateData;
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
        text.setText(patientID);

        _patientMac = patientID;

        hr = (TextView) findViewById(R.id.heartRate);
        hr.setText(TentActivity._tent.getHeartrateByMac(patientID));
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

//    void updateListView(ArrayList<Patient> data){
//        _adapter.setData(data);
//        _adapter.notifyDataSetChanged();
//    }

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
