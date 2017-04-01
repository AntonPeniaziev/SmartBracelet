package com.example.androidbtcontrol;
import org.json.JSONObject;
import org.json.JSONArray;




public class Bracelet {
    JSONObject _bracelet_json;
    String _mac_address;

    public Bracelet(String jsonStr, String macAddress) {
        try {
            _bracelet_json = new JSONObject(jsonStr);
        }
        catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        _mac_address = macAddress;

    }

    public String getJsonAsStr () {
        return _bracelet_json.toString();
    }
}
