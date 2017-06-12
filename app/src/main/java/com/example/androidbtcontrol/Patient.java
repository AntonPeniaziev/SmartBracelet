package com.example.androidbtcontrol;

import java.util.ArrayList;
import java.util.Random;

public class Patient {
    Bracelet _bracelet;
    String id;
    boolean _isConnected;
    boolean _urgantEvacuation;

    public Patient(String initialInfo, String braceletMac) {
        _bracelet = new Bracelet(initialInfo, braceletMac);
        id = braceletMac;
        _isConnected = false;
        _urgantEvacuation = false;
    }

//region random dummy functions
    public String getHeartRate() {
        Random rand = new Random();
        return Integer.toString(rand.nextInt(200) + 40);
    }

    public String getBloodPressure() {
        Random rand = new Random();
        int sys = rand.nextInt(200) + 90;
        int dia = rand.nextInt(80) + 30;
        return Integer.toString(sys) + "/" + Integer.toString(dia);
    }

    public String getBreatheRate() {
        Random rand = new Random();
        return Integer.toString(rand.nextInt(120) + 10);
    }

    public String getBodyTemp() {
        Random rand = new Random();
        return Double.toString(33 + (42 - 33) * rand.nextDouble()).substring(0,4) + " ºC";
    }
//endregion random dummy functions

//region public methods
    public void AddActions(String additionalActions) {
        _bracelet.AddActionsToBracelet(additionalActions);

    }

    public ArrayList<Treatment> getTreatmentsArray() {
        return _bracelet.getTreatmentsArray();
    }

    public String getBtMac() {
        return id;
    }

    public void setConnected(boolean status) {
        _isConnected = status;
    }

    public boolean isConnected() {
        return _isConnected;
    }

    public void updateTreatment(String treatmentUid, String newName) {
        _bracelet.updateTreatment(treatmentUid, newName);
    }
    //endregion public methods
    public void setUrgantEvacuation(boolean value){ _urgantEvacuation = value; }

    public boolean getUrgantEvacuationState(){ return _urgantEvacuation; }
}

