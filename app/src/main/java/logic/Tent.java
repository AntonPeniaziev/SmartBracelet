package logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import activities.TentActivity;

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
            TentActivity.logger.writeToLog("\n=== updatePatientInfoFromBT ===: MAC = " + it.getKey());
            synchronized(it.getValue()) {
                Iterator i = it.getValue().iterator();
                while (i.hasNext()) {
                    String jsonStr = new String(i.next().toString());
                    AddPatientInfo(jsonStr, it.getKey());
                    TentActivity.logger.writeToLog("\nadded string to patient = " + jsonStr + "|\n");
                    _patients.get(it.getKey()).setConnected(connected);
                    TentActivity.updateToWeb = true;
                }
            }
            TentActivity.logger.writeToLog("\n=== updatePatientInfoFromBT === END ___");
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

    public void updatePatientsTreatment(String mac, Treatment treatment, String newTreatmentName) {
        if (_patients.containsKey(mac)) {
            _patients.get(mac).updateTreatment(treatment.getUid(), newTreatmentName);
        }
    }

    public void setUrgantEvacuation(String mac, boolean value){
        if(_patients.containsKey(mac)){
            _patients.get(mac).setUrgentEvacuation(value);
        }
    }

    public boolean getUrgantEvacuation(String mac){
        if(_patients.containsKey(mac)){
            return _patients.get(mac).getUrgentEvacuationState();
        }
        return false;
    }

    public void setPatientState(String mac, String value){
        if(_patients.containsKey(mac)){
            _patients.get(mac).setPatientState(value);
        }
    }

    public String getPatientState(String mac){
        if(_patients.containsKey(mac)){
            return _patients.get(mac).getPatientState();
        }
        return "";
    }

    public boolean isContain(String mac){
        if (_patients.containsKey(mac)) {
            return true;
        }
        return false;
    }
//endregion public methods
}
