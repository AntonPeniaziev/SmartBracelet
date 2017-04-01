package com.example.androidbtcontrol;



public class Patient {
    Bracelet _bracelet;
    // Bracelet MAC + patient registration time
    String id;
    /**
     * id: depends on bracelet reusability + ability to identify soldier in the tent
     * StateDescription : text, voice, image
     * ImmediateInfo : current pulse, blood pressure ...
     */

    public Patient(String initialInfo, String braceletMac) {
        _bracelet = new Bracelet(initialInfo, braceletMac);
        id = braceletMac;
    }

    public String getId() {
        return id;
    }

    public String getJson() {
        return _bracelet.getJsonAsStr();
    }

    public void AddActions(String additionalActions) {
        return;//TBD
    }
}
