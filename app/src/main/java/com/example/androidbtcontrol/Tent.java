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
    ConcurrentHashMap<String, Patient> _patients;

    public Tent() {
        _patients = new ConcurrentHashMap<String, Patient>();
    }
//TODO must be Private! for debug only
    public void AddPatientInfo(String infoFromBracelet, String bt_mac) {

        if (_patients.containsKey(bt_mac)) {
            _patients.get(bt_mac).AddActions(infoFromBracelet);
        }
        else {
            _patients.put(bt_mac, new Patient(infoFromBracelet, bt_mac));
        }
    }

    public String getAllIds() {
        String res = "All Patients:\n";
        for (Map.Entry<String, Patient> pt : _patients.entrySet()
             ) {

            res += "\nJSON from BT: " + pt.getKey().toString() + "\n";
            res += pt.getValue().getJson();
            res += "\n";
        }

        return res;
    }


    /**
     *
     * @return an array of Mac addresses of the bracelets
     */
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

    public void updatePatientInfoTest(){
        int numOfPatientTest = 5;
        for(int i=0; i< numOfPatientTest ; ++i){
            String num = new Integer(i).toString();
            AddPatientInfo("", num);
        }

    }

    public ArrayList<Patient> getPatientsArray() {
        return new ArrayList<Patient>(_patients.values());
    }

    public ArrayList<String> getPatientsMacsArray() {
        return new ArrayList<String>(_patients.keySet());
    }

    public String  getHeartrateByMac(String mac) {
        return _patients.get(mac).getHeartRate();
    }

    public String  getJsonByMac(String mac) {
        return _patients.get(mac).getJson();
    }

    public ArrayList<Treatment> getTreatmentsArrayByMac(String mac) {
        return _patients.get(mac).getTreatmentsArray();
    }

}
