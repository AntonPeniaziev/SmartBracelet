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

class ConnectionManager extends Thread {
    private final BluetoothSocket connectedBluetoothSocket;
    private final InputStream connectedInputStream;
    private final OutputStream connectedOutputStream;
    private final BluetoothDevice device;
    private String _receivedMessage;
    private ConcurrentHashMap<String, List<String>> _macToReceivedBraceletData;
    private LinkedList<String> _onConnectionBroadcastList;
    private Context _context;
    private boolean breakLoop = false;
    private boolean receivedOldData = false;
    private Handler _handler;
    private static final int ANSWER_THRESHOLD = 1000;
    private long firstMessageTrTime;
    private String deviceAddr;

    //region constructor
    ConnectionManager(BluetoothSocket socket, BluetoothDevice btDevice,
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
        }

        connectedInputStream = in;
        connectedOutputStream = out;
    }
    //endregion constructor
    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;
        sendInitialData();

        while (true) {
            if (!receivedOldData && answerTimeout()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(_context,
                                "Response timeout " + device.getName() + "\nTry to reconnect.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                cancel();
                break;
            }
            if (breakLoop) {
                break;
            }
            try {
                bytes = connectedInputStream.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                if (!breakLoop) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(_context,
                                    "Lost connection with " + device.getAddress(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                cancel();
                break;
            }

            final String strReceived = new String(buffer, 0, bytes);
            handleBraceletMessage(strReceived);

        }
    }
    //region private
    private void handleBraceletMessage(String mes) {
        if (((!receivedOldData) && mes.contains("]")) ||
                ((receivedOldData) && mes.contains(">"))) {
            sendAck();
            _receivedMessage += mes;
            if (_macToReceivedBraceletData.containsKey(deviceAddr)) {
                _macToReceivedBraceletData.get(deviceAddr).add(_receivedMessage);
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
        }
    }
    //endregion private
    //region public
    void cancel() {
        if (breakLoop) {
            return;
        }
        breakLoop = true;
        try {
            connectedBluetoothSocket.close();
            _macToReceivedBraceletData.remove(device.getAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addInitialDataToSend(String data) {
        _onConnectionBroadcastList.add(data);
    }

    void writeString(String str) {
        byte[] bytesToSend = str.getBytes();
        write(bytesToSend);
    }

    boolean isWorking() {
        return !breakLoop;
    }
    //endregion public
}
