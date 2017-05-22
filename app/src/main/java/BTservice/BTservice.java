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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by apeniazi on 11-Apr-17.
 */

public class BTservice implements BTserviceInterface {
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter _bluetoothAdapter;
    private HashMap<String, String> _supportedDeviceNames;
    ConcurrentHashMap<String, List<String>> _macToJsonList;
    ConcurrentHashMap<String, List<String>> _macToDataForBracelet;
    HashMap<String, ThreadConnected> _ConnectionThreadsByMac;
    TextView _textInfo;
    Context _context;
    Handler _handler;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    String JsonMessage;
    ArrayList<BluetoothDevice> discoveredDevices;

    private String _startMessage = new String("");

    public BTservice(TextView info, Context context) {

        _context = context;
        _textInfo = info;
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (_bluetoothAdapter == null) {
            Toast.makeText(_context,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            return;
        }

        _supportedDeviceNames = new HashMap<>();
        _supportedDeviceNames.put("HC-06", "");
        _supportedDeviceNames.put("HC-05", "");
        _supportedDeviceNames.put("11", "");
        _supportedDeviceNames.put("gun1", "");

        String stInfo = _bluetoothAdapter.getName() + "\n" +
                _bluetoothAdapter.getAddress();
        _textInfo.setText(stInfo);

        Toast.makeText(_context,
                "BTservice is on",
                Toast.LENGTH_SHORT).show();

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);


        _handler = new Handler(context.getMainLooper());
        discoveredDevices = new ArrayList<BluetoothDevice>();
        _macToJsonList = new ConcurrentHashMap<String, List<String>>();
        _macToDataForBracelet = new ConcurrentHashMap<String, List<String>>();
        _ConnectionThreadsByMac = new HashMap<String, ThreadConnected>();
    }


    private void runOnUiThread(Runnable r) {
        _handler.post(r);
    }
    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket, BluetoothDevice device){
        _ConnectionThreadsByMac.put(device.getAddress().toString(), new ThreadConnected(socket, device));
        _ConnectionThreadsByMac.get(device.getAddress().toString()).start();
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
                    Toast.makeText(_context,
                            "Connection lost with " + bluetoothDevice.getName(),
                            Toast.LENGTH_LONG).show();
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

                    }
                });

                startThreadConnected(bluetoothSocket, bluetoothDevice);

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
                Toast.makeText(_context,
                        "Connection trouble with " + btDevice.getName(),
                        Toast.LENGTH_LONG).show();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            boolean receivedOldData = false;
            byte[] bytesToSend = _startMessage.getBytes();
            String deviceAddr = device.getAddress().toString();
            if (_ConnectionThreadsByMac.containsKey(deviceAddr)) {
                _ConnectionThreadsByMac.get(deviceAddr).write(bytesToSend);
            }

            while (true) {

                try {

                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);
                    final String strByteCnt = String.valueOf(bytes) + " bytes received.\n";


                    if (((false == receivedOldData) && strReceived.contains("]")) ||
                            ((true == receivedOldData) && strReceived.contains(">"))) {

                        JsonMessage += strReceived;

                        if (_macToJsonList.containsKey(deviceAddr)) {
                            _macToJsonList.get(deviceAddr).add(JsonMessage);
                        }
                        JsonMessage = "";
                        receivedOldData = true;
                    }
                    else {
                        JsonMessage += strReceived;
                    }


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();

                    _macToJsonList.remove(device.getAddress().toString());
                    _ConnectionThreadsByMac.remove(device.getAddress().toString());
                    _macToDataForBracelet.remove(device.getAddress().toString());

//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            Toast.makeText(_context,
//                                    msgConnectionLost,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });

                    this.interrupt();

                }
            }
        }

        private void write(byte[] buffer) {
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
        _bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Finding devices
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null &&
                        _supportedDeviceNames.containsKey(device.getName().toString())) {
                    discoveredDevices.add(device);
                    Toast.makeText(_context,
                            "Found supported device " + device.getName(),
                            Toast.LENGTH_LONG).show();
                }


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
            if (_supportedDeviceNames.containsKey(device.getName().toString())) {
                Toast.makeText(_context,
                        "got bracelet bluetooth " + device.getAddress().toString(),
                        Toast.LENGTH_SHORT).show();
                myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                _macToJsonList.putIfAbsent(device.getAddress().toString(), Collections.synchronizedList(new ArrayList<String>()));
                myThreadConnectBTdevice.start();
            }
        }
    }

    public void destroy() {
        if (myThreadConnectBTdevice != null) {
            myThreadConnectBTdevice.cancel();
        }
        _context.unregisterReceiver(mReceiver);
    }

    public ConcurrentHashMap<String, List<String>> getMacToJsonList() {
        return _macToJsonList;
    }

    public void clearBtBuffers() {
        for (Map.Entry<String, List<String>> it : _macToJsonList.entrySet()) {
            it.getValue().clear();
        }
    }

    public void addDataToBeSentByMac(String mac, String data) {
        _macToDataForBracelet.putIfAbsent(mac, Collections.synchronizedList(new ArrayList<String>()));
        if (_macToJsonList.containsKey(mac)) {
            _macToDataForBracelet.get(mac).add(data);
        }
        writeToMac(mac);
    }

    public void addStartDataToSendToAll(String data) {
        _startMessage = data;
//        for (String mac : _ConnectionThreadsByMac.keySet()) {
//            addDataToBeSentByMac(mac, data);
//        }
    }

    private void writeToMac(String mac) {
        //TODO perfomance
        if (_macToDataForBracelet.size() > 0) {
            if (_macToDataForBracelet.containsKey(mac)) {
                synchronized (_macToDataForBracelet.get(mac)) {
                    Iterator i = _macToDataForBracelet.get(mac).iterator();
                    while (i.hasNext()) {
                        byte[] toMac = i.next().toString().getBytes();
                        if (_ConnectionThreadsByMac.containsKey(mac)) {
                            _ConnectionThreadsByMac.get(mac).write(toMac);
                        }
                    }
                    if (_macToDataForBracelet.containsKey(mac)) {
                        _macToDataForBracelet.get(mac).clear();
                    }
                }
            }
        }
    }

}
