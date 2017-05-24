package BTservice;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
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

public class BTservice implements BTserviceInterface {
    private BluetoothAdapter _bluetoothAdapter;
    private HashMap<String, String> _supportedDeviceNames;
    private ConcurrentHashMap<String, List<String>> _macToReceivedBraceletData;
    private ConcurrentHashMap<String, List<String>> _macToDataForBracelet;
    private HashMap<String, ThreadConnected> _connectionThreadsByMac;
    private Context _context;
    private Handler _handler;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    private ThreadConnectBTdevice _myThreadConnectBTdevice;
    private String _receivedMessage;
    private ArrayList<BluetoothDevice> _discoveredDevices;
    private String _startMessage;
//region BTservice constructor
    public BTservice(Context context) {

        _context = context;
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

        Toast.makeText(_context,
                "BTservice is on",
                Toast.LENGTH_SHORT).show();

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        _handler = new Handler(context.getMainLooper());
        _discoveredDevices = new ArrayList<>();
        _macToReceivedBraceletData = new ConcurrentHashMap<>();
        _macToDataForBracelet = new ConcurrentHashMap<>();
        _connectionThreadsByMac = new HashMap<String, ThreadConnected>();
        _startMessage = "";
    }
//endregion BTservice constructor

//region ThreadConnectBTdevice
    private class ThreadConnectBTdevice extends Thread {
        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;

        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;
            try {
                bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
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
                        Toast.makeText(_context,
                                "something wrong bluetoothSocket.connect(): \n" + eMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
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
//endregion ThreadConnectBTdevice

//region ThreadConnected
//Background Thread to handle Bluetooth data communication
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
            _receivedMessage = "";

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
            String deviceAddr = device.getAddress();
            if (_connectionThreadsByMac.containsKey(deviceAddr)) {
                _connectionThreadsByMac.get(deviceAddr).write(bytesToSend);
            }

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);

                    if (((false == receivedOldData) && strReceived.contains("]")) ||
                            ((true == receivedOldData) && strReceived.contains(">"))) {

                        _receivedMessage += strReceived;

                        if (_macToReceivedBraceletData.containsKey(deviceAddr)) {
                            _macToReceivedBraceletData.get(deviceAddr).add(_receivedMessage);
                        }
                        _receivedMessage = "";
                        receivedOldData = true;
                    }
                    else {
                        _receivedMessage += strReceived;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    _macToReceivedBraceletData.remove(device.getAddress());
                    _connectionThreadsByMac.remove(device.getAddress());
                    _macToDataForBracelet.remove(device.getAddress());
                    this.interrupt();
                }
            }
        }

        private void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
//endregion ThreadConnected

//region private internal
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Finding devices
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null &&
                        _supportedDeviceNames.containsKey(device.getName())) {
                    _discoveredDevices.add(device);
                    Toast.makeText(_context,
                            "Found supported device " + device.getName(),
                            Toast.LENGTH_LONG).show();
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(_context,
                        "Scanned devices number = " + _discoveredDevices.size(),
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
                    e.printStackTrace();
                }
            }
        }
    };

    private void setup() {
        for (BluetoothDevice device : _discoveredDevices) {
            if (_supportedDeviceNames.containsKey(device.getName())) {
                Toast.makeText(_context,
                        "got bracelet bluetooth " + device.getAddress(),
                        Toast.LENGTH_SHORT).show();
                _myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                _macToReceivedBraceletData.putIfAbsent(device.getAddress(), Collections.synchronizedList(new ArrayList<String>()));
                _myThreadConnectBTdevice.start();
            }
        }
    }

    private void runOnUiThread(Runnable r) {
        _handler.post(r);
    }
    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket, BluetoothDevice device){
        _connectionThreadsByMac.put(device.getAddress(), new ThreadConnected(socket, device));
        _connectionThreadsByMac.get(device.getAddress()).start();
    }

    private void writeToMac(String mac) {
        if (_macToDataForBracelet.size() > 0) {
            if (_macToDataForBracelet.containsKey(mac)) {
                synchronized (_macToDataForBracelet.get(mac)) {
                    Iterator i = _macToDataForBracelet.get(mac).iterator();
                    while (i.hasNext()) {
                        byte[] toMac = i.next().toString().getBytes();
                        if (_connectionThreadsByMac.containsKey(mac)) {
                            _connectionThreadsByMac.get(mac).write(toMac);
                        }
                    }
                    if (_macToDataForBracelet.containsKey(mac)) {
                        _macToDataForBracelet.get(mac).clear();
                    }
                }
            }
        }
    }
//endregion private internal

//region public methods
    public void startBT() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        _context.registerReceiver(mReceiver, filter);
        _bluetoothAdapter.startDiscovery();
    }

    public void destroy() {
        if (_myThreadConnectBTdevice != null) {
            _myThreadConnectBTdevice.cancel();
        }
        _context.unregisterReceiver(mReceiver);
    }

    public ConcurrentHashMap<String, List<String>> getMacToReceivedDataMap() {
        return _macToReceivedBraceletData;
    }

    public void clearBtBuffers() {
        for (Map.Entry<String, List<String>> it : _macToReceivedBraceletData.entrySet()) {
            it.getValue().clear();
        }
    }

    public void addDataToBeSentByMac(String mac, String data) {
        _macToDataForBracelet.putIfAbsent(mac, Collections.synchronizedList(new ArrayList<String>()));
        if (_macToReceivedBraceletData.containsKey(mac)) {
            _macToDataForBracelet.get(mac).add(data);
        }
        writeToMac(mac);
    }

    public void addStartDataToSendToAll(String data) {
        _startMessage = data;
    }
//endregion public methods

}
