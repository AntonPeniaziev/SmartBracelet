package BTservice;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BTservice implements BTserviceInterface {
    private BluetoothAdapter _bluetoothAdapter;
    private HashMap<String, String> _supportedDeviceNames;
    private ConcurrentHashMap<String, List<String>> _macToReceivedBraceletData;
    private LinkedList<String> _onConnectionBroadcastList;
    private ConcurrentHashMap<String, ConnectionManager> _connectionThreadsByMac;
    private Context _context;
    private SerialBTConnector _myThreadConnectBTdevice;
    private ArrayList<BluetoothDevice> _discoveredDevices;

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

        _discoveredDevices = new ArrayList<>();
        _macToReceivedBraceletData = new ConcurrentHashMap<>();
        _connectionThreadsByMac = new ConcurrentHashMap<String, ConnectionManager>();
        _onConnectionBroadcastList = new LinkedList<>();
    }
//endregion BTservice constructor


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
                            Toast.LENGTH_SHORT).show();
                }
            }
            //can add else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
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

    private void cleanupNonWorkingThreads () {
        for (String mac : _connectionThreadsByMac.keySet()
                ) {
            if (!_connectionThreadsByMac.get(mac).isWorking()) {
                _connectionThreadsByMac.remove(mac);
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
        cleanupNonWorkingThreads();
        return _macToReceivedBraceletData;
    }

    public void clearBtBuffers() {
        for (Map.Entry<String, List<String>> it : _macToReceivedBraceletData.entrySet()) {
            it.getValue().clear();
        }
    }

    public void addDataToBeSentByMac(String mac, String data) {
        if (_connectionThreadsByMac.containsKey(mac)) {
            _connectionThreadsByMac.get(mac).writeString(data);
        }
    }

    public void addStartDataToSendToAll(String data) {
        _onConnectionBroadcastList.add(data);
    }

    public void disconnectByMac(String mac) {
        _macToReceivedBraceletData.remove(mac);
        if (_connectionThreadsByMac.containsKey(mac)) {
            _connectionThreadsByMac.get(mac).interrupt();
            _connectionThreadsByMac.get(mac).cancel();
        }
        _connectionThreadsByMac.remove(mac);
    }

    public void connectByMac(String mac) {
        if(_bluetoothAdapter.isDiscovering()) {
           _bluetoothAdapter.cancelDiscovery();
       }
        BluetoothDevice device = _bluetoothAdapter.getRemoteDevice(mac);
        if(_discoveredDevices.contains(device)) {
            _discoveredDevices.remove(device);
            if (_supportedDeviceNames.containsKey(device.getName())) {
                _myThreadConnectBTdevice = new SerialBTConnector(device, _context,
                        _connectionThreadsByMac, _macToReceivedBraceletData,
                        _onConnectionBroadcastList);
                _macToReceivedBraceletData.put(device.getAddress(), Collections.synchronizedList(new ArrayList<String>()));
                _myThreadConnectBTdevice.start();
            }
        }
    }

    public ConcurrentHashMap<String, List<String>> getDisconnecteListsdMap() {
        ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<>();
        for (BluetoothDevice device : _discoveredDevices) {
            map.put(device.getAddress(), Collections.synchronizedList(new ArrayList<String>()));
            map.get(device.getAddress()).add("");
        }
        return map;
    }

    public void discover() {
        if (_bluetoothAdapter.isDiscovering()) {
            _bluetoothAdapter.cancelDiscovery();
        }
        _discoveredDevices.clear();
        _bluetoothAdapter.startDiscovery();
    }

    public boolean isConnectedToBtMac (String mac) {
        return _connectionThreadsByMac.containsKey(mac);
    }
//endregion public methods
}
