package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast{
    private String errorDescription;
    private String sensor;
    public CrashedBroadcast(String Sensor,String errorDescription){
        this.errorDescription = errorDescription;
        this.sensor = Sensor;
    }
    public String getErrorDescription() {
        return errorDescription;
    }
    public String getSensor() {
        return sensor;
    }
}
