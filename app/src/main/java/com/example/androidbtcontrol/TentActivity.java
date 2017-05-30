package com.example.androidbtcontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import BTservice.BTservice;

public class TentActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    BTservice _bTservice;
    static Tent _tent;
    UpdateData _updateData;
    CustomAdapter _adapter;
    ListView _listView;

    static public TreatmentsTable treatmentUidTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tent);

        _tent = new Tent();
        _bTservice = new BTservice(this);

        _listView = (ListView) findViewById(android.R.id.list);

        _adapter = new CustomAdapter(this, R.layout.list_row);
        _listView.setAdapter(_adapter);
        _listView.setOnItemClickListener(this);

        String doc_id =  getIntent().getStringExtra("DOC_ID");
        _bTservice.addStartDataToSendToAll("<1," + doc_id + ">");
        _bTservice.startBT();

        _updateData = new UpdateData();
        _updateData.start();
        treatmentUidTranslator = new TreatmentsTable(TentActivity.this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
              Patient item = _adapter.getItem(position);
        Intent intent = new Intent(TentActivity.this, PatientInfoActivity.class);
        intent.putExtra("PATIENT_ID", item.getBtMac().toString());

        startActivity(intent);

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
                _tent.updatePatientInfoFromBT(_bTservice.getMacToReceivedDataMap());
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

}


