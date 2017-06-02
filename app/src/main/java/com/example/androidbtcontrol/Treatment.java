package com.example.androidbtcontrol;

import java.util.ArrayList;

public class Treatment extends Equipment {
    String arduinoUid;// ts|tsid
    String lastTime;
    ArrayList<String> _history;

    public Treatment(String name, String type, String time, String id) {
        super(name, type);
        this.lastTime = time;
        this.arduinoUid = id;
    }

    public String getLastTime() { return lastTime; }

    public String getUid() {
        return arduinoUid;
    }

    public void updateTreatment(String newValue) {
        this.setName(newValue);
    }

    public String generateUpdateRecord(String newNumber) {
        String res = "<3," + arduinoUid.split("\\|")[0] + "," + arduinoUid.split("\\|")[1] + "," + newNumber + ">";
        return res;
    }
}
