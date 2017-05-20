package com.example.androidbtcontrol;

import java.util.ArrayList;

public class Treatment extends Equipment {
    String /*name,type,*/lastTime;
    ArrayList<String> _history;

    public Treatment(String name, String type, String time) {
        super(name, type);
        //this.name = name;
        //this.type = type;
        this.lastTime = time;
    }

    /*public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }*/

    public String getLastTime() { return lastTime; }
}
