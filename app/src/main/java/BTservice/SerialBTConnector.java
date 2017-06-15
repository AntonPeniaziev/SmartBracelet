package BTservice;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.example.androidbtcontrol.TentActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SerialBTConnector extends Thread {
    private BluetoothSocket bluetoothSocket = null;
    private final BluetoothDevice bluetoothDevice;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    private Handler _handler;
    private Context _context;
    private ConcurrentHashMap<String, List<String>> _macToReceivedBraceletData;
    private LinkedList<String> _onConnectionBroadcastList;
    private HashMap<String, ConnectionManager> _connectionThreadsByMac;

    public SerialBTConnector(BluetoothDevice device, Context context,
                             HashMap<String, ConnectionManager> connectionThreadsByMac,
                             ConcurrentHashMap<String, List<String>> macTotData,
                             LinkedList<String> onConnectionBroadcastList) {
        bluetoothDevice = device;
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
        try {
            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            e.printStackTrace();
            TentActivity.logger.writeToLog("\nIOException85" + e.getMessage() + "STACK = \n" + e.getStackTrace());
        }
        _handler = new Handler(context.getMainLooper());
        _context = context;
        _macToReceivedBraceletData = macTotData;
        _onConnectionBroadcastList = onConnectionBroadcastList;
        _connectionThreadsByMac = connectionThreadsByMac;
    }

    @Override
    public void run() {
        boolean success = false;
        try {
            bluetoothSocket.connect();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
            TentActivity.logger.writeToLog("\nIOException97" + e.getMessage() + "STACK = \n" + e.getStackTrace());
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
                TentActivity.logger.writeToLog("\nIOException114" + e.getMessage() + "STACK = \n" + e.getStackTrace());
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
            TentActivity.logger.writeToLog("\nIOException152" + e.getMessage() + "STACK = \n" + e.getStackTrace());
        }

    }

    private void runOnUiThread(Runnable r) {
        _handler.post(r);
    }

    private void startThreadConnected(BluetoothSocket socket, BluetoothDevice device) {
        _connectionThreadsByMac.put(device.getAddress(), new ConnectionManager(socket, device,
                _macToReceivedBraceletData, _context));
        for (String dt:_onConnectionBroadcastList
                ) {
            _connectionThreadsByMac.get(device.getAddress()).addInitialDataToSend(dt);
        }
        TentActivity.logger.writeToLog("\nStarting connection with " + device.getAddress() + "\n");
        _connectionThreadsByMac.get(device.getAddress()).start();
    }
}
