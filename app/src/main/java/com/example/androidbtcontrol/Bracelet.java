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
    List<JSONObject> JsonArrList;
    String jsonAsStr;
    String jsonArray;

    TreatmentsTable tTable; //hash table to translate codes to treatments name and types

    public Bracelet(String jsonStr, String macAddress) {

        tTable = new TreatmentsTable();
        JsonArrList = Collections.synchronizedList(new ArrayList<JSONObject>());
        JsonArrList.add(ArduinoFormatToJson(jsonStr));

        _mac_address = macAddress;
        jsonAsStr = new String();
        jsonArray = new String();

        jsonArray = "[" + ArduinoFormatToJson(jsonStr).toString() + "]";
        //jsonAsStr = jsonStr;

        //JsonArrList = new List<JSONArray>();

    }

    private JSONObject ArduinoFormatToJson(String mes) {
        String JsonString = "{\"uid\": \"" + mes + "\",\"ts\": \"0\",\"tsid\": \"0\"}";
        JSONObject JsonResult = null;
        try {
            JsonResult = new JSONObject(JsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return JsonResult;
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
        return jsonArray;
    }

    public void AddActionsToBracelet(String jsonStr) {

        jsonAsStr += jsonStr;
        StringBuilder ArrayResult = new StringBuilder(jsonArray);
        ArrayResult.insert(jsonArray.length() - 1, "," + ArduinoFormatToJson(jsonStr).toString());
        jsonArray = ArrayResult.toString();

        synchronized(JsonArrList) {

                JsonArrList.add(ArduinoFormatToJson(jsonStr));

        }

    }
}
