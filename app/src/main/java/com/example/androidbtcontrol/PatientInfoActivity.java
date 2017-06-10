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

    /**
     * Initiates the list of treatments of specific patient
     */
    void initListOfTreatments(){
        _listView = (ListView) findViewById(R.id.listView);
        _patientsAdapter = new PatientInfoAdapter(this, R.layout.patient_info_list_row);
        _listView.setAdapter(_patientsAdapter);
    }

    /**
     * Initiate the patient ID got from TentActivity
     * @param patientID
     */
    void initPatientID(String patientID) {
        TextView text = (TextView) findViewById(R.id.braceletID);
        Typeface army_font = Typeface.createFromAsset(getAssets(), "fonts/Army.ttf");
        text.setTypeface(army_font);
        text.setText(patientID);

    }

    /**
     * Initiate the Edit/Save button and its behavior
     */
    void initSaveButton(){
        _saveButton = (Button)findViewById(R.id.saveFile);
        _saveButton.setClickable(true);
        _saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _patientsAdapter.setEditTextEnabled();
            }
        });
    }

    /**
     * Initiate the Urgent Evacuation button and its behavior
     */
    void initUrgentButton(){
        _urgentButton = (Button) findViewById(R.id.button4);
        _urgentButton.setClickable(true);
    }

    /**
     * Initiate the heart rate of the patient
     * @param patientID
     */
    void initHeartRate(String patientID){
        hr = (TextView) findViewById(R.id.heartRate);
        hr.setText(TentActivity.getHeartrateByMac(patientID));
    }

    /**
     * Initiate the Back button located right top on the screen and its behavior
     */
    void initBackButton(){
        _backButton = (ImageButton) findViewById(R.id.back_button);
        _backButton.setClickable(true);
        _backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * main OnCreate function. initiates the views on the activity and the background services
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        String patientID = getIntent().getStringExtra("PATIENT_ID");
        _patientMac = patientID;
        initListOfTreatments();
        initPatientID(patientID);
        initSaveButton();
        initUrgentButton();
        initHeartRate(patientID);
        initBackButton();

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUI();
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    void runOnUI() {
        hr.setText(TentActivity.getHeartrateByMac(_patientMac));
        //TODO : pressure, breath ..
        updateListView(TentActivity.getTreatmentsArrayByMac(_patientMac));
    }

    void updateListView(ArrayList<Treatment> treatmentsArr){
        _patientsAdapter.setData(treatmentsArr);
        _patientsAdapter.notifyDataSetChanged();
    }
}
