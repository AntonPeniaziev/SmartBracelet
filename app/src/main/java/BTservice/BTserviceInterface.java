package BTservice;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * List of BTservice package functions available for user-code
 * This interface created for documentation purpose only
 */
public interface BTserviceInterface {
    //starts BT scanning,pairing and connection, connection maintaining threads.
    void startBT();
    //returns hashmap of received data from bracelets
    ConcurrentHashMap<String, List<String>> getMacToReceivedDataMap();
    //adds data to be sent to bluetooth by specified mac address
    void addDataToBeSentByMac(String mac, String data);
    //adds initial data that will be sent to every device after connection established
    void addStartDataToSendToAll(String data);
    //clears all received data buffers
    void clearBtBuffers();
    //disconnects from bracelet bluetooth
    void disconnectByMac(String mac);
    //connects to bracelet bluetooth
    void connectByMac(String mac);
    //returns a list of disconnected bt devices
    ConcurrentHashMap<String, List<String>> getDisconnecteListsdMap();
    //starts bluetooth discovery
    void discover();
    //check whether serial bluetooth with specified mac is currently connected
    boolean isConnectedToBtMac (String mac);
    //need to be called in onDestroy()
    void destroy();
}
