package com.example.androidbtcontrol;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class PatientInfoActivity extends AppCompatActivity {

    PatientInfoAdapter _patientsAdapter;
    ListView _listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        _listView = (ListView) findViewById(R.id.listView);

        _patientsAdapter = new PatientInfoAdapter(this, R.layout.patient_info_list_row);
        _listView.setAdapter(_patientsAdapter);

        //_listView.setAdapter(_patientsAdapter);

        //String patientID = getIntent().getStringExtra("PATIENT_ID");
        TextView text = (TextView) findViewById(R.id.braceletID);
        //text.setText(patientID);
    }
}
