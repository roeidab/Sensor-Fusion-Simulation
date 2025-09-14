package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;

public class TerminatedBroadcast implements Broadcast {
    private String terminationDescription;
    public TerminatedBroadcast(String terminationDescription){
        this.terminationDescription = terminationDescription;
    }
    public String getTerminationDescription() {
        return terminationDescription;
    }
}

