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
    static Tent _tent; // TODO change to private and add access methods
    UpdateData _updateData;
    CustomAdapter _adapter;
    ListView _listView;
    Button _refreshButton, _logoutButton;
    static public TreatmentsTable treatmentUidTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tent);

        _tent = new Tent();
        _bTservice = new BTservice(this);
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Army.ttf");
        _listView = (ListView) findViewById(android.R.id.list);
        _refreshButton = (Button) findViewById(R.id.refreshBracelet);
        _logoutButton = (Button) findViewById(R.id.logOut);
        _refreshButton.setTypeface(army_font);
        setOnClickRefresh(_refreshButton);

        _logoutButton = (Button) findViewById(R.id.logOut);
        _refreshButton.setTypeface(army_font);
        _logoutButton.setTypeface(army_font);
        _adapter = new CustomAdapter(this, R.layout.list_row);
        _listView.setAdapter(_adapter);
        _listView.setOnItemClickListener(this);
        _listView.setLongClickable(true);
        _listView.setOnItemLongClickListener(this);

        final String doc_id =  getIntent().getStringExtra("DOC_ID");
        _bTservice.addStartDataToSendToAll("<1," + doc_id + ">");
        _bTservice.startBT();

        _updateData = new UpdateData();
        _updateData.start();
        treatmentUidTranslator = new TreatmentsTable(TentActivity.this);

    }

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

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
        final Patient item = _adapter.getItem(position);
        final String itemAddress = item.getBtMac();
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Disconnect from " + itemAddress + "?");
        dlgAlert.setTitle("Bracelet " + itemAddress);
        dlgAlert.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        _bTservice.disconnectByMac(itemAddress);
                    }
                });

        dlgAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        dlgAlert.show();

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

    void updateListView(ArrayList<Patient> data){
        _adapter.setData(data);
        _adapter.notifyDataSetChanged();
    }

    void runOnUI() {
        updateListView(_tent.getPatientsArray());
    }

    BTservice getBt(){
        return _bTservice;
    }

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


    @Override
    public void onBackPressed() {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Logout?");
        dlgAlert.setTitle("Smart Bracelet");
        dlgAlert.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });

        dlgAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        dlgAlert.show();
    }

    private void setOnClickRefresh(final Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _bTservice.discover();
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


}


