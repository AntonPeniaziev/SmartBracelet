package com.example.androidbtcontrol;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Tent {
    LinkedHashMap<String, Patient> _patients;//TODO: sync

    public Tent() {
        _patients = new LinkedHashMap<String, Patient>();
    }

    private void AddPatientInfo(String infoFromBracelet, String bt_mac) {

        if (_patients.containsKey(bt_mac)) {
            _patients.get(bt_mac).AddActions(infoFromBracelet);
        }
        else {
            _patients.put(bt_mac, new Patient(infoFromBracelet, bt_mac));
        }
    }

    public String getAllIds() {
        String res = "QQQ";
        for (Map.Entry<String, Patient> pt : _patients.entrySet()
             ) {

            res += "\nJSON: ";
            res += pt.getValue().getJson();
            res += "\n";
        }

        return res;
    }

    public String[] getDataBase(){
        int _size = _patients.size();
        String[] userNames = new String[_size];
        int index = 0;
        for(String name : _patients.keySet()){
            userNames[index] = name;
            index++;
        }
        return userNames;
    }

    public void updatePatientInfoFromBT(ConcurrentHashMap<String, List<String>> macToJsonList) {
        for (Map.Entry<String, List<String>> it : macToJsonList.entrySet()) {
            synchronized(it.getValue()) {
                Iterator i = it.getValue().iterator();
                while (i.hasNext()) {
                    String jsonStr = new String(i.next().toString());
                    AddPatientInfo(jsonStr, it.getKey());
                }
            }

        }
    }

}
