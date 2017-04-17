package BTservice;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by apeniazi on 11-Apr-17.
 */

public class BTservice implements BTserviceInterface {
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    ConcurrentHashMap<String, List<String>> _macToJsonList;
    TextView _textInfo;
    Context _context;
    Handler handler;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    String JsonMessage;
    ArrayList<BluetoothDevice> discoveredDevices;

    public BTservice(TextView info, Context context) {

        _context = context;
        _textInfo = info;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(_context,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();
        _textInfo.setText(stInfo);

        Toast.makeText(_context,
                "BTservice constructor!",
                Toast.LENGTH_SHORT).show();

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);


        handler = new Handler(context.getMainLooper());
        discoveredDevices = new ArrayList<BluetoothDevice>();
        _macToJsonList = new ConcurrentHashMap<String, List<String>>();
    }


    private void runOnUiThread(Runnable r) {
        handler.post(r);
    }
    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket, BluetoothDevice device){

        myThreadConnected = new ThreadConnected(socket, device);
        myThreadConnected.start();
    }

    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                _textInfo.setText("bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        _textInfo.setText("something wrong bluetoothSocket.connect(): \n" + eMessage);
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        _textInfo.setText("");
                        Toast.makeText(_context, msgconnected, Toast.LENGTH_LONG).show();

                        //listViewPairedDevice.setVisibility(View.GONE);
                        //inputPane.setVisibility(View.VISIBLE);
                    }
                });

                startThreadConnected(bluetoothSocket, bluetoothDevice);

                String startChar = "j";
                byte[] bytesToSend = startChar.getBytes();
                myThreadConnected.write(bytesToSend);
                byte[] NewLine = "\n".getBytes();
                myThreadConnected.write(NewLine);

            }else{
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(_context,
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /*
  ThreadConnected:
  Background Thread to handle Bluetooth data communication
  after connected
   */
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;
        private final BluetoothDevice device;


        public ThreadConnected(BluetoothSocket socket, BluetoothDevice btDevice) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;
            device = btDevice;
            JsonMessage = "";

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            String strRx = "";

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);
                    final String strByteCnt = String.valueOf(bytes) + " bytes received.\n";


                    if (strReceived.contains("#")) {
                        JsonMessage += strReceived;
                        JsonMessage = JsonMessage.substring(0, JsonMessage.length() - 3);
                        //tent.AddPatient(JsonMessage, device.getAddress());



//                        runOnUiThread(new Runnable(){
//                            @Override
//                            public void run() {
//                                _textInfo.setText(JsonMessage);
//
//
//                            }});
                        _macToJsonList.get(device.getAddress().toString()).add(JsonMessage);
                    }
                    else {
                        JsonMessage += strReceived;
                    }


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            _textInfo.setText(msgConnectionLost);
                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void startBT() {
        //Turn ON BlueTooth if it is OFF
     /*   if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        */
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        _context.registerReceiver(mReceiver, filter);
        bluetoothAdapter.startDiscovery();


        //_textInfo.setText("in start");
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Finding devices
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.add(device);

                Toast.makeText(_context,
                        "Found device " + device.getName(),
                        Toast.LENGTH_LONG).show();
                // Add the name and address to an array adapter to show in a ListView
                // mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(_context,
                        "Scanned devices number = " + discoveredDevices.size(),
                        Toast.LENGTH_LONG).show();
                setup();
            }
            else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                try {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int pin=intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234);
                    //the pin in case you need to accept for an specific pin
                    //Log.d(TAG, "Start Auto Pairing. PIN = " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",1234));
                    byte[] pinBytes;
                    pinBytes = (""+pin).getBytes("UTF-8");
                    device.setPin(pinBytes);
                    //setPairing confirmation if neeeded
                    device.setPairingConfirmation(true);
                } catch (Exception e) {
                    // Log.e(TAG, "Error occurs when trying to auto pair");
                    e.printStackTrace();
                }
            }
        }
    };

    private void setup() {

        for (BluetoothDevice device : discoveredDevices) {
            if (device.getName().toString().equals("HC-06")) {
                Toast.makeText(_context,
                        "got HC-06 ",
                        Toast.LENGTH_SHORT).show();
                myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                _macToJsonList.putIfAbsent(device.getAddress().toString(), Collections.synchronizedList(new ArrayList<String>()));
                myThreadConnectBTdevice.start();
            }
        }
    }

    public void destroy() {
        myThreadConnectBTdevice.cancel();
        _context.unregisterReceiver(mReceiver);
    }

    public ConcurrentHashMap<String, List<String>> getMacToJsonList() {
        return _macToJsonList;// TODO : consider deep copy
    }

    public void clearBtBuffers() {
        for (Map.Entry<String, List<String>> it : _macToJsonList.entrySet()) {
            it.getValue().clear();
        }
    }
}
