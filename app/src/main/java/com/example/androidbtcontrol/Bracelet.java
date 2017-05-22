package com.example.androidbtcontrol;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Bracelet {
    String _mac_address;
    ConcurrentHashMap<String, Treatment> _treatments;
    String jsonArray;
    long braceletStartTimeMinutes = 0;

    public Bracelet(String jsonStr, String macAddress) {

        _mac_address = macAddress;
        _treatments = new ConcurrentHashMap<String, Treatment>();

        braceletStartTimeMinutes = getArduinoStartTimeFromFirstData(jsonStr);

        AddActionsToBracelet(jsonStr);

    }



        public void AddActionsToBracelet(String jsonStr) {

            if (jsonStr.contains("[") && jsonStr.contains("]")) {
                String[] firstData = jsonStr.split("<");
                for (int i = 1; i < firstData.length; i++) {
                    String toAdd = "<" + firstData[i];

                    if (!getMessageType(toAdd).equals("0") || toAdd.contains("#")) {//TODO: cases # is sent
                        continue;
                    }


                    _treatments.put(getTimeField(toAdd) + "|" + getMessageTsID(toAdd),
                            new Treatment(getMessageTreatmentName(toAdd),
                                    "A", getMessageTime(toAdd)));
                }

                return;
            }

            //TODO different types of messages and origins
            if (!getMessageType(jsonStr).equals("0") || jsonStr.contains("#")) {
                return;
            }

            _treatments.put(getTimeField(jsonStr) + "|" + getMessageTsID(jsonStr),
                    new Treatment(getMessageTreatmentName(jsonStr),
                            "A", getMessageTime(jsonStr)));


    }

    private String getMessageType(String mes) {

        if (mes.split(",").length > 0 && mes.split(",")[0].split("<").length > 1) {
            return mes.split(",")[0].split("<")[1];
        }
        return "";
    }

    private String getMessageTime(String mes) {
        int arduinoMinutes = Integer.parseInt(mes.split(",")[1]);
        long resultMinutes = braceletStartTimeMinutes + arduinoMinutes;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(resultMinutes * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(calendar.getTime());
    }

    private String getTimeField(String mes) {
        return mes.split(",")[1];
    }
    private String getMessageTsID(String mes) {
        return mes.split(",")[2];
    }
    private String getMessageUID(String mes) {
        return mes.split(",")[3].split(">")[0];
    }

    private String getMessageTreatmentName(String mes) {
        if (false == TentActivity.TreatmensUidToName.containsKey(getMessageUID(mes))) {
            return "Unknown";
        }
        return TentActivity.TreatmensUidToName.get(getMessageUID(mes));
    }

    public ArrayList<Treatment> getTreatmentsArray() {
        return new ArrayList<Treatment>(_treatments.values());
    }

    private long getArduinoStartTimeFromFirstData(String mes) {
        long res = 0;
        Calendar c = Calendar.getInstance();
        long minutes = c.getTimeInMillis() / (60 * 1000);

        if (mes.contains("[") && mes.contains("]")) {
            String[] firstData = mes.split("<");

            res = minutes - Integer.parseInt(getTimeField("<" + firstData[firstData.length - 1]));
        }

        return res;
    }
}
