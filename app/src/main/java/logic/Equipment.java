package logic;


public class Equipment {
    private String name,type, equipment_id;


    /**
     * creates a new equipment object, which might be use as a treatment
     * @param name equipment name
     * @param type type of equipment
     * @param id the id code for that equipment
     */
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

    public String getEquipment_id() { return equipment_id; }

    public void setName(String newName) {
        name = newName;
    }
}
