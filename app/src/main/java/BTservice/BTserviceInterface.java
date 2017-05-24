package BTservice;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    //need to be called in onDestroy()
    void destroy();
}
