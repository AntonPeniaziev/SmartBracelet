package com.example.androidbtcontrol;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Bracelet {
    String _mac_address;
    List<JSONObject> JsonArrList;
    ConcurrentHashMap<String, Treatment> _treatments;
    String jsonAsStr;
    String jsonArray;

    public Bracelet(String jsonStr, String macAddress) {

        JsonArrList = Collections.synchronizedList(new ArrayList<JSONObject>());
        JsonArrList.add(ArduinoFormatToJson(jsonStr));

        _mac_address = macAddress;
        jsonAsStr = new String();
        jsonArray = new String();
        _treatments = new ConcurrentHashMap<String, Treatment>();

        jsonArray = "[" + ArduinoFormatToJson(jsonStr).toString() + "]";
        //jsonAsStr = jsonStr;

        //JsonArrList = new List<JSONArray>();

    }

    private JSONObject ArduinoFormatToJson(String mes) {
        String JsonString = "{\"uid\": \"" + getMessageTreatmentName(mes) + "\",\"ts\": \"" + getMessageTime(mes) + "\",\"tsid\": \"" + getMessageTsID(mes) + "\"}";
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

        //TODO different types of messages and origins
        if (!getMessageType(jsonStr).equals("0")) {
            return;
        }
        jsonAsStr += jsonStr;
        StringBuilder ArrayResult = new StringBuilder(jsonArray);
        ArrayResult.insert(jsonArray.length() - 1, "," + ArduinoFormatToJson(jsonStr).toString());
        jsonArray = ArrayResult.toString();

        _treatments.put(getMessageTime(jsonStr) + "|" + getMessageTsID(jsonStr),
                new Treatment(getMessageTreatmentName(jsonStr),
                        "A", getMessageTime(jsonStr)));

        synchronized(JsonArrList) {

                JsonArrList.add(ArduinoFormatToJson(jsonStr));

        }

    }

    private String getMessageType(String mes) {
        return mes.split(",")[0].split("<")[1];
    }
    private String getMessageTime(String mes) {
        return mes.split(",")[1];
    }
    private String getMessageTsID(String mes) {
        return mes.split(",")[2];
    }
    private String getMessageUID(String mes) {
        return mes.split(",")[3].split(">")[0];
    }

    private String getMessageTreatmentName(String mes) {
        return TentActivity.TreatmensUidToName.get(getMessageUID(mes));
    }

    public ArrayList<Treatment> getTreatmentsArray() {
        return new ArrayList<Treatment>(_treatments.values());
    }
}
