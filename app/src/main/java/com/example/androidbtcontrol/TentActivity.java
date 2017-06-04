package com.example.androidbtcontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import BTservice.BTservice;

public class TentActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    static private BTservice _bTservice;
    static private Tent _tent;
    UpdateData _updateData;
    CustomAdapter _adapter;
    ListView _listView;
    Button _refreshButton, _logoutButton;
    static public TreatmentsTable treatmentUidTranslator;


    /**
     * Initiates the list of bracelets around
     */
    void initListOfBracelets(){
        _listView = (ListView) findViewById(android.R.id.list);
        _adapter = new CustomAdapter(this, R.layout.list_row);
        _listView.setAdapter(_adapter);
        _listView.setOnItemClickListener(this);
        _listView.setLongClickable(true);
        _listView.setOnItemLongClickListener(this);
    }


    /**
     * Initiates the refresh Button located on the right top of the screen
     */
    void initRefreshButton(){
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Army.ttf");
        _refreshButton = (Button) findViewById(R.id.refreshBracelet);
        _refreshButton.setTypeface(army_font);
        setOnClickRefresh(_refreshButton);
        _refreshButton.setTypeface(army_font);
    }


    /**
     * Initiates the Log out Button located on the left top of the screen
     *
     */
    void initLogOutButton(){
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Army.ttf");
        _logoutButton = (Button) findViewById(R.id.logOut);
        setOnClickLogOut(_logoutButton);
        _logoutButton = (Button) findViewById(R.id.logOut);
        _logoutButton.setTypeface(army_font);
    }


    /**
     * Initiates the Bluetooth service
     */
    void initBTService(){
        _bTservice = new BTservice(this);
        final String doc_id =  getIntent().getStringExtra("DOC_ID");
        _bTservice.addStartDataToSendToAll("<1," + doc_id + ">");
        _bTservice.startBT();
    }


    /**
     * The main OnCreate function. Initiates all the views on the screen
     * and the services running in the background.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tent);
        initListOfBracelets();
        initBTService();
        initRefreshButton();
        initLogOutButton();
        _tent = new Tent();
        _updateData = new UpdateData();
        _updateData.start();
        treatmentUidTranslator = new TreatmentsTable(TentActivity.this);
    }

    /**
     * when clicking on item in the list, moves to PatientInfoActivity with the necessary data
     * @param adapterView
     * @param view
     * @param position
     * @param l
     *
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Patient item = _adapter.getItem(position);
        if (item.isConnected()) {
            Intent intent = new Intent(TentActivity.this, PatientInfoActivity.class);
            intent.putExtra("PATIENT_ID", item.getBtMac());
            startActivity(intent);
        }
        else {
            _bTservice.connectByMac(item.getBtMac());
        }
    }

    /**
     * Creates Dialog Alert
     * @param message
     * @param title
     * @param clickYes
     * @param clickNo
     */
    void dialogAlert(String message, String title, DialogInterface.OnClickListener clickYes, DialogInterface.OnClickListener clickNo){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("Yes",clickYes);
        dlgAlert.setNegativeButton("No", clickNo);
        dlgAlert.show();
    }

    /**
     * when clicking long press on item in the list, creating a dialog alert
     * for disconnect it
     * @param arg0
     * @param arg1
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
        final Patient item = _adapter.getItem(position);
        final String itemAddress = item.getBtMac();
        String message = "Disconnect from " + itemAddress + "?";
        String title = "Bracelet " + itemAddress;
        DialogInterface.OnClickListener clickYes = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                _bTservice.disconnectByMac(itemAddress);
            }
        };
        DialogInterface.OnClickListener clickNo =  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        dialogAlert(message, title,clickYes ,clickNo);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _bTservice.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     * updates the data from web and Bluetooth
     */
    private class UpdateData extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                _tent.updatePatientInfoFromBT(_bTservice.getMacToReceivedDataMap(), true);
                _tent.updatePatientInfoFromBT(_bTservice.getDisconnecteListsdMap(), false);
                _bTservice.clearBtBuffers();
                new SendToMongodbTask(TentActivity.this).execute(_tent.getPatientsArray());

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

    /**
     * updates the list of bracelets
     * @param data
     */
    void updateListView(ArrayList<Patient> data){
        _adapter.setData(data);
        _adapter.notifyDataSetChanged();
    }

    /**
     * runs on background all the time. updates the list of bracelets
     */
    void runOnUI() {
        updateListView(_tent.getPatientsArray());
    }

    /**
     * get the Bluetooth service object
     * @return
     */
    BTservice getBt(){
        return _bTservice;
    }

    /**
     * function for the back pressed behavior
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * when back pressed, shows a dialog alert
     */
    @Override
    public void onBackPressed() {
        String message = "Logout?";
        String title = "Smart Bracelet";
        DialogInterface.OnClickListener clickYes = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        };

        DialogInterface.OnClickListener clickNo = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        dialogAlert(message, title, clickYes, clickNo);
    }

    /**
     * the behavior when clicking on refresh button: shows the list of bracelet around
     * @param btn
     */
    private void setOnClickRefresh(final Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bTservice.discover();
            }
        });
    }

    /**
     * the behavior when clicking on logout button: show a dialog alert
     * @param btn
     */
    private void setOnClickLogOut(final Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
        Sends an update record to bracelet and updates a treatment of specified patient in tent
        mac - bracelet mac address, treatment - treatment to update
        if newTreatmentName == null a specified treatment will be deleted
        in case of successful update an empty string will be returned
     **/
    static public String updateTreatment(String mac, Treatment treatment, String newTreatmentName) {

        String treatmentId = "10"; //TODO : need to be a function of newTreatmentName. i.e. 'Acamol' => "10" , null => dont care
//        if (not found) {
//            return "Specified treatment doesn't exist";
//        }

        _bTservice.addDataToBeSentByMac(mac, treatment.generateUpdateRecord(treatmentId, newTreatmentName));
        _tent.updatePatientsTreatment(mac, treatment, newTreatmentName);
        return "";
    }

    /**
     * get Heart Rate
     * @param patientID
     * @return
     */
    static public String getHeartrateByMac(String patientID){
        return _tent.getHeartrateByMac(patientID);
    }

    /**
     * get Treatments Array of specific patient
     * @param patientMac
     * @return
     */
    static public ArrayList<Treatment> getTreatmentsArrayByMac(String patientMac) {
        return _tent.getTreatmentsArrayByMac(patientMac);
    }

}


