package BTservice;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by apeniazi on 12-Apr-17.
 */

public interface BTserviceInterface {
    void startBT();
    ConcurrentHashMap<String, Vector<String>> getMacToJsonList();
}
