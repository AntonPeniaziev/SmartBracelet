package com.example.androidbtcontrol;

/**
 * Created by avizel on 17/5/2017.
 */

public class Equipment {
    private String name,type, equipment_id;


    public Equipment(String name, String type, String id) {
        this.name = name;
        this.type = type;
        this.equipment_id = id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getEquipment_id() { return equipment_id; }        //added by avizel 10/6

    public void setName(String newName) {
        name = newName;
    }
}
