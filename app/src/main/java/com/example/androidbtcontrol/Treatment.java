package com.example.androidbtcontrol;

import java.util.ArrayList;

public class Treatment extends Equipment {
    private String arduinoUid;// ts|tsid
    private String lastTime;
    ArrayList<String> _history;

    public Treatment(String name, String type, String equpmentID, String time, String id) {
        super(name, type, equpmentID);
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

    public String generateUpdateRecord(String newNumber, String name) {
        String res;
        if (name == null) {//delete record
            res = "<7," + arduinoUid.split("\\|")[0] + "," + arduinoUid.split("\\|")[1] + ",0>";
        }
        else {
            res = "<3," + arduinoUid.split("\\|")[0] + "," + arduinoUid.split("\\|")[1] + "," + newNumber + ">";
        }
        return res;
    }
}
