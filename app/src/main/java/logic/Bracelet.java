package logic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import ArduinoParsingUtils.ArduinoParsingUtils;

public class Bracelet {
    private String _mac_address;
    private Map<String, Treatment> _treatments;
    private TimeUnit _timeUnit = TimeUnit.MINUTE;
    private long _absoluteBraceletStartTime = 0;
    private long _evacuationSentTime = 0;

    private String _severity;

    public enum  TimeUnit {
        SECOND, MINUTE
    }

    boolean evacuationStatus;

//region Constructor
    public Bracelet(String initialDataFromBT, String macAddress) {
        evacuationStatus = false;
        _severity = "";
        _mac_address = macAddress;
        _treatments = Collections.synchronizedMap(new LinkedHashMap<String, Treatment>());
        _absoluteBraceletStartTime = ArduinoParsingUtils.getArduinoStartTimeFromFirstData(initialDataFromBT, _timeUnit);
        AddActionsToBracelet(initialDataFromBT);
    }
//endregion Constructor

    private void handleTreatmentUpdate(String mes) {
        if (ArduinoParsingUtils.isUpdateRecord(mes)) {
            synchronized (_treatments) {
                String uidKey = ArduinoParsingUtils.getTargetUidFromUpdateRec(mes);
                if(_treatments.containsKey(uidKey)) {
                    _treatments.get(uidKey).updateTreatment(ArduinoParsingUtils.getTreatmentNameFromUpdateRecord(mes));
                }
            }
            return;
        }
    }

    private void handleTreatmentDeletion(String mes) {
        if (ArduinoParsingUtils.isDeletionRecord(mes)) {
            synchronized (_treatments) {
                String uidKey = ArduinoParsingUtils.getTargetUidFromUpdateRec(mes);
                if(_treatments.containsKey(uidKey)) {
                    _treatments.remove(uidKey);
                }
            }
            return;
        }
    }

    private void handleEvacuation(String mes) {
        if (ArduinoParsingUtils.isEvacuationCancelRecord(mes)) {
            evacuationStatus = false;
        }
        if (ArduinoParsingUtils.isEvacuationOrderedRecord(mes)) {
            _evacuationSentTime = ArduinoParsingUtils.getMessageRealTime(mes, _absoluteBraceletStartTime);
            evacuationStatus = true;
        }
        return;
    }

    private void handleSeverityLevel(String mes) {
        if (ArduinoParsingUtils.isMinorSeverityRecord(mes)) {
            _severity = "Minor";
        }
        if (ArduinoParsingUtils.isModerateSeverityRecord(mes)) {
            _severity = "Moderate";
        }
        if (ArduinoParsingUtils.isSevereSeverityRecord(mes)) {
            _severity = "Severe";
        }
        if (ArduinoParsingUtils.isCriticalSeverityRecord(mes)) {
            _severity = "Critical";
        }
        if (ArduinoParsingUtils.isDeadSeverityRecord(mes)) {
            _severity = "Dead";
        }
    }

    private void dispatchMessage(String mes) {
        handleTreatmentUpdate(mes);
        handleTreatmentDeletion(mes);
        handleEvacuation(mes);
        handleSeverityLevel(mes);
    }
    // adds new Treatment instance to _treatments
    private void addTreatmentFromDiamond(String mes) {
        dispatchMessage(mes);
        //ignore all types another from treatment record
        if (!ArduinoParsingUtils.isNewTreatmentRecord(mes) || ArduinoParsingUtils.isNoise(mes)) {
            return;
        }
        synchronized (_treatments) {
            _treatments.put(ArduinoParsingUtils.getMessageUniqueIdentifier(mes),
                    new Treatment(ArduinoParsingUtils.getMessageTreatmentName(mes),
                            ArduinoParsingUtils.getMessageTreatmentType(mes),
                            ArduinoParsingUtils.getMessageTreatmentEquipmentID(mes),
                            ArduinoParsingUtils.getMessageFormattedTime(mes, _absoluteBraceletStartTime, _timeUnit),
                            ArduinoParsingUtils.getMessageUniqueIdentifier(mes)));
        }
    }

//region public methods

    public void AddActionsToBracelet(String inputMessage) {
        if (ArduinoParsingUtils.isInitialMessage(inputMessage)) {
            for (String oneDiamondMessage : ArduinoParsingUtils.getDiamondMesArrayFromInitial(inputMessage)) {
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

    public void setEvacStatus(boolean val) {
        evacuationStatus = val;
    }

    public boolean getEvacStatus() {
        return evacuationStatus;
    }

    public void setSeverity(String level) {
        _severity = level;
    }

    public String  getSeverity() {
        return _severity;
    }

    public void setEvacuationTime (long timeInMillis) {
        if (_timeUnit == TimeUnit.MINUTE) {
            _evacuationSentTime = timeInMillis / (60 * 1000);
        }
        if (_timeUnit == TimeUnit.SECOND) {
            _evacuationSentTime = timeInMillis / (1000);
        }
    }

    public boolean evacuationCancelTimedout() {
        int divider = _timeUnit == TimeUnit.MINUTE ? (60 * 1000) : 1000;
        int trsh = _timeUnit == TimeUnit.MINUTE ? 1 : 60;
        return System.currentTimeMillis() / divider - _evacuationSentTime > trsh;
    }

//endregion public methods
}
