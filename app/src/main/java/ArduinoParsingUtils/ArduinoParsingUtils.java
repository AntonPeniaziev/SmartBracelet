package ArduinoParsingUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import activities.LoginActivity;
import logic.Bracelet;
import logic.Equipment;

/**
 * The following package provides methods for parsing messages received by serial bluetooth device
 * HC-06 or HC-05. The format was defined by Arduino Bracelet team as a part of the "Smart Bracelet"
 * project at Technion, Computer Science dep.
 *
 * Bluetooth protocol:

 The bracelet reads commands via Bluetooth serial with the format <$type,$data>. The following types can be sent to it:

 <1,Integer> on connection this is the first thing should be sent to the bracelet. $data represents a unique number of the phone. as an acknowledgement the bracelet will send back its database with the following format: [<$type,$time,$tsid,$data>, <$type,$time,$tsid,$data>, ..., <$type,$time,$tsid,$data>] also the format <3,$time,$tsid,$pointer_time,$pointer_tsid,$data> may be in the container, which represents an update record (that was added with <3,$time,$tsid,Integer> message from doctor) (records can be tags, app_data or any other things as defined in this doc https://docs.google.com/document/d/1-qHOoyWK5xLiwJvJ40AK0ONJ1xDmyQk-YbECwnUxzqk/edit)
 <2,Integer> - adds new data (such as temperature measurement by doctor). As an acknowledge bracelet sends # back.
 <3,$time,$tsid,Integer> - update record [$time,$tsid] with new data (Integer 14bit max)
 <4,Integer> - adds a record of headquarters communication (such as evacuation notification), $data can be an ID of something, better ask course staff As an acknowledge bracelet sends # back.
 <5,Integer> - adds a record of blood pressure. $data = blood pressure As an acknowledge bracelet sends # back.
 <6,Integer> - Turns on buzzer, $data has no meaning, but should be some kind of Integer. 0 can be put there. As an acknowledge bracelet sends # back.
 <13,Integer> - adds soldier status (severity of injury) $data is id of the status As an acknowledge bracelet sends # back.
 <15,Integer> - adds a custom record As an acknowledge bracelet sends # back.
 For example, the command <1,123> will add a record into the bracelet that represents a new connection by a phone that is represented by the number 123. As a response you will get the database.

 https://github.com/ValkA/BraceletIOT
 */

public class ArduinoParsingUtils {
    private ArduinoParsingUtils(){}

    public static final String BEEP_RECORD = "<6,0>";
    public static final String EVAC_SENT_RECORD = "<4,1>";
    public static final String EVAC_CANCELED_RECORD = "<4,0>";
    public static final String SEVERITY_MINOR_RECORD = "<13,0>";
    public static final String SEVERITY_MODERATE_RECORD = "<13,1>";
    public static final String SEVERITY_SEVERE_RECORD = "<13,2>";
    public static final String SEVERITY_CRITICAL_RECORD = "<13,3>";
    public static final String SEVERITY_DEAD_RECORD = "<13,4>";

    public static String getTimeField(String mes) {
        if (mes.split(",").length > 1) {
            return mes.split(",")[1];
        }
        return "0";
    }

    public static String getTimeFieldFromUpdateRec(String mes) {
        if (mes.split(",").length > 3) {
            return mes.split(",")[3];
        }
        return "0";
    }

    //returns a target uid from an update message
    public static String getTargetUidFromUpdateRec(String mes) {
        return getTimeFieldFromUpdateRec(mes) + "|" + getMessageTsIDFromUpdateRec(mes);
    }

    public static String getMessageTsIDFromUpdateRec(String mes) {
        if (mes.split(",").length > 4) {
            return mes.split(",")[4];
        }
        return "";
    }

    public static String getMessageTsID(String mes) {
        if (mes.split(",").length > 2) {
            return mes.split(",")[2];
        }
        return "";
    }

    public static String getDataFromUpdateRec(String mes) {
        if (mes.split(",").length > 5 && mes.split(",")[5].split(">").length > 0) {
            return mes.split(",")[5].split(">")[0];
        }
        return "";
    }

    public static String getMessageUID(String mes) {
        if (mes.split(",").length > 3 && mes.split(",")[3].split(">").length > 0) {
            return mes.split(",")[3].split(">")[0];
        }
        return "";
    }

    public static String getMessageType(String mes) {

        if (mes.split(",").length > 0 && mes.split(",")[0].split("<").length > 1) {
            return mes.split(",")[0].split("<")[1];
        }
        return "";
    }

    public static String getTreatmentNameByNumber(String number) {
        Equipment equipment = LoginActivity.treatmentUidTranslator.getEquipment(number);
        if (null == equipment) {
            return "Unknown Name";
        }
        return equipment.getName();
    }

    public static String getMessageTreatmentName(String mes) {
        return getTreatmentNameByNumber(getMessageUID(mes));
    }

    public static String getMessageTreatmentType(String mes) {
        Equipment equipment = LoginActivity.treatmentUidTranslator.getEquipment(getMessageUID(mes));
        if (null == equipment) {
            return "Unknown Type";
        }

        return equipment.getType();
    }

    public static String getMessageTreatmentEquipmentID(String mes) {
        Equipment equipment = LoginActivity.treatmentUidTranslator.getEquipment(getMessageUID(mes));
        if (null == equipment) {
            return "Unknown Type";
        }

        return equipment.getEquipment_id();
    }

    public static String getTreatmentNameFromUpdateRecord(String mes) {
        return getTreatmentNameByNumber(getDataFromUpdateRec(mes));
    }

    //returns a unique identifier of the Arduino message in the form time|tsid
    public static String getMessageUniqueIdentifier(String mes) {
        return ArduinoParsingUtils.getTimeField(mes) + "|" + getMessageTsID(mes);
    }

    public static String getMessageFormattedTime(String mes, long arduinoAbsolutestartTime, Bracelet.TimeUnit timeUnit) {
        int messageTimeField = Integer.parseInt(ArduinoParsingUtils.getTimeField(mes));
        long resultMinutes = arduinoAbsolutestartTime + messageTimeField;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        if (timeUnit == Bracelet.TimeUnit.SECOND) {
            calendar.setTimeInMillis(resultMinutes * 1000);
            sdf = new SimpleDateFormat("HH:mm:ss");
        }
        if (timeUnit == Bracelet.TimeUnit.MINUTE) {
            calendar.setTimeInMillis(resultMinutes * 1000 * 60);
        }

        return sdf.format(calendar.getTime());
    }

    public static long getMessageRealTime(String mes, long arduinoAbsolutestartTime) {
        int messageTimeField = Integer.parseInt(ArduinoParsingUtils.getTimeField(mes));
        long resultMinOrSec = arduinoAbsolutestartTime + messageTimeField;

        return resultMinOrSec;
    }

    public static long getArduinoStartTimeFromFirstData(String mes, Bracelet.TimeUnit timeUnit) {
        long absoluteStartTime = 0;
        long millisInUnit = 60 * 1000;
        if (timeUnit == Bracelet.TimeUnit.SECOND) {
            millisInUnit = 1000;
        }
        Calendar c = Calendar.getInstance();
        long mesReceivingTime = c.getTimeInMillis() / millisInUnit;

        if (mes.contains("[") && mes.contains("]")) {
            String[] firstData = mes.split("<");

            long a = Integer.parseInt(ArduinoParsingUtils.getTimeField("<" + firstData[firstData.length - 1]));
            absoluteStartTime = mesReceivingTime - Integer.parseInt(ArduinoParsingUtils.getTimeField("<" + firstData[firstData.length - 1]));
        }

        return absoluteStartTime;
    }

    public static boolean isUpdateRecord(String mes) {
        return getMessageType(mes).equals("3");
    }

    public static boolean isDeletionRecord(String mes) {
        return getMessageType(mes).equals("7");
    }

    public static boolean isEvacuationRecord(String mes) {
        return getMessageType(mes).equals("4");
    }

    public static boolean isSeverityRecord(String mes) {
        return getMessageType(mes).equals("13");
    }

    public static boolean isEvacuationCancelRecord(String mes) {
        return isEvacuationRecord(mes) && getMessageUID(mes).equals("0");
    }

    public static boolean isEvacuationOrderedRecord(String mes) {
        return isEvacuationRecord(mes) && getMessageUID(mes).equals("1");
    }

    public static boolean isMinorSeverityRecord(String mes) {
        return isSeverityRecord(mes) && getMessageUID(mes).equals("0");
    }

    public static boolean isModerateSeverityRecord(String mes) {
        return isSeverityRecord(mes) && getMessageUID(mes).equals("1");
    }

    public static boolean isSevereSeverityRecord(String mes) {
        return isSeverityRecord(mes) && getMessageUID(mes).equals("2");
    }

    public static boolean isCriticalSeverityRecord(String mes) {
        return isSeverityRecord(mes) && getMessageUID(mes).equals("3");
    }

    public static boolean isDeadSeverityRecord(String mes) {
        return isSeverityRecord(mes) && getMessageUID(mes).equals("4");
    }

    public static boolean isNewTreatmentRecord(String mes) {
        return getMessageType(mes).equals("0") ;
    }

    public static boolean isNoise(String mes) {
        return (mes.contains("#") && !mes.contains("<"))
                || !mes.contains(">");
    }

    public static boolean isInitialMessage(String mes) {
        return mes.contains("[") && mes.contains("]");
    }

    public static ArrayList<String> getDiamondMesArrayFromInitial(String mes) {
        ArrayList<String> resArr = new ArrayList<>();
        if (isInitialMessage(mes)) {
            String[] firstData = mes.split("<");
            for (int i = 1; i < firstData.length; i++) {
                String oneDiamondMessage = "<" + firstData[i];
                resArr.add(oneDiamondMessage);
            }
            return resArr;
        }

        return null;
    }
}
