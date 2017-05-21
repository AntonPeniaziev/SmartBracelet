package com.example.androidbtcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import BTservice.BTservice;

public class TentActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    TextView textInfo2;
    BTservice _bTservice;
    static Tent _tent;
    static public ConcurrentHashMap<String,String> TreatmensUidToName;
    UpdateData _updateData;
    CostumAdapter _adapter;
    ListView _listView;

    static public TreatmentsTable treatmentUidTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(this,
//               "Tent onCreate" ,
//                Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_tent);

        _tent = new Tent();
        textInfo2 = (TextView)findViewById(R.id.myView);

        _bTservice = new BTservice(textInfo2, this);

        _listView = (ListView) findViewById(android.R.id.list);

        _adapter = new CostumAdapter(this, R.layout.list_row);
        _listView.setAdapter(_adapter);
        _listView.setOnItemClickListener(this);

        String doc_id =  getIntent().getStringExtra("DOC_ID");
        _bTservice.addStartDataToSendToAll("<1," + doc_id + ">");
        _bTservice.startBT();


        _updateData = new UpdateData();
        _updateData.start();

        /** Will be updated from the web**/
        TreatmensUidToName = new ConcurrentHashMap<>();
        TreatmensUidToName.put("0","Tourniquet");
        TreatmensUidToName.put("10","Acamol");
        TreatmensUidToName.put("20","Israeli bandage");
        TreatmensUidToName.put("30","Hemostatic");
        TreatmensUidToName.put("40","Morphine");

        treatmentUidTranslator = new TreatmentsTable();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
              Patient item = _adapter.getItem(position);
//        Toast.makeText(this,
//               "beep sent to " + item.getBtMac().toString(),
//                Toast.LENGTH_SHORT).show();
//        _bTservice.addDataToBeSentByMac(item.getBtMac().toString(),"<6,0>");
        Intent intent = new Intent(TentActivity.this, PatientInfoActivity.class);
        intent.putExtra("PATIENT_ID", item.getBtMac().toString());

        startActivity(intent);

    }

    @Override
    protected void onStart() {
//        Toast.makeText(this,
//                "Tent onStart" ,
//                Toast.LENGTH_SHORT).show();
        super.onStart();



    }

    @Override
    protected void onDestroy() {
//        Toast.makeText(this,
//                "Tent onDestroy" ,
//                Toast.LENGTH_SHORT).show();
        super.onDestroy();
        _bTservice.destroy();
        if(_updateData!=null){
          //  updateData.cancel();
        }
    }

    @Override
    protected void onPause() {
//        Toast.makeText(this,
//                "Tent onPause" ,
//                Toast.LENGTH_SHORT).show();
        super.onPause();
    }

    @Override
    protected void onResume() {
//        Toast.makeText(this,
//                "Tent onResume" ,
//                Toast.LENGTH_SHORT).show();
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
        Intent setIntent = new Intent(TentActivity.this, LoginActivity.class);
        startActivity(setIntent);
    }

}


