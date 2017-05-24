package com.example.androidbtcontrol;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

public class Bracelet {
    String _mac_address;
    LinkedHashMap<String, Treatment> _treatments;
    TimeUnit _timeUnit = TimeUnit.SECOND;
    long _absoluteBraceletStartTime = 0;

    public enum TimeUnit {
        SECOND, MINUTE
    }

//region Constructor
    public Bracelet(String initialDataFromBT, String macAddress) {
        _mac_address = macAddress;
        _treatments = new LinkedHashMap<>();
        _absoluteBraceletStartTime = getArduinoStartTimeFromFirstData(initialDataFromBT);
        AddActionsToBracelet(initialDataFromBT);
    }
//endregion Constructor

//region BT message parsing functions
    private String getMessageType(String mes) {

        if (mes.split(",").length > 0 && mes.split(",")[0].split("<").length > 1) {
            return mes.split(",")[0].split("<")[1];
        }
        return "";
    }

    private String getMessageFormattedTime(String mes) {
        int messageTimeField = Integer.parseInt(getTimeField(mes));
        long resultMinutes = _absoluteBraceletStartTime + messageTimeField;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        if (_timeUnit == TimeUnit.SECOND) {
            calendar.setTimeInMillis(resultMinutes * 1000);
            sdf = new SimpleDateFormat("HH:mm:ss");
        }

        return sdf.format(calendar.getTime());
    }

    private String getTimeField(String mes) {
        if (mes.split(",").length > 1) {
            return mes.split(",")[1];
        }
        return "0";
    }
    private String getMessageTsID(String mes) {
        if (mes.split(",").length > 2) {
            return mes.split(",")[2];
        }
        return "";
    }
    private String getMessageUID(String mes) {
        if (mes.split(",").length > 3 && mes.split(",")[3].split(">").length > 0) {
            return mes.split(",")[3].split(">")[0];
        }
        return "";
    }

    private String getMessageTreatmentName(String mes) {
        Equipment equipment = TentActivity.treatmentUidTranslator.get(getMessageUID(mes));
        if (null == equipment) {
            return "Unknown Name";
        }

        return equipment.getName();
    }

    private String getMessageTreatmentType(String mes) {
        Equipment equipment = TentActivity.treatmentUidTranslator.get(getMessageUID(mes));
        if (null == equipment) {
            return "Unknown Type";
        }

        return equipment.getType();
    }

    private long getArduinoStartTimeFromFirstData(String mes) {
        long absoluteStartTime = 0;
        long millisInUnit = 60 * 1000;
        if (_timeUnit == TimeUnit.SECOND) {
            millisInUnit = 1000;
        }
        Calendar c = Calendar.getInstance();
        long mesReceivingTime = c.getTimeInMillis() / millisInUnit;

        if (mes.contains("[") && mes.contains("]")) {
            String[] firstData = mes.split("<");

            absoluteStartTime = mesReceivingTime - Integer.parseInt(getTimeField("<" + firstData[firstData.length - 1]));
        }

        return absoluteStartTime;
    }
    //returns a unique identifier of the Arduino message in the form time|tsid
    private String getMessageUniqueIdentifier(String mes) {
        return getTimeField(mes) + "|" + getMessageTsID(mes);
    }
    // adds new Treatment instance to _treatments from message of type <0,time,tsid,uid>
    private void addTreatmentFromDiamond(String mes) {
        _treatments.put(getMessageUniqueIdentifier(mes),
                new Treatment(getMessageTreatmentName(mes),
                        getMessageTreatmentType(mes), getMessageFormattedTime(mes)));
    }
//endregion BT message parsing functions

//region public methods

    public void AddActionsToBracelet(String inputMessage) {
        if (inputMessage.contains("[") && inputMessage.contains("]")) {
            String[] firstData = inputMessage.split("<");
            for (int i = 1; i < firstData.length; i++) {
                String oneDiamondMessage = "<" + firstData[i];
                if (!getMessageType(oneDiamondMessage).equals("0") ||
                        oneDiamondMessage.contains("#") ||
                        !firstData[i].contains(">")) {//TODO: cases # is sent
                    continue;
                }
                addTreatmentFromDiamond(oneDiamondMessage);
            }
            return;
        }
        //TODO different types of messages and origins
        if (!getMessageType(inputMessage).equals("0") || inputMessage.contains("#")) {
            return;
        }
        addTreatmentFromDiamond(inputMessage);
    }

    public ArrayList<Treatment> getTreatmentsArray() {
        return new ArrayList<>(_treatments.values());
    }
//endregion public methods
}
