package com.example.androidbtcontrol;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
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
    static private NfcAdapter mNfcAdapter;
    static final String MIME_TEXT_PLAIN = "text/plain";

    /**
     * Initiates the list of bracelets around
     */
    void initListOfBracelets() {
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
    void initRefreshButton() {
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        _refreshButton = (Button) findViewById(R.id.refreshBracelet);
        _refreshButton.setTypeface(army_font);
        setOnClickRefresh(_refreshButton);
        _refreshButton.setTypeface(army_font);
    }


    /**
     * Initiates the Log out Button located on the left top of the screen
     *
     */
    void initLogOutButton() {
        Typeface army_font = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        _logoutButton = (Button) findViewById(R.id.logOut);
        setOnClickLogOut(_logoutButton);
        _logoutButton = (Button) findViewById(R.id.logOut);
        _logoutButton.setTypeface(army_font);
    }


    /**
     * Initiates the Bluetooth service
     */
    void initBTService() {
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

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();

        }
        else {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "Please enable NFC", Toast.LENGTH_LONG).show();
            }
        }
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
            openPatientActivityByMac(item.getBtMac());
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
    void dialogAlert(String message, String title, DialogInterface.OnClickListener clickYes, DialogInterface.OnClickListener clickNo) {
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
        dialogAlert(message, title, clickYes ,clickNo);

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
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleNFCIntent(intent);
    }

    /**
     * dispatches NFC actions when tag is attached
     * @param intent
     */
    private void handleNFCIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    /**
     * fetches NDEF messages headers
     * @param msgs
     */
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        openPatientActivityByMac(text);
    }

    /**
     * Starts new PtientActivity
     * @param mac - patient's bracelet mac-address
     */
    private void openPatientActivityByMac(String mac) {
        if (false == _bTservice.isConnectedToBtMac(mac)) {
            Toast.makeText(this,
                    "Unknown device address\n",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(TentActivity.this, PatientInfoActivity.class);
        intent.putExtra("PATIENT_ID", mac);
        startActivity(intent);
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
    void updateListView(ArrayList<Patient> data) {
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
                LoginActivity._loginButton.setEnabled(true);
                finish();
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

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding equesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
}


