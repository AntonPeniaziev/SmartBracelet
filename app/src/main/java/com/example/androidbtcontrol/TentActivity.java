package com.example.androidbtcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import BTservice.BTservice;

public class TentActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    TextView textInfo2;
    BTservice _bTservice;
    Tent _tent;
    UpdateData _updateData;
    CostumAdapter _adapter;
    ListView _listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tent);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        _tent = new Tent();
        textInfo2 = (TextView)findViewById(R.id.myView);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        _bTservice = new BTservice(textInfo2, this);

        _listView = (ListView) findViewById(android.R.id.list);

        _adapter = new CostumAdapter(this, R.layout.list_row);
        _listView.setAdapter(_adapter);
        _listView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
              Patient item = _adapter.getItem(position);
//        Toast.makeText(this,
//               "beep sent to " + item.getBtMac().toString(),
//                Toast.LENGTH_SHORT).show();
//        _bTservice.addDataToBeSentByMac(item.getBtMac().toString(),"<6,0>");
        Intent intent = new Intent(TentActivity.this, PatientInfoActivity.class);
        //intent.putExtra("PATIENT_ID", item.getBtMac().toString());

        startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        _bTservice.startBT();
        _updateData = new UpdateData();
        _updateData.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _bTservice.destroy();
        if(_updateData!=null){
          //  updateData.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //_updateData.run();
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

    /**
     * updates the List View on Tent Avtivity.
     * @param data
     */
    void updateListView(ArrayList<Patient> data){
        _adapter.setData(data);
        _adapter.notifyDataSetChanged();

        //_listView.setAdapter(_adapter);



    }

    void runOnUI() {

        /**
         * Uncomment the code below to test without physical Bluetooth devices
         * AddPatientInfo should be changed back to be private before release
         */
//        _tent.AddPatientInfo("[{\"uid\": \"111\",\"ts\": \"1\",\"tsid\": \"1\"},{\"uid\": \"111\",\"ts\": \"1\",\"tsid\": \"1\"}, {\"uid\": \"111\",\"ts\": \"1\",\"tsid\": \"1\"}]#", "mac1");
//        _tent.AddPatientInfo("[{\"uid\": \"22\",\"ts\": \"0\",\"tsid\": \"0\"},{\"uid\": \"22 1\",\"ts\": \"1\",\"tsid\": \"1\"}]#", "MAC2");

        /**************************************************************************/
        updateListView(_tent.getPatientsArray());

    }

    BTservice getBt(){
        return _bTservice;
    }

}


