package com.example.androidbtcontrol;


import android.util.Log;
import java.util.Random;

public class Patient {
    Bracelet _bracelet;
    // Bracelet MAC + patient registration time
    String id;
    boolean _issueBeep;
    /**
     * id: depends on bracelet reusability + ability to identify soldier in the tent
     * StateDescription : text, voice, image
     * ImmediateInfo : current pulse, blood pressure ...
     */

    public Patient(String initialInfo, String braceletMac) {
        _bracelet = new Bracelet(initialInfo, braceletMac);
        id = braceletMac;
        _issueBeep = false;
    }

    public String getBtMac() {
        return id;
    }

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

    public String getJson() {
        return _bracelet.getJsonAsStr();
    }

    public void AddActions(String additionalActions) {
        _bracelet.AddActionsToBracelet(additionalActions);

    }

    public void beepBracelet() {
        _issueBeep = true;
    }
}
