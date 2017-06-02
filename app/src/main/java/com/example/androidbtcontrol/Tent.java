package com.example.androidbtcontrol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Tent {
    ConcurrentHashMap<String, Patient> _patients;

    public Tent() {
        _patients = new ConcurrentHashMap<>();
    }

    private void AddPatientInfo(String infoFromBracelet, String bt_mac) {

        if (_patients.containsKey(bt_mac)) {
            _patients.get(bt_mac).AddActions(infoFromBracelet);
        }
        else {
            _patients.put(bt_mac, new Patient(infoFromBracelet, bt_mac));
        }
    }

//region public methods
    public void updatePatientInfoFromBT(ConcurrentHashMap<String, List<String>> macToJsonList, boolean connected) {

        for (Map.Entry<String, List<String>> it : macToJsonList.entrySet()) {
            synchronized(it.getValue()) {
                Iterator i = it.getValue().iterator();
                while (i.hasNext()) {
                    String jsonStr = new String(i.next().toString());
                    AddPatientInfo(jsonStr, it.getKey());
                    _patients.get(it.getKey()).setConnected(connected);
                }
            }

        }

        //Check there are disconnections
        for (Map.Entry<String, Patient> it : _patients.entrySet()) {
            String key = it.getKey();
            if ((false == macToJsonList.containsKey(key) && connected && _patients.get(key).isConnected()) ||
            (false == macToJsonList.containsKey(key) && !connected && !_patients.get(key).isConnected())){
                _patients.remove(key);
            }
        }

    }

    public ArrayList<Patient> getPatientsArray() {
        return new ArrayList<>(_patients.values());
    }

    public String  getHeartrateByMac(String mac) {
        return _patients.containsKey(mac) ? _patients.get(mac).getHeartRate() : "";
    }

    public ArrayList<Treatment> getTreatmentsArrayByMac(String mac) {
        return _patients.containsKey(mac) ? _patients.get(mac).getTreatmentsArray() : null;
    }
//endregion public methods
}
