package com.example.androidbtcontrol;


import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Tent {
    LinkedHashMap<String, Patient> _patients;

    public Tent() {
        _patients = new LinkedHashMap<String, Patient>();
    }

    public void AddPatient(String infoFromBracelet, String bt_mac) {

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
            res += "ID: ";
            res += pt.getValue().getId();
            res += "\n";
            res += "JSON: ";
            res += pt.getValue().getJson();
            res += "\n";
        }

        return res;
    }

}
