package com.example.androidbtcontrol;

import java.util.ArrayList;

/**
 * Created by avizel on 17/5/2017.
 */

public class Equipment {
    String name,type;


    public Equipment(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
