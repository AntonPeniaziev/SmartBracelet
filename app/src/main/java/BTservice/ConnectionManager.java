package BTservice;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import activities.TentActivity;

public class ConnectionManager extends Thread {
    private final BluetoothSocket connectedBluetoothSocket;
    private final InputStream connectedInputStream;
    private final OutputStream connectedOutputStream;
    private final BluetoothDevice device;
    private String _receivedMessage;
    private ConcurrentHashMap<String, List<String>> _macToReceivedBraceletData;
    private LinkedList<String> _onConnectionBroadcastList;
    private Context _context;
    private boolean breakLoop = false;
    boolean receivedOldData = false;
    private Handler _handler;
    private static final int ANSWER_THRESHOLD = 1000;
    private long firstMessageTrTime;
    private String deviceAddr;

    //region constructor
    public ConnectionManager(BluetoothSocket socket, BluetoothDevice btDevice,
                             ConcurrentHashMap<String, List<String>> macTotData,
                             Context context) {
        connectedBluetoothSocket = socket;
        InputStream in = null;
        OutputStream out = null;
        device = btDevice;
        deviceAddr = device.getAddress();
        _receivedMessage = "";
        _macToReceivedBraceletData = macTotData;
        _onConnectionBroadcastList = new LinkedList<>();
        _context = context;
        _handler = new Handler(context.getMainLooper());

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(_context,
                    "Connection trouble with " + device.getName(),
                    Toast.LENGTH_LONG).show();
            TentActivity.logger.writeToLog("\nIOException183" + e.getMessage() + "STACK = \n" + e.getStackTrace());
        }

        connectedInputStream = in;
        connectedOutputStream = out;
    }
    //endregion constructor
    @Override
    public void run() {
        TentActivity.logger.writeToLog("\nbeginning thread con run. breakLoop = " + breakLoop + "recOld = " + receivedOldData);
        byte[] buffer = new byte[1024];
        int bytes;
        sendInitialData();

        while (true) {
            if (receivedOldData == false && answerTimeout()) {
                TentActivity.logger.writeToLog("\nTIMEOUT!!!\n");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(_context,
                                "Response timeout " + device.getName() + "\nTry to reconnect.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            }
            if (breakLoop) {
                TentActivity.logger.writeToLog("\nBreaking thread loop\n");
                break;
            }
            try {
                bytes = connectedInputStream.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                TentActivity.logger.writeToLog("\nIOException212" + e.getMessage() + "STACK = \n" + e.getStackTrace());
                cancel();
                break;
            }

            final String strReceived = new String(buffer, 0, bytes);
            TentActivity.logger.writeToLog("\nstrReceived = " + strReceived + "|\n");
            handleBraceletMessage(strReceived);

        }
    }
    //region private
    private void handleBraceletMessage(String mes) {
        if (((false == receivedOldData) && mes.contains("]")) ||
                ((true == receivedOldData) && mes.contains(">"))) {
            sendAck();
            _receivedMessage += mes;
            TentActivity.logger.writeToLog("\nfinal message = " + _receivedMessage + "|\n");
            if (_macToReceivedBraceletData.containsKey(deviceAddr)) {
                _macToReceivedBraceletData.get(deviceAddr).add(_receivedMessage);
                TentActivity.logger.writeToLog("\nfinal message added to = " + deviceAddr + "|\n");
            }
            _receivedMessage = "";
            receivedOldData = true;
        } else {
            _receivedMessage += mes;
        }
    }

    private void write(byte[] buffer) {
        try {
            connectedOutputStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            TentActivity.logger.writeToLog("\nIOException251" + e.getMessage() + "STACK = \n" + e.getStackTrace());
        }
    }

    private void runOnUiThread(Runnable r) {
        _handler.post(r);
    }

    private void sendAck() {
        writeString("#");
    }

    private boolean answerTimeout() {
        return ((System.currentTimeMillis() - firstMessageTrTime) > ANSWER_THRESHOLD);
    }

    private void sendInitialData() {
        for (String mes : _onConnectionBroadcastList) {
            writeString(mes);
            firstMessageTrTime = System.currentTimeMillis();
            TentActivity.logger.writeToLog("\nwriting start message = " + mes + "|\n");
        }
    }
    //endregion private
    //region public
    public void cancel() {
        TentActivity.logger.writeToLog("\ncanceling thread = " + device.getAddress());
        if (breakLoop) {
            return;
        }
        breakLoop = true;
        try {
            connectedBluetoothSocket.close();
            _macToReceivedBraceletData.remove(device.getAddress());
        } catch (IOException e) {
            e.printStackTrace();
            TentActivity.logger.writeToLog("\nIOException274" + e.getMessage() + "STACK = \n" + e.getStackTrace());
        }
    }

    public void addInitialDataToSend(String data) {
        _onConnectionBroadcastList.add(data);
    }

    public void writeString(String str) {
        byte[] bytesToSend = str.getBytes();
        write(bytesToSend);
    }
    //endregion public
}
