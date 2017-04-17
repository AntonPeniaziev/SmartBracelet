package BTservice;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by apeniazi on 12-Apr-17.
 */

public interface BTserviceInterface {

    void startBT();

    ConcurrentHashMap<String, List<String>> getMacToJsonList();

    void clearBtBuffers();
    //need to be called in onDestory()
    void destroy();
}
