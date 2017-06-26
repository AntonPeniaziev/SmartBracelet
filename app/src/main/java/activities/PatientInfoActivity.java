package activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ArduinoParsingUtils.ArduinoParsingUtils;
import logic.Bracelet;
import logic.Patient;
import logic.Tent;
import tasks.CallEvacuationTask;
import com.android.SmartBracelet.R;
import logic.Treatment;
import tasks.LogoutTask;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class PatientInfoActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    PatientInfoAdapter _patientsAdapter;
    ListView _listView;
    String _patientMac;
    TextView hr;
    UpdateData _updateData;
    Button _saveButton;
    Button _urgentButton;
    ImageButton _backButton;
    static PatientInfoActivity instance;
    Button[] _stateButtons;
    static final int NUMBER_OF_STATES = 5;
    String _currentState;


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
        _patientsAdapter.setMac(_patientMac);
        _listView.setAdapter(_patientsAdapter);
        _listView.setOnItemClickListener(this);
    }

    /**
     * Initiate the patient ID got from TentActivity
     * @param patientID
     */
    void initPatientID(String patientID) {
        TextView text = (TextView) findViewById(R.id.braceletID);
        Typeface army_font = Typeface.createFromAsset(getAssets(), "fonts/Assistant-Bold.ttf");
        text.setTypeface(army_font);
        text.setText(patientID);
    }

    /**
     * Initiate the Edit/Save button and its behavior
     */
    void initSaveButton(){
        _saveButton = (Button)findViewById(R.id.saveFile);
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        _saveButton.setTypeface(army_font);
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
                    //TentActivity.updateToWeb = true;
                    _saveButton.setText("Edit");
                    _patientsAdapter.setTextViewDisabled();
                    TentActivity.lock.lock();
                    try {
                        TentActivity.updateToWeb = true;
                    } finally {
                        TentActivity.lock.unlock();
                    }
                }
            }
        });
    }

    /**
     * Initiate the Urgent Evacuation button and its behavior
     */
    void initUrgentButton(){
        _urgentButton = (Button) findViewById(R.id.button4);
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        _urgentButton.setTypeface(army_font);
        _urgentButton.setClickable(true);
        _urgentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_urgentButton.getText().equals("Urgent Evacuation")){
                    String[] valAndMac = {String.valueOf(true), _patientMac};
                    new CallEvacuationTask(PatientInfoActivity.this).execute(valAndMac);
                    TentActivity.setEvacTime(System.currentTimeMillis(), _patientMac);
                    changeUrgant();
                    TentActivity.sendRecordToBracelet(_patientMac,  ArduinoParsingUtils.EVAC_SENT_RECORD);
                    TentActivity.editPatientEvacuation(true, _patientMac);
                    return;
                } else{
                    if(!TentActivity.evacuationCancelTimedout(_patientMac)) {
                        String message = "Cancel Evacuation?";
                        String title = "Smart Bracelet";
                        DialogInterface.OnClickListener clickYes = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String[] valAndMac = {String.valueOf(false), _patientMac};
                                new CallEvacuationTask(PatientInfoActivity.this).execute(valAndMac);
                                returnUrgant();
                                TentActivity.sendRecordToBracelet(_patientMac,  ArduinoParsingUtils.EVAC_CANCELED_RECORD);
                                TentActivity.editPatientEvacuation(false, _patientMac);
                            }
                        };

                        DialogInterface.OnClickListener clickNo = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        };

                        android.support.v7.app.AlertDialog.Builder dlgAlert  = new android.support.v7.app.AlertDialog.Builder(PatientInfoActivity.this);
                        dlgAlert.setMessage(message);
                        dlgAlert.setTitle(title);
                        dlgAlert.setPositiveButton("Yes",clickYes);
                        dlgAlert.setNegativeButton("No", clickNo);
                        dlgAlert.show();

                        return;

                    }

                    _urgentButton.setEnabled(false);
                    _urgentButton.setClickable(false);
                }
            }
        });
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

    void loadState(String state) {
        _currentState = state;
        switch (state) {
            case "Minor": _stateButtons[0].setBackgroundColor(Color.BLACK);
                            break;
            case "Moderate": _stateButtons[1].setBackgroundColor(Color.BLACK);
                            break;
            case "Severe": _stateButtons[2].setBackgroundColor(Color.BLACK);
                            break;
            case "Critical": _stateButtons[3].setBackgroundColor(Color.BLACK);
                            break;
            case "Dead": _stateButtons[4].setBackgroundColor(Color.BLACK);
        }
    }

    void sendStateToBracelet(String state) {

        switch (state){
            case "Minor": TentActivity.sendRecordToBracelet(_patientMac,  ArduinoParsingUtils.SEVERITY_MINOR_RECORD);
                break;
            case "Moderate": TentActivity.sendRecordToBracelet(_patientMac,  ArduinoParsingUtils.SEVERITY_MODERATE_RECORD);
                break;
            case "Severe": TentActivity.sendRecordToBracelet(_patientMac,  ArduinoParsingUtils.SEVERITY_SEVERE_RECORD);
                break;
            case "Critical": TentActivity.sendRecordToBracelet(_patientMac,  ArduinoParsingUtils.SEVERITY_CRITICAL_RECORD);
                break;
            case "Dead": TentActivity.sendRecordToBracelet(_patientMac,  ArduinoParsingUtils.SEVERITY_DEAD_RECORD);
        }
    }

    void initStateButtons(){
        _stateButtons = new Button[NUMBER_OF_STATES];
        _stateButtons[0] = (Button) findViewById(R.id.minorMode);
        _stateButtons[1] = (Button) findViewById(R.id.moderateMode);
        _stateButtons[2] = (Button) findViewById(R.id.severeMode);
        _stateButtons[3] = (Button) findViewById(R.id.criticalMode);
        _stateButtons[4] = (Button) findViewById(R.id.deadMode);

        for(int i=0; i< _stateButtons.length ; ++i){
            setOnClickState(_stateButtons[i]);
        }

        loadState(TentActivity.getPatientState(_patientMac));

    }

    void changePatientStateButton(String state){
        for(int i=0; i< _stateButtons.length ; ++i){
            if(!_stateButtons[i].getText().toString().equals(_currentState)){
                _stateButtons[i].setBackgroundColor(Color.parseColor("#4E5944"));
            }
        }
    }

    void setOnClickState(final Button stateButton){
        stateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Change Patient State to: " + stateButton.getText() + "?";
                String title = "Smart Bracelet";
                DialogInterface.OnClickListener clickYes = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String state = stateButton.getText().toString();
                        TentActivity.editPatientState(state, _patientMac);
                        //TentActivity.updateToWeb = true;
                        sendStateToBracelet(state);
                        loadState(state);
                        changePatientStateButton(state);
                        TentActivity.lock.lock();
                        try {
                            TentActivity.updateToWeb = true;
                        } finally {
                            TentActivity.lock.unlock();
                        }
                    }
                };

                DialogInterface.OnClickListener clickNo = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                };

                android.support.v7.app.AlertDialog.Builder dlgAlert  = new android.support.v7.app.AlertDialog.Builder(PatientInfoActivity.this);
                dlgAlert.setMessage(message);
                dlgAlert.setTitle(title);
                dlgAlert.setPositiveButton("Yes",clickYes);
                dlgAlert.setNegativeButton("No", clickNo);
                dlgAlert.show();

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
        if(TentActivity.getPatientUrgantEvacuation(_patientMac)){
            changeUrgant();
        }

        initStateButtons();
        TextView idOfPatient = (TextView) findViewById(R.id.PatientID);
        idOfPatient.setText("ID: " + TentActivity.getPatientPersonalNumber(_patientMac));
        Typeface army_font = Typeface.createFromAsset(getAssets(), "fonts/Assistant-Bold.ttf");
        idOfPatient.setTypeface(army_font);

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


    static public PatientInfoActivity getInstance() {
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
                            String result  = TentActivity.updateTreatment(_patientMac, treatmentName, newName);
                            if(!result.equals("")){
                                Toast.makeText(PatientInfoActivity.this, result, Toast.LENGTH_LONG).show();
                                return;
                            }
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
        _urgentButton.setTextColor(Color.parseColor("#D74C43"));

    }

    public void returnUrgant(){
        _urgentButton.setText("Urgent Evacuation");
        _urgentButton.setTextColor(Color.WHITE);
    }

}
