package com.example.androidbtcontrol;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Bracelet {
    String _mac_address;
    Map<String, Treatment> _treatments;
    TimeUnit _timeUnit = TimeUnit.MINUTE;
    long _absoluteBraceletStartTime = 0;

    public enum TimeUnit {
        SECOND, MINUTE
    }

//region Constructor
    public Bracelet(String initialDataFromBT, String macAddress) {
        _mac_address = macAddress;
        _treatments = Collections.synchronizedMap(new LinkedHashMap<String, Treatment>());
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
        if (_timeUnit == TimeUnit.MINUTE) {
            calendar.setTimeInMillis(resultMinutes * 1000 * 60);
        }

        return sdf.format(calendar.getTime());
    }

    private String getTimeField(String mes) {
        if (mes.split(",").length > 1) {
            return mes.split(",")[1];
        }
        return "0";
    }
    private String getTimeFieldFromUpdateRec(String mes) {
        if (mes.split(",").length > 3) {
            return mes.split(",")[3];
        }
        return "0";
    }
    private String getMessageTsID(String mes) {
        if (mes.split(",").length > 2) {
            return mes.split(",")[2];
        }
        return "";
    }
    private String getMessageTsIDFromUpdateRec(String mes) {
        if (mes.split(",").length > 4) {
            return mes.split(",")[4];
        }
        return "";
    }
    private String getDataFromUpdateRec(String mes) {
        if (mes.split(",").length > 5 && mes.split(",")[5].split(">").length > 0) {
            return mes.split(",")[5].split(">")[0];
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
        return getTreatmentNameByNumber(getMessageUID(mes));
    }

    private String getTreatmentNameByNumber(String number) {
        Equipment equipment = TentActivity.treatmentUidTranslator.getEquipment(number);
        if (null == equipment) {
            return "Unknown Name";
        }
        return equipment.getName();
    }

    private String getMessageTreatmentType(String mes) {
        Equipment equipment = TentActivity.treatmentUidTranslator.getEquipment(getMessageUID(mes));
        if (null == equipment) {
            return "Unknown Type";
        }

        return equipment.getType();
    }

    private String getMessageTreatmentEquipmentID(String mes) {             //added by avizel 10/6
        Equipment equipment = TentActivity.treatmentUidTranslator.getEquipment(getMessageUID(mes));
        if (null == equipment) {
            return "Unknown Type";
        }

        return equipment.getEquipment_id();
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

            long a = Integer.parseInt(getTimeField("<" + firstData[firstData.length - 1]));
            absoluteStartTime = mesReceivingTime - Integer.parseInt(getTimeField("<" + firstData[firstData.length - 1]));
        }

        return absoluteStartTime;
    }
    //returns a unique identifier of the Arduino message in the form time|tsid
    private String getMessageUniqueIdentifier(String mes) {
        return getTimeField(mes) + "|" + getMessageTsID(mes);
    }
    //returns a target uid from an update message
    private String getTargetUidFromUpdateRec(String mes) {
        return getTimeFieldFromUpdateRec(mes) + "|" + getMessageTsIDFromUpdateRec(mes);
    }
    // adds new Treatment instance to _treatments from message of type <0,time,tsid,uid>
    private void addTreatmentFromDiamond(String mes) {
        //update message record
        if (getMessageType(mes).equals("3")) {
            synchronized (_treatments) {
                String uidKey = getTargetUidFromUpdateRec(mes);
                if(_treatments.containsKey(uidKey)) {
                    _treatments.get(uidKey).updateTreatment(getTreatmentNameByNumber(getDataFromUpdateRec(mes)));
                }
            }
            return;
        }
        //deletion type
        if (getMessageType(mes).equals("7")) {
            synchronized (_treatments) {
                String uidKey = getTargetUidFromUpdateRec(mes);
                if(_treatments.containsKey(uidKey)) {
                    _treatments.remove(uidKey);
                }
            }
            return;
        }
        //ignore all types another from treatment record
        if (!getMessageType(mes).equals("0") || (mes.contains("#") && !mes.contains("<"))
                || !mes.contains(">")) {
            return;
        }

        synchronized (_treatments) {
            _treatments.put(getMessageUniqueIdentifier(mes),
                    new Treatment(getMessageTreatmentName(mes),
                            getMessageTreatmentType(mes),
                            getMessageTreatmentEquipmentID(mes),   //added by avizel 10/6
                            getMessageFormattedTime(mes),
                            getMessageUniqueIdentifier(mes)));
        }
    }
//endregion BT message parsing functions

//region public methods

    public void AddActionsToBracelet(String inputMessage) {
        if (inputMessage.contains("[") && inputMessage.contains("]")) {
            String[] firstData = inputMessage.split("<");
            for (int i = 1; i < firstData.length; i++) {
                String oneDiamondMessage = "<" + firstData[i];
                addTreatmentFromDiamond(oneDiamondMessage);
            }
            return;
        }
        addTreatmentFromDiamond(inputMessage);
    }

    public ArrayList<Treatment> getTreatmentsArray() {
        ArrayList<Treatment> treatmentsArr;
        synchronized(_treatments) {
            treatmentsArr = new ArrayList<>(_treatments.values());
        }
        return treatmentsArr;
    }

    public void updateTreatment(String treatmentUid, String newName) {
        synchronized(_treatments) {
            if (_treatments.containsKey(treatmentUid)) {
                if (newName == null) {
                    _treatments.remove(treatmentUid);
                }
                else {
                    _treatments.get(treatmentUid).updateTreatment(newName);
                }
            }
        }
    }

//endregion public methods
}
