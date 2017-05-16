package com.example.androidbtcontrol;

import java.util.ArrayList;

public class Treatment {
    String name,type,lastTime;
    ArrayList<String> _history;

    public Treatment(String name, String type, String time) {
        this.name = name;
        this.type = type;
        this.lastTime = time;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getLastTime() { return lastTime; }
}
