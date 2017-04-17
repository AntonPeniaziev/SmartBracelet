package com.example.androidbtcontrol;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class Bracelet {
    String _mac_address;
    List<JSONArray> JsonArrList;
    String jsonAsStr;

    public Bracelet(String jsonStr, String macAddress) {
        try {
            JsonArrList = Collections.synchronizedList(new ArrayList<JSONArray>());
            JsonArrList.add(new JSONArray(jsonStr));
        }
        catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        _mac_address = macAddress;
        jsonAsStr = new String();

    }

    public String getJsonAsStr () {
//        String res;
//        synchronized(JsonArrList) {
//            res = new String();
//            Iterator i = JsonArrList.iterator();
//            while (i.hasNext()) {
//                res += i.next().toString();
//            }
//
//        }
        return jsonAsStr;
    }

    public void AddActionsToBracelet(String jsonStr) {
        Log.d("AddActionsToBracelet",jsonStr);

        synchronized(JsonArrList) {
            jsonAsStr += jsonStr;
            try {
                JsonArrList.add(new JSONArray(jsonStr));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
