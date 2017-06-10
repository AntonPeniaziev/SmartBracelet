package com.example.androidbtcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class PatientInfoActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    PatientInfoAdapter _patientsAdapter;
    ListView _listView;
    String _patientMac;
    TextView hr;
    UpdateData _updateData;
    Button _saveButton;
    Button _urgentButton;
    ImageButton _backButton;
    PatientInfoActivity instance;



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Treatment item = _patientsAdapter.getItem(position);
        createDialogEditTreatment(item, position);
    }
    /**
     * Initiates the list of treatments of specific patient
     */
    void initListOfTreatments(){
        _listView = (ListView) findViewById(R.id.listView);
        _patientsAdapter = new PatientInfoAdapter(this, R.layout.patient_info_list_row);
        _patientsAdapter.setDiseable=true;
        _listView.setAdapter(_patientsAdapter);
        _listView.setOnItemClickListener(this);

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
                if(_saveButton.getText().equals("Edit")){
                    _saveButton.setText("Save");
                    _patientsAdapter.setTextViewEnabled();
                    return;
                }
                if(_saveButton.getText().equals("Save")){
                    _saveButton.setText("Edit");
                    _patientsAdapter.setTextViewDisabled();
                }
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
        instance = this;

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





    public PatientInfoActivity getInstance() {
        return instance;
    }

    public void createDialogEditTreatment(final Treatment treatmentName, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View view  = inflater.inflate(R.layout.costum_dialog, null);
        EditText editText = (EditText)view.findViewById(R.id.treatment_edited);
        editText.setText(treatmentName.getName());
        builder.setTitle("Edit Treatment");
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editedText = (EditText)((AlertDialog) dialog).findViewById(R.id.treatment_edited);
                        String newName = editedText.getText().toString();
                        if(!newName.equals("") && !newName.equals(treatmentName.getName())) {
                            TentActivity.updateTreatment(_patientMac, treatmentName, newName);
                            _patientsAdapter.setDiseable=false;
                            _patientsAdapter.notifyDataSetChanged();
                            _listView.deferNotifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

       builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        _patientsAdapter.notifyDataSetChanged();
    }

    public void changeUrgant(){
        _urgentButton.setText("Evacuation Sent");
        _urgentButton.setEnabled(false);
    }



}
